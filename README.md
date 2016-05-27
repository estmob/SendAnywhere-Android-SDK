Send Anywhere Android SDK [ ![Download](https://api.bintray.com/packages/estmob/maven/sendanywhere/images/download.svg) ](https://bintray.com/estmob/maven/sendanywhere/_latestVersion)
===

#Setup
```gradle
compile 'com.estmob.android:sendanywhere-transfer:6.5.4@aar'
compile 'com.estmob.android:sendanywhere:1.0.0@aar'
```

#Usage
Task Constructor
---

```
public class SendTask extends Task {
    public SendTask(Context context, File[] files);

    ...
}

public class ReceiveTask extends Task {
    public ReceiveTask(Context context, String key, File destDir);

    ...
}
```

### SendTask(Context context, File[] files)
Parameters |                                      |
-----------| -------------------------------------|
context    | The current context.                 |
files      | The file list what you want to send. |

### ReceiveTask(Context context, String key, File destDir)
Parameters |                      |
-----------| ---------------------|
context    | The current context. |
key        | The KEY of sender.   |
destDir    | Save folder.         |



Public method
---

```
public class Task {
    ...

    public static void init(String key);

    public void start();

    public void await();

    public void cancel();

    ...
}
```

### static void init(String key)
Set your API key.

Parameters |               |
-----------| --------------|
key        | Your API Key. |

### void start()
Start task for sending or receiving.

### void await()
Wait until task is finished.

### void cancel()
Cancel the task to stop.


Listener for task
---
```
public class Task {
    ...

    public interface OnTaskListener {
        void onNotify(int state, int detailedState, Object obj);
    }

    public void setOnTaskListener(OnTaskListener callback)

    ...
}
```


```
public class Task {
    ...

    public class State {
        public static final int UNKNOWN;
        public static final int FINISHED;
        public static final int ERROR;
        public static final int PREPARING;
        public static final int TRANSFERRING;
    }

    public class DetailedState {
        public static final int UNKNOWN;

        public static final int FINISHED_SUCCESS;
        public static final int FINISHED_CANCEL;
        public static final int FINISHED_ERROR;

        public static final int ERROR_WRONG_API_KEY;
        public static final int ERROR_SERVER;

        public static final int PREPARING_UPDATED_KEY;
        public static final int PREPARING_UPDATED_FILE_LIST;

        public static final int TRANSFERRING;
    }

    ...
}
```

```
public class SendTask extends Task {
    ...

    public class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_REQUEST;
        public static final int ERROR_NO_EXIST_FILE;
    }

    ...
}
```

```
public class ReceiveTask extends Task {
    ...

    public class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_EXIST_KEY;
        public static final int ERROR_FILE_NO_DOWNLOAD_PATH;
        public static final int ERROR_FILE_NO_DISK_SPACE;
        public static final int ERROR_FILE_DISK_NOT_MOUNTED;
    }

    ...
}
```

### public void setOnTaskListener(OnTaskListener callback)
Register a callback to be invoked when transfer event occurs.

### void onNotify(int state, int detailedState, Object obj)

Task.State   | Task.DetailedState           | param           |
-------------|----------------------------- |-----------------|
FINISHED     | FINISHED_SUCCESS             |                 |
             | FINISHED_CANCEL              |                 |
             | FINISHED_ERROR               |                 |
ERROR        | ERROR_WRONG_API_KEY          |                 |
             | ERROR_SERVER                 |                 |
             | ERROR_NO_REQUEST             |                 |
             | ERROR_NO_EXIST_FILE          |                 |
             | ERROR_NO_EXIST_KEY           |                 |
             | ERROR_FILE_NO_DOWNLOAD_PATH  |                 |
             | ERROR_FILE_NO_DISK_SPACE     |                 |
             | ERROR_FILE_DISK_NOT_MOUNTED  |                 |
PREPARING    | PREPARING_UPDATED_KEY        | String          |
             | PREPARING_UPDATED_FILE_LIST  | Task.FileInfo[] |
TRANSFERRING | TRANSFERRING                 | Task.FileInfo   |


* Flow Step
  * PREPARING_UPDATED_KEY
    * PREPARING_UPDATED_FILE_LIST
      * TRANSFERRING
      * TRANSFERRING
      * ...
      * TRANSFERRING
      * TRANSFERRING
        * FINISHED_SUCCESS
        * FINISHED_CANCEL
        * ERROR
          * FINISHED_ERROR

File Information
---
```
public class Task {
    ...

    public class FileInfo {
        public File getFile();
        public String getPathName();
        public long getTransferSize();
        public long getTotalSize();
    }

    ...
}
```

### File getFile()
Return the File object.

### String getPathName()
Return the relative path and file name.

### long getTransferSize()
Returns the transfer size in bytes.

### long getTotalSize();
Return the size of the file in bytes.
