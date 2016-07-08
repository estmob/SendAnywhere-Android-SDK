Send Anywhere Android SDK  [![Download](https://api.bintray.com/packages/estmob/maven/sendanywhere/images/download.svg) ](https://bintray.com/estmob/maven/sendanywhere/_latestVersion)
===
#Prerequisites
Please issue your API key from following link first:
https://send-anywhere.com/web/page/api

#Setup
Send Anywhere Android SDK is available via both `jcenter()` and `mavenCentral()`.
Just add the following line to your gradle dependency:
```gradle
compile ('com.estmob.android:sendanywhere:6.6.10@aar') {
	transitive = true
}
```

#Troubleshooting
If you have any problem or questions with Send Anywhere Android SDK, please create new issue(https://github.com/estmob/SendAnywhere-Android-SDK/issues) or use our customer center(https://send-anywhere.zendesk.com).

### Proguard
If your are using Proguard and it complains during complie, refer [Proguard rules in sample app](https://github.com/estmob/SendAnywhere-Android-SDK/blob/master/app/proguard-rules.pro).

### Runtime permission error: `java.io.IOException: open failed: EACCES (Permission denied)`
From Android Marshmallow(API 23), Android introduced new way to handle application permissions, called "Runtime Permissions". This requires developers to request sensitive permissions to users explicitly in application runtime. Send Anywhere SDK requires external storage permissions(`android.permission.WRITE_EXTERNAL_STORAGE`,`
android.permission.READ_EXTERNAL_STORAGE`) to work properly, but **does NOT** handle these permissions automatically. Please refer these articles to see more about Runtime Permission Model:
* Requesting Permissions at Run Time (Android Developers) -  https://developer.android.com/training/permissions/requesting.html
* Exploring the new Android Permissions Model (Ribot labs) - https://labs.ribot.co.uk/exploring-the-new-android-permissions-model-ba1d5d6c0610#.95cppknud

### API key error: `ERROR_WRONG_API_KEY`
You must call `Task.init("YOUR_API_KEY")` proceeding any transfer operations, e.g. `onCreate` of `Activity`. It is declared as `static`, so you just have to call it once.

If this problem persists, please contact us to re-issue your api-key.

### Conflict with `google-play-services`
Send Anywhere SDK uses `play-services-analytics:8.4.0` internally.
If this conflicts with your `play-services` dependecy, please exclude `play-services` module used in our SDK:
```gradle
compile ('com.estmob.android:sendanywhere:6.6.10@aar') {
    exclude module: "play-services-analytics"
    transitive = true
}
```


#Usage
First look at the source code of [the provided demo](https://github.com/estmob/SendAnywhere-Android-SDK/blob/master/app/src/main/java/com/estmob/android/sendanywhere/sdk/example/MainActivity.java).

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
