# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep line information for Crashlytics deobfuscated stack traces.
-keepattributes SourceFile,LineNumberTable

# Keep exception class names/messages readable in crash reports.
-keep public class * extends java.lang.Exception

# AndroidX Startup initializer classes are referenced by class name in
# AndroidManifest meta-data, so keep their names and constructors.
-keepnames class * implements androidx.startup.Initializer
-keep class * implements androidx.startup.Initializer {
    <init>();
}

# WorkManager may instantiate workers reflectively.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Kakao SDK ---
# Kakao SDK uses reflection/metadata; keep it to avoid release-only crashes when minify is enabled.
-keep class com.kakao.sdk.** { *; }
-dontwarn com.kakao.sdk.**

# --- Firebase / Google Play Services ---
# Keep public APIs commonly accessed reflectively.
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Navigation SafeArgs custom types ---
# These types are referenced by fully-qualified class name in navigation XML (app:argType).
-keepnames class com.rocket.cosmic_detox.data.datasource.remote.model.AllowedApp

# --- Firestore POJOs (Kotlin data classes) ---
# Firestore maps properties via JavaBean getters (getX/isX). Keep names from obfuscation.
-keepclassmembers class com.rocket.cosmic_detox.data.datasource.remote.model.** { *; }
-keepnames class com.rocket.cosmic_detox.data.datasource.remote.model.**
