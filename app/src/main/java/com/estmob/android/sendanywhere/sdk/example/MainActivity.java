package com.estmob.android.sendanywhere.sdk.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.estmob.android.sendanywhere.sdk.ReceiveTask;
import com.estmob.android.sendanywhere.sdk.SendTask;
import com.estmob.android.sendanywhere.sdk.Task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView logView = ((ListView)findViewById(R.id.log));
        logView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, new ArrayList<String>()));
        logView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setEnabled(false);
                send();
            }
        });

        findViewById(R.id.receive).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setEnabled(false);
                receive();
            }
        });

        Task.init("YOUR_API_KEY");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    static public File makeDummyFile(long size) throws IOException {
        File file = File.createTempFile("tmp", null);
        file.deleteOnExit();

        OutputStream out = new BufferedOutputStream(
                new FileOutputStream(file), 8192);
        Random generator = new Random();
        for(long i=0; i<size; i++) {
            out.write(generator.nextInt());
        }
        out.close();

        return file;
    }

    private void print(final String text) {
        ListView view = ((ListView)findViewById(R.id.log));
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)view.getAdapter();
        adapter.add(text);
        adapter.notifyDataSetChanged();
    }

    private void send() {
        // Make dummy file.
        File file1 = null;
        File file2 = null;
        try {
            file1 = makeDummyFile(512);
            file2 = makeDummyFile(1024);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SendTask sendTask = new SendTask(this, new File[] {file1, file2});

        sendTask.setOnTaskListener(new SendTask.OnTaskListener() {
            @Override
            public void onNotify(int state, int detailedState, Object obj) {

                if(state == SendTask.State.PREPARING) {
                    if(detailedState == SendTask.DetailedState.PREPARING_UPDATED_KEY) {
                        String key = (String)obj;
                        if(key != null) {
                            ((TextView)findViewById(R.id.key)).setText(key);
                            print(String.format("Received key: %s", key));
                        }
                    }
                } else if(state == SendTask.State.TRANSFERRING) {
                    SendTask.FileInfo fileState = (SendTask.FileInfo)obj;
                    if(fileState != null) {
                        print(String.format("%s: %s/%s",
                                fileState.getFile().getName(),
                                fileState.getTransferSize(), fileState.getTotalSize()));
                    }
                } else if(state == SendTask.State.FINISHED) {
                    switch(detailedState) {
                        case SendTask.DetailedState.FINISHED_SUCCESS:
                            print("Transfer finished (success)");
                            break;
                        case SendTask.DetailedState.FINISHED_CANCEL:
                            print("Transfer finished (canceled)");
                            break;
                        case SendTask.DetailedState.FINISHED_ERROR:
                            print("Transfer finished (error!)");
                            break;
                    }

                    findViewById(R.id.send).setEnabled(true);
                } else if(state == SendTask.State.ERROR) {
                    switch(detailedState) {
                        case SendTask.DetailedState.ERROR_SERVER:
                            print("Network or Server Error!");
                            break;
                        case SendTask.DetailedState.ERROR_NO_REQUEST:
                            print("Timeout for waiting recipient");
                            break;
                        case SendTask.DetailedState.ERROR_NO_EXIST_FILE:
                            print("No exist files!");
                            break;
                    }
                }
            }
        });

        sendTask.start();
        //sendTask.await();
    }

    class Listener implements ReceiveTask.OnTaskListener {
        @Override
        public void onNotify(int state, int detailedState, Object obj) {
        }
    };

    private void receive() {
        String key = ((TextView)findViewById(R.id.key)).getText().toString();

        ReceiveTask recvTask = new ReceiveTask(this, key);

        recvTask.setOnTaskListener(new ReceiveTask.OnTaskListener() {
            @Override
            public void onNotify(int state, int detailedState, Object obj) {

                if (state == ReceiveTask.State.PREPARING) {
                    if (detailedState == ReceiveTask.DetailedState.PREPARING_UPDATED_FILE_LIST) {
                        Task.FileInfo[] fileInfoList = (Task.FileInfo[])obj;
                        for(Task.FileInfo file : fileInfoList) {
                            print(String.format("%s: %d bytes",
                                    file.getPathName(), file.getTotalSize()));
                        }
                    }
                } else if (state == ReceiveTask.State.TRANSFERRING) {
                    ReceiveTask.FileInfo fileState = (ReceiveTask.FileInfo) obj;
                    if (fileState != null) {
                        print(String.format("%s => %s/%s",
                                fileState.getFile().getName(),
                                fileState.getTransferSize(), fileState.getTotalSize()));
                    }
                } else if (state == ReceiveTask.State.FINISHED) {
                    switch (detailedState) {
                        case ReceiveTask.DetailedState.FINISHED_SUCCESS:
                            print("Transfer finished (success)");
                            break;
                        case ReceiveTask.DetailedState.FINISHED_CANCEL:
                            print("Transfer finished (canceled)");
                            break;
                        case ReceiveTask.DetailedState.FINISHED_ERROR:
                            print("Transfer finished (error!)");
                            break;
                    }

                    findViewById(R.id.receive).setEnabled(true);
                } else if (state == ReceiveTask.State.ERROR) {
                    switch (detailedState) {
                        case ReceiveTask.DetailedState.ERROR_SERVER:
                            print("Network or Server Error!");
                            break;
                        case ReceiveTask.DetailedState.ERROR_NO_EXIST_KEY:
                            print("Invalid Key!");
                            break;
                        case ReceiveTask.DetailedState.ERROR_FILE_NO_DOWNLOAD_PATH:
                            print("Invalid download path");
                            break;
                    }
                }
            }
        });

        recvTask.start();
    }
}
