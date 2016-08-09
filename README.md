Send Anywhere Android SDK  [![Download](https://api.bintray.com/packages/estmob/maven/sendanywhere/images/download.svg) ](https://bintray.com/estmob/maven/sendanywhere/_latestVersion)
===

[Project homepage](http://estmob.github.io/SendAnywhere-Android-SDK/)

# NOTICE
We have following important changes on `v6.7.20` and later:
 - `SendTask.FileInfo` interface added to support sending non-traditional files.
 - `Task.FileInfo` is renamed to `Task.FileState` for avoiding confusion with `SendTask.FileInfo`.

See [Releases](https://github.com/estmob/SendAnywhere-Android-SDK/releases) for more details.

# Prequisites
Please issue your API key from following link first:
https://send-anywhere.com/web/page/api

# Setup
Send Anywhere Android SDK is available via both `jcenter()` and `mavenCentral()`.
Just add the following line to your gradle dependency:
```gradle
compile ('com.estmob.android:sendanywhere:6.7.21@aar') {
	transitive = true
}
```

# Troubleshooting
If you have any problem or questions with Send Anywhere Android SDK, please create new issue(https://github.com/estmob/SendAnywhere-Android-SDK/issues) or contact to our customer center(https://send-anywhere.zendesk.com).

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
Send Anywhere SDK uses `play-services-analytics:9.0.2` internally.
If this conflicts with your `play-services` dependency, please exclude `play-services` module used in our SDK:
```gradle
compile ('com.estmob.android:sendanywhere:x.x.x@aar') {
    exclude module: "play-services-analytics"
    transitive = true
}
```


# Usage
First look at the source code of [the provided demo](https://github.com/estmob/SendAnywhere-Android-SDK/blob/master/app/src/main/java/com/estmob/android/sendanywhere/sdk/example/MainActivity.java).

[Javadoc](http://estmob.github.io/SendAnywhere-Android-SDK/javadoc/6.x/)

Task Constructors
---

```java
public class SendTask extends Task {
    public SendTask(Context context, File[] files);

    public SendTask(Context context, List<? extends FileInfo> files);

	public interface FileInfo {
        @NonNull Uri getUri();
        @NonNull String getFileName();
        long getLength();
        long getLastModified();
    }
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

### SendTask(Context context, List<? extends FileInfo> files)
Parameters |                                      							   |
-----------| ------------------------------------------------------------------|
context    | The current context.                 							   |
files      | The file list with your own implementation of `SendTask.FileInfo` |

You can provide your own implementation of `SendTask.FileInfo` for non-traditional files that cannot be represented as `Java.io.File`, such as files starting with `content://..`

See [SimpleFileInfo in sample](https://github.com/estmob/SendAnywhere-Android-SDK/blob/master/app/src/main/java/com/estmob/android/sendanywhere/sdk/example/SimpleFileInfo.java) for the example implementaion.

#### SendTask.FileInfo interface
Methods         | Return type	|                                   |
--------------- | ------------- |---------------------------------- |
getUri          | Uri           | The Uri of sending file.          |
getFileName     | String        | The file name for recevier.       |
getLength       | long          | The length of sending file.       |
getLastModified | long          | The last modified time in seconds |

### ReceiveTask(Context context, String key, File destDir)
Parameters |                           |
---------- | --------------------------|
context    | The current context.      |
key        | The KEY of sender.        |
destDir    | The folder to save files. |


Public methods
---

```java
public class Task {
    ...

    public static void init(String key);

    public static void setProfileName(String name);

    public void start();

    public void await();

    public void cancel();

    public Object getValue();

    ...
}
```

### public static void init(String key)
Set your API key.

Parameters |               |
-----------| --------------|
key        | Your API Key. |

### public static void setProfileName(String name)
Set profile name of the device

Parameters |                     |
-----------| --------------------|
name       | Desired device name |

### public void start()
Start task for sending or receiving.

### public void await()
Wait until task is finished.

### public void cancel()
Cancel the task to stop.

### public Object getValue(int key)
Fetch additional information of `Task`. You should cast return `Object` to vaild types for each cases.

```java
public static class Value {
    public static final int KEY;
    public static final int EXPIRES_TIME;
}
```

Task.Value (key)| Task.DetailedState (available on)  | Type                                  |
----------------|------------------------------------|---------------------------------------|
KEY             | PREPARING_UPDATED_KEY              | String                                |
EXPIRES_TIME    | PREPARING_UPDATED_KEY              | long (UNIX Epoch time **in seconds**) |


Listeners for `Task`
---
```java
public class Task {
    ...

    public interface OnTaskListener {
        void onNotify(int state, int detailedState, Object obj);
    }

    public void setOnTaskListener(OnTaskListener callback)

    ...
}
```


```java
public class Task {
    public static class State {
        public static final int UNKNOWN;
        public static final int FINISHED;
        public static final int ERROR;
        public static final int PREPARING;
        public static final int TRANSFERRING;
    }

    public static class DetailedState {
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

```java
public class SendTask extends Task {
    public static class DetailedState extends Task.DetailedState {
        public static final int ERROR_NO_REQUEST;
    }

    ...
}
```

```java
public class ReceiveTask extends Task {
    public static class DetailedState extends Task.DetailedState {
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
             | ERROR_NO_EXIST_KEY           |                 |
             | ERROR_FILE_NO_DOWNLOAD_PATH  |                 |
             | ERROR_FILE_NO_DISK_SPACE     |                 |
             | ERROR_FILE_DISK_NOT_MOUNTED  |                 |
PREPARING    | PREPARING_UPDATED_KEY        | String          |
             | PREPARING_UPDATED_FILE_LIST  | Task.FileInfo[] |
TRANSFERRING | TRANSFERRING                 | Task.FileInfo   |


Flow Step
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

FileState (File Transfer State Information)
---
```java
public class Task {
    ...

    public static class FileSate {
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
