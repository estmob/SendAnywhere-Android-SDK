package com.estmob.android.sendanywhere.sdk;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.estmob.paprika.transfer.UploadTask;

import java.io.File;
import java.util.List;

public class SendTask extends Task {
    public class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_REQUEST =  (State.ERROR << 8) + 20;
    }

    public interface FileInfo extends UploadTask.FileInfo {
        @NonNull
        @Override
        Uri getUri();

        @NonNull
        @Override
        String getFileName();

        @Override
        long getLength();

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

        if(taskListener != null && state != State.UNKNOWN && detailedState != DetailedState.UNKNOWN) {
            taskListener.onNotify(state, detailedState, obj);
        } else {
            super.onNotify(pState, pDetailedState, obj);
        }
    }
}
