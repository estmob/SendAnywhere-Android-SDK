package com.estmob.android.sendanywhere.sdk.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estmob.android.sendanywhere.sdk.ReceiveTask;
import com.estmob.android.sendanywhere.sdk.SendTask;
import com.estmob.android.sendanywhere.sdk.Task;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    Button sendButton, recvButton;
    private ListView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logView = ((ListView)findViewById(R.id.log));
        logView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<String>()));
        logView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        sendButton = (Button) findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send();
            }
        });

        recvButton = (Button) findViewById(R.id.receive);
        recvButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                receive();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

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
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)logView.getAdapter();
        adapter.add(text);
        adapter.notifyDataSetChanged();
    }

    private void send() {
        if(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "External storage permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        sendButton.setEnabled(false);

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

        final boolean uploadMode = true;
//        final SendTask sendTask = new SendTask(this, new File[] {file1, file2}, uploadMode);
        final Uri uri = Uri.fromFile(file1);
        final SimpleFileInfo fileInfo1 = new SimpleFileInfo(this, uri);
        final SimpleFileInfo fileInfo2 = new SimpleFileInfo(file2);
        if (!fileInfo1.isValid() || !fileInfo2.isValid()) {
            print("ERROR: Invalid file info.");
            return;
        }
        final SendTask sendTask = new SendTask(this, Arrays.asList(fileInfo1, fileInfo2), uploadMode);

        sendTask.setOnTaskListener(new SendTask.OnTaskListener() {
            @Override
            public void onNotify(int state, int detailedState, Object obj) {

                if(state == SendTask.State.PREPARING) {
                    if(detailedState == SendTask.DetailedState.PREPARING_UPDATED_KEY) {
                        String key = (String)obj;
                        if(key != null) {
                            ((TextView)findViewById(R.id.key)).setText(key);
                            print(String.format("Received key: %s", key));
                            print(String.format("Link URL: %s", sendTask.getValue(Task.Value.LINK_URL)));
                            print(String.format("Expires at: %s", new Timestamp((long)sendTask.getValue(Task.Value.EXPIRES_TIME) * 1000)));
                        }
                    }
                } else if(state == SendTask.State.TRANSFERRING) {
                    Task.FileState fileState = (Task.FileState)obj;
                    if(fileState != null) {
                        print(String.format("%s: %s/%s",
                                fileState.getFile().getLastPathSegment(),
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

                    sendButton.setEnabled(true);

                } else if(state == SendTask.State.ERROR) {
                    switch(detailedState) {
                        case SendTask.DetailedState.ERROR_SERVER:
                            print("Network or Server Error!");
                            break;
                        case SendTask.DetailedState.ERROR_NO_REQUEST:
                            print("Timeout for waiting recipient");
                            break;
                    }

                    sendButton.setEnabled(true);
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
    }

    private void receive() {
        if(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "External storage permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        recvButton.setEnabled(false);

        String key = ((TextView)findViewById(R.id.key)).getText().toString();

        ReceiveTask recvTask = new ReceiveTask(this, key);

        recvTask.setOnTaskListener(new ReceiveTask.OnTaskListener() {
            @Override
            public void onNotify(int state, int detailedState, Object obj) {

                if (state == ReceiveTask.State.PREPARING) {
                    if (detailedState == ReceiveTask.DetailedState.PREPARING_UPDATED_FILE_LIST) {
                        Task.FileState[] fileStateList = (Task.FileState[])obj;
                        for(Task.FileState file : fileStateList) {
                            print(String.format("%s: %d bytes",
                                    file.getPathName(), file.getTotalSize()));
                        }
                    }
                } else if (state == ReceiveTask.State.TRANSFERRING) {
                    Task.FileState fileState = (Task.FileState) obj;
                    if (fileState != null) {
                        print(String.format("%s => %s/%s",
                                fileState.getFile().getLastPathSegment(),
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

                    recvButton.setEnabled(true);
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

                    recvButton.setEnabled(true);
                }
            }
        });

        recvTask.start();
    }
}
