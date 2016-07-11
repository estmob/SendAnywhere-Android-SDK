package com.estmob.android.sendanywhere.sdk;

import android.content.Context;

import com.estmob.paprika.transfer.UploadTask;

import java.io.File;

public class SendTask extends Task {
    public class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_REQUEST =  (State.ERROR << 8) + 20;
    }

    public SendTask(Context context, File[] files) {
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
