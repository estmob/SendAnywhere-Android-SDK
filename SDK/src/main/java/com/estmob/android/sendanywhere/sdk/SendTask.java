package com.estmob.android.sendanywhere.sdk;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.estmob.paprika.transfer.UploadTask;

import java.io.File;
import java.util.List;

/**
 * The class for sending file to receiver.
 */
public class SendTask extends Task {
    /**
     * The class contains detailed transfer state values for {@link SendTask}.
     */
    public class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_REQUEST = (State.ERROR << 8) + 20;
    }

    /**
     * The interface to provide file information of sending files.
     * <p>
     * You can provide your own implementation of {@link FileInfo} for non-traditional files that
     * cannot be represented as {@link java.io.File}, such as files starting with {@code content://..}
     * <p>
     * See <a target="_blank" href="https://github.com/estmob/SendAnywhere-Android-SDK/blob/master/app/src/main/java/com/estmob/android/sendanywhere/sdk/example/SimpleFileInfo.java">SimpleFileInfo</a>
     * in sample for the example implementation of traditional {@link java.io.File}.
     */
    public interface FileInfo extends UploadTask.FileInfo {
        /**
         * Returns {@link android.net.Uri} to file.
         */
        @NonNull
        @Override
        Uri getUri();

        /**
         * Returns file name including path for receiver.
         */
        @NonNull
        @Override
        String getFileName();

        /**
         * Returns total file size.
         */
        @Override
        long getLength();

        /**
         * Returns last modified time of the file in UNIX Epoch time <b>in seconds</b>
         */
        @Override
        long getLastModified();
    }

    public SendTask(Context context, File[] files) {
        super(context);

        task = new UploadTask(context, files);
    }

    public SendTask(Context context, List<? extends FileInfo> files) {
        super(context);

        task = new UploadTask(context, files);
    }

    protected void onNotify(int pState, int pDetailedState, Object obj) {
        int state = State.UNKNOWN;
        int detailedState = DetailedState.UNKNOWN;

        if (pState == State.ERROR) {
            state = State.ERROR;
            if (pDetailedState == DetailedState.ERROR_NO_REQUEST) {
                detailedState = DetailedState.ERROR_NO_REQUEST;
            }
        }

        if (mTaskListener != null && state != State.UNKNOWN && detailedState != DetailedState.UNKNOWN) {
            mTaskListener.onNotify(state, detailedState, obj);
        } else {
            super.onNotify(pState, pDetailedState, obj);
        }
    }
}
