package com.estmob.android.sendanywhere.sdk.example;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.estmob.android.sendanywhere.sdk.SendTask;

import java.io.File;

public class SimpleFileInfo implements SendTask.FileInfo {
    private Uri file;
    private String fileName;
    private long length;
    private long lastModified;

    public SimpleFileInfo(File file) {
        set(file, null);
    }

    public SimpleFileInfo(File file, String fileName) {
        set(file, fileName);
    }

    synchronized void set(File file, String fileName) {
        this.file = Uri.fromFile(file);
        this.fileName = fileName != null ? fileName : this.file.getLastPathSegment();
        this.length = file.length();
        this.lastModified = file.lastModified()/1000;
    }

    @NonNull
    @Override
    public Uri getUri() {
        return file;
    }

    @NonNull
    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }
}