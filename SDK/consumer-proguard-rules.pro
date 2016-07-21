### apache commons & net stuff
-dontnote org.apache.commons.**
-dontnote org.apache.http.**
-dontnote android.net.http.**

### OKHTTP

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote okhttp.internal.Platform
-dontnote com.squareup.okhttp.internal.Platform


### OKIO

# java.nio.file.* usage which cannot be used at runtime. Animal sniffer annotation.
-dontwarn okio.Okio
# JDK 7-only method which is @hide on Android. Animal sniffer annotation.
-dontwarn okio.DeflaterSink

#error : Note: the configuration refers to the unknown class 'com.google.vending.licensing.ILicensingService'
#solution : @link http://stackoverflow.com/a/14463528
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

# GMS
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }