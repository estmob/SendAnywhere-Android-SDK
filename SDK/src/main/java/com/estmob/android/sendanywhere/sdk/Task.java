package com.estmob.android.sendanywhere.sdk;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;

import com.estmob.paprika.transfer.TransferTask;


/**
 * Base class for File transfer class, such as {@link SendTask}, {@link ReceiveTask}.
 */
public class Task {
    /**
     * The class contains rough transfer state values.
     */
    public static class State {
        public static final int UNKNOWN = 0;
        public static final int FINISHED = 1;
        public static final int ERROR = 2;
        public static final int PREPARING = 10;
        public static final int TRANSFERRING = 100;
    }

    /**
     * The class contains detailed transfer state values.
     */
    public static class DetailedState {
        public static final int UNKNOWN = 0;

        /**
         * Finished with success
         */
        public static final int FINISHED_SUCCESS = (State.FINISHED << 8) + 1;
        /**
         * Finished by cancel
         */
        public static final int FINISHED_CANCEL = (State.FINISHED << 8) + 2;
        /**
         * Finished with error
         */
        public static final int FINISHED_ERROR = (State.FINISHED << 8) + 3;

        /**
         * Requested with wrong API key or <b>API key is not set</b>
         *
         * @see #init(String)
         */
        public static final int ERROR_WRONG_API_KEY = (State.ERROR << 8) + 1;
        public static final int ERROR_SERVER = (State.ERROR << 8) + 41;

        /**
         * Device is registered to our server, and ready to transfer.
         */
        public static final int PREPARING_UPDATED_DEVICE_ID = (State.PREPARING << 8) + 1;
        /**
         * 6-digit transfer key is created. You can fetch key and its expiry.
         */
        public static final int PREPARING_UPDATED_KEY = (State.PREPARING << 8) + 11;
        /**
         * Transfer file list is updated either to server(send) or from server(receive)
         */
        public static final int PREPARING_UPDATED_FILE_LIST = (State.PREPARING << 8) + 14;

        /**
         * In transfer status
         */
        public static final int TRANSFERRING = (State.TRANSFERRING << 8);
    }

    /**
     * The class contains value keys used to fetch additional information of {@link Task}.
     *
     * @see #getValue(int)
     */
    public static class Value {
        /**
         * Key of 6-digit transfer key
         */
        public static final int KEY = 0x100;
        /**
         * Key of expiration time of corresponding transfer key
         */
        public static final int EXPIRES_TIME = 0x103;
        /**
         * Link URL for recipient
         */
        public static final int LINK_URL = 0x1000;
    }

    /**
     * The class to store transfer status of individual files.
     */
    public static class FileState {
        private Uri file;
        private String pathName;
        private long transferSize;
        private long totalSize;

        FileState(Uri file, String pathName, long transferSize, long totalSize) {
            this.file = file;
            this.pathName = pathName;
            this.transferSize = transferSize;
            this.totalSize = totalSize;
        }

        /**
         * @return {@link Uri} of the file.
         */
        public Uri getFile() {
            return file;
        }

        /**
         * @return pathName of the file.
         */
        public String getPathName() {
            return pathName;
        }

        /**
         * @return transferred size of the file
         */
        public long getTransferSize() {
            return transferSize;
        }

        /**
         * @return total size of the file
         */
        public long getTotalSize() {
            return totalSize;
        }
    }


    /**
     * The callback interface used to indicate the transfer state has been changed.
     * <p>
     * This should be provided to {@link Task#setOnTaskListener(OnTaskListener)}.
     */
    public interface OnTaskListener {
        /**
         * Called when the transfer status has been changed.
         *
         * @param state         The rough transfer state {@link State}.
         * @param detailedState The detailed transfer state {@link DetailedState}.
         * @param obj           Additional information according to the transfer status.<br>
         *                      You must cast to valid types for each cases.<br>
         *                      {@link String} when {@link DetailedState#PREPARING_UPDATED_KEY}<br>
         *                      {@link Task.FileState[]} when {@link DetailedState#PREPARING_UPDATED_FILE_LIST}<br>
         *                      {@link Task.FileState} when {@link DetailedState#TRANSFERRING}
         */
        void onNotify(int state, int detailedState, Object obj);
    }

    /**
     * Sets Send-Anywhere API key.
     * <p>
     * You must call this proceeding to any transfer operations,
     * e.g. {@link android.app.Activity#onCreate}.
     * It is declared as static, so you just have to call it once.
     * <p>
     * Issue your API key at <a href="https://send-anywhere.com/web/page/api" target="_blank">
     * https://send-anywhere.com/web/page/api</a>.
     *
     * @param key Your API Key
     */
    public static void init(String key) {
        TransferTask.setApiKey(key);
    }

    private static String profileName = "Send Anywhere SDK";

    private Context context;
    protected TransferTask task;
    protected OnTaskListener mTaskListener;

    Task(Context context) {
        this.context = context;
    }

    /**
     * Sets profile name shown on the recent device list.
     *
     * @param name Profile name
     */
    public static void setProfileName(String name) {
        profileName = name;
    }

    /**
     * Set the callback that indicates the transfer state has been changed.
     *
     * @param onTaskListener The callback.
     */
    public void setOnTaskListener(@Nullable OnTaskListener onTaskListener) {
        mTaskListener = onTaskListener;
    }

    protected void onNotify(int pState, int pDetailedState, Object obj) {
        int state = State.UNKNOWN;
        int detailedState = DetailedState.UNKNOWN;

        if (pState == TransferTask.State.FINISHED) {
            state = State.FINISHED;
            if (pDetailedState == TransferTask.DetailedState.FINISHED_SUCCESS) {
                detailedState = DetailedState.FINISHED_SUCCESS;
            } else if (pDetailedState == TransferTask.DetailedState.FINISHED_CANCEL) {
                detailedState = DetailedState.FINISHED_CANCEL;
            } else if (pDetailedState == TransferTask.DetailedState.FINISHED_ERROR) {
                detailedState = DetailedState.FINISHED_ERROR;
            }
        } else if (pState == TransferTask.State.ERROR) {
            state = State.ERROR;
            if (pDetailedState == TransferTask.DetailedState.ERROR_WRONG_API_KEY) {
                detailedState = DetailedState.ERROR_WRONG_API_KEY;
            } else {
                detailedState = DetailedState.ERROR_SERVER;
            }
        } else if (pState == TransferTask.State.PREPARING) {
            state = State.PREPARING;
            if (pDetailedState == TransferTask.DetailedState.PREPARING_UPDATED_KEY) {
                detailedState = DetailedState.PREPARING_UPDATED_KEY;
            } else if (pDetailedState == TransferTask.DetailedState.PREPARING_UPDATED_FILE_LIST) {
                detailedState = DetailedState.PREPARING_UPDATED_FILE_LIST;

                TransferTask.FileState[] fileStatus = (TransferTask.FileState[]) obj;
                FileState[] fileState = new FileState[fileStatus.length];
                for (int i = 0; i < fileStatus.length; ++i) {
                    fileState[i] = new FileState(
                            fileStatus[i].getFile(),
                            fileStatus[i].getPathName(),
                            fileStatus[i].getTransferSize(),
                            fileStatus[i].getTotalSize());
                }
                obj = fileState;
            }
        } else if (pState == TransferTask.State.TRANSFERRING) {
            state = State.TRANSFERRING;
            detailedState = DetailedState.TRANSFERRING;

            TransferTask.FileState fileStatus = (TransferTask.FileState) obj;
            obj = new FileState(fileStatus.getFile(),
                    fileStatus.getPathName(),
                    fileStatus.getTransferSize(),
                    fileStatus.getTotalSize());
        }

        if (mTaskListener != null && state != State.UNKNOWN && detailedState != DetailedState.UNKNOWN) {
            mTaskListener.onNotify(state, detailedState, obj);
        }
    }

    /**
     * Starts transfer task for send/receive.
     * <p>
     * Task is executed on the background thread.
     *
     * @see #await()
     */
    public void start() {
        task.setOption(TransferTask.Option.PROFILE_NAME, profileName);
        task.setOnTaskListener(new TransferTask.OnTaskListener() {
            @Override
            public void onNotify(int pState, int pDetailedState, Object obj) {
                Task.this.onNotify(pState, pDetailedState, obj);
            }
        });

        task.start();
    }

    /**
     * Wait until this transfer task is finished.
     */
    public void await() {
        task.await();
    }

    /**
     * Cancel this transfer task.
     */
    public void cancel() {
        task.cancel();
    }

    /**
     * Fetch additional information of this {@link Task}.
     * <p>
     * You should cast return {@link Object} to valid types for each cases.
     *
     * @param key The key of desired value from {@link Task.Value}.
     * @return Returns additional information of task.<br>
     * {@link String} when {@link DetailedState#PREPARING_UPDATED_KEY}<br>
     * {@code long} when {@link DetailedState#PREPARING_UPDATED_KEY}
     * (UNIX Epoch time <b>in seconds</b>)
     */
    public Object getValue(int key) {
        return task.getValue(key);
    }
}
