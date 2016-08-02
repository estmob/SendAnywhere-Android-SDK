package com.estmob.android.sendanywhere.sdk;

import android.content.Context;
import android.net.Uri;

import com.estmob.paprika.transfer.DownloadTask;

import java.io.File;

/**
 * The class for receiving file from sender.
 */
public class ReceiveTask extends Task {
    /**
     * The class contains detailed transfer state values for {@link ReceiveTask}.
     */
    public static class DetailedState extends Task.DetailedState {
        /**
         * Wrong transfer key.
         */
        public static final int ERROR_NO_EXIST_KEY = (Task.State.ERROR << 8) + 20;
        /**
         * Cannot find download path, e.g. Permission problem.
         */
        public static final int ERROR_FILE_NO_DOWNLOAD_PATH = (Task.State.ERROR << 8) + 21;
        /**
         * Destination disk is full.
         */
        public static final int ERROR_FILE_NO_DISK_SPACE = (Task.State.ERROR << 8) + 22;
        /**
         * Disk is not mounted.
         */
        public static final int ERROR_FILE_DISK_NOT_MOUNTED = (Task.State.ERROR << 8) + 23;
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

        if (pState == State.ERROR) {
            state = State.ERROR;
            if (pDetailedState == DetailedState.ERROR_NO_EXIST_KEY) {
                detailedState = DetailedState.ERROR_NO_EXIST_KEY;
            } else if (pDetailedState == DetailedState.ERROR_FILE_NO_DOWNLOAD_PATH) {
                detailedState = DetailedState.ERROR_FILE_NO_DOWNLOAD_PATH;
            } else if (pDetailedState == DetailedState.ERROR_FILE_NO_DISK_SPACE) {
                detailedState = DetailedState.ERROR_FILE_NO_DISK_SPACE;
            } else if (pDetailedState == DetailedState.ERROR_FILE_DISK_NOT_MOUNTED) {
                detailedState = DetailedState.ERROR_FILE_DISK_NOT_MOUNTED;
            }
        }

        if (mTaskListener != null && state != State.UNKNOWN && detailedState != DetailedState.UNKNOWN) {
            mTaskListener.onNotify(state, detailedState, obj);
        } else {
            super.onNotify(pState, pDetailedState, obj);
        }
    }
}
