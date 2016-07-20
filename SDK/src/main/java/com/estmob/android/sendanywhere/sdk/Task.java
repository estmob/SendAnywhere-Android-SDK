package com.estmob.android.sendanywhere.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.estmob.paprika.transfer.AuthTokenValue;
import com.estmob.paprika.transfer.TransferTask;

import java.io.File;

public class Task {
    public static class State {
        public static final int UNKNOWN = 0;
        public static final int FINISHED = 1;
        public static final int ERROR = 2;
        public static final int PREPARING = 10;
        public static final int TRANSFERRING = 100;
    }

    public static class DetailedState {
        public static final int UNKNOWN = 0;

        public static final int FINISHED_SUCCESS = (State.FINISHED << 8) + 1;
        public static final int FINISHED_CANCEL = (State.FINISHED << 8) + 2;
        public static final int FINISHED_ERROR = (State.FINISHED << 8) + 3;

        public static final int ERROR_WRONG_API_KEY = (State.ERROR << 8) + 1;
        public static final int ERROR_SERVER =  (State.ERROR << 8) + 41;

        public static final int PREPARING_UPDATED_DEVICE_ID = (State.PREPARING << 8) + 1;
        public static final int PREPARING_UPDATED_KEY = (State.PREPARING << 8) + 11;
        public static final int PREPARING_UPDATED_FILE_LIST = (State.PREPARING << 8) + 14;

        public static final int TRANSFERRING = (State.TRANSFERRING << 8);
    }

    public static class Value {
        public static final int KEY = 0x100;
        public static final int EXPIRES_TIME = 0x103;
    }

    public static class FileState {
        private File file;
        private String pathName;
        private long transferSize;
        private long totalSize;

        FileState(File file, String pathName, long transferSize, long totalSize) {
            this.file = file;
            this.pathName = pathName;
            this.transferSize = transferSize;
            this.totalSize = totalSize;
        }

        public File getFile() {
            return file;
        }

        public String getPathName() {
            return pathName;
        }

        public long getTransferSize() {
            return transferSize;
        }

        public long getTotalSize() {
            return totalSize;
        }
    }


    public interface OnTaskListener {
        void onNotify(int state, int detailedState, Object obj);
    }

    public static void init(String key) {
        TransferTask.setApiKey(key);
    }

    private static String profileName = "Send Anywhere SDK";
    private static final String PREF_NAME = "sendanywhere";
    private static AuthTokenValue token;

    private Context context;
    protected TransferTask task;
    protected OnTaskListener taskListener;

    Task(Context context) {
        this.context = context;
    }

    public static void setProfileName(String name) {
        profileName = name;
    }

    public void setOnTaskListener(OnTaskListener callback) {
        taskListener = callback;
    }

    protected void onNotify(int pState, int pDetailedState, Object obj) {
        int state = State.UNKNOWN;
        int detailedState = DetailedState.UNKNOWN;

        if (pState == State.FINISHED) {
            state = State.FINISHED;
            if (pDetailedState == DetailedState.FINISHED_SUCCESS) {
                detailedState = DetailedState.FINISHED_SUCCESS;
            } else if (pDetailedState == DetailedState.FINISHED_CANCEL) {
                detailedState = DetailedState.FINISHED_CANCEL;
            } else if (pDetailedState == DetailedState.FINISHED_ERROR) {
                detailedState = DetailedState.FINISHED_ERROR;
            }
        } else if (pState == State.ERROR) {
            state = State.ERROR;
            if (pDetailedState == DetailedState.ERROR_WRONG_API_KEY) {
                detailedState = DetailedState.ERROR_WRONG_API_KEY;
            } else {
                detailedState = DetailedState.ERROR_SERVER;
            }
        } else if (pState == State.PREPARING) {
            state = State.PREPARING;
            if (pDetailedState == DetailedState.PREPARING_UPDATED_KEY) {
                detailedState = DetailedState.PREPARING_UPDATED_KEY;
            } else if (pDetailedState == DetailedState.PREPARING_UPDATED_FILE_LIST) {
                detailedState = DetailedState.PREPARING_UPDATED_FILE_LIST;

                TransferTask.FileState[] fileStatus = (TransferTask.FileState[])obj;
                FileState[] fileState = new FileState[fileStatus.length];
                for(int i=0; i<fileStatus.length; ++i) {
                    fileState[i] = new FileState(
                            new File(fileStatus[i].getFile().getPath()),
                            fileStatus[i].getPathName(),
                            fileStatus[i].getTransferSize(),
                            fileStatus[i].getTotalSize());
                }
                obj = fileState;
            } else if (pDetailedState == DetailedState.PREPARING_UPDATED_DEVICE_ID) {
                String deviceId = token.getDeviceId();
                String devicePassword = token.getDevicePassword();

                SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("device_id", deviceId);
                editor.putString("device_password", devicePassword);
                editor.commit();
            }
        } else if (pState == State.TRANSFERRING) {
            state = State.TRANSFERRING;
            detailedState = DetailedState.TRANSFERRING;

            TransferTask.FileState fileStatus = (TransferTask.FileState)obj;
            obj = new FileState(new File(fileStatus.getFile().getPath()),
                    fileStatus.getPathName(),
                    fileStatus.getTransferSize(),
                    fileStatus.getTotalSize());
        }

        if(taskListener != null && state != State.UNKNOWN && detailedState != DetailedState.UNKNOWN) {
            taskListener.onNotify(state, detailedState, obj);
        }
    }

    public void start() {
        task.setOptionValues(new TransferTask.Option() {
            @Override
            public String getApiServer() {
                return null;
            }

            @Override
            public String getPushId() {
                return null;
            }

            @Override
            public String getOneSignalId() {
                return null;
            }

            @Override
            public String getProfileName() {
                return profileName;
            }
        });

        if(token == null) {
            SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String deviceId = pref.getString("device_id", null);
            String devicePassword = pref.getString("device_password", null);

            if(deviceId != null && devicePassword != null) {
                token = new AuthTokenValue(deviceId, devicePassword);
            } else {
                token = new AuthTokenValue();
            }
        }
        task.setAuthTokenValue(token);

        task.setOnTaskListener(new TransferTask.OnTaskListener() {
            @Override
            public void onNotify(int pState, int pDetailedState, Object obj) {
                Task.this.onNotify(pState, pDetailedState, obj);
            }
        });

        task.start();
    }

    public void await() {
        task.await();
    }

    public void cancel() {
        task.cancel();
    }

    public Object getValue(int key) {
        return task.getValue(key);
    }
}
