# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/boom/devel/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

# apache commons & net stuff
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.**
-keep class org.apache.http.** { *; }
-keep class android.net.http.** { *; }

# gms
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.** { *; }

-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}