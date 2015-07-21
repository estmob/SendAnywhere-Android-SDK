package com.estmob.android.sendanywhere.sdk;

import android.content.Context;
import android.net.Uri;

import com.estmob.paprika.transfer.DownloadTask;
import com.estmob.paprika.transfer.TransferTask;

import java.io.File;

public class ReceiveTask extends Task {
    public class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_EXIST_KEY =  (TransferTask.State.ERROR << 8) + 20;
        public static final int ERROR_FILE_NO_DOWNLOAD_PATH =  (TransferTask.State.ERROR << 8) + 21;
        public static final int ERROR_FILE_NO_DISK_SPACE =  (TransferTask.State.ERROR << 8) + 22;
        public static final int ERROR_FILE_DISK_NOT_MOUNTED =  (TransferTask.State.ERROR << 8) + 23;
    }

    public ReceiveTask(Context context, String key) {
        super(context);

        task = new DownloadTask(context, key);
    }

    public ReceiveTask(Context context, String key, File destDir) {
        super(context);

        task = new DownloadTask(context, key, Uri.fromFile(destDir));
    }

    protected void onNotify(int pState, int pDetailedState, Object obj) {
        int state = State.UNKNOWN;
        int detailedState = DetailedState.UNKNOWN;

        if (pState == DownloadTask.State.ERROR) {
            state = State.ERROR;
            if (pDetailedState == DownloadTask.DetailedState.ERROR_NO_EXIST_KEY) {
                detailedState = DetailedState.ERROR_NO_EXIST_KEY;
            } else if (pDetailedState == DownloadTask.DetailedState.ERROR_FILE_NO_DOWNLOAD_PATH) {
                detailedState = DetailedState.ERROR_FILE_NO_DOWNLOAD_PATH;
            } else if (pDetailedState == DownloadTask.DetailedState.ERROR_FILE_NO_DISK_SPACE) {
                detailedState = DetailedState.ERROR_FILE_NO_DISK_SPACE;
            } else if (pDetailedState == DownloadTask.DetailedState.ERROR_FILE_DISK_NOT_MOUNTED) {
                detailedState = DetailedState.ERROR_FILE_DISK_NOT_MOUNTED;
            }
        }

        if(taskListener != null && state != State.UNKNOWN && detailedState != DetailedState.UNKNOWN) {
            taskListener.onNotify(state, detailedState, obj);
        } else {
            super.onNotify(pState, pDetailedState, obj);
        }
    }
}
