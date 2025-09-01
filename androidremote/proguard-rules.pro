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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


# Keep the constructor and public methods of FireTvConnection
-keep class com.sdk.remote.androidremote.fire.FireTvConnection {
    public <init>(android.content.Context, java.lang.String,com.sdk.remote.androidremote.ConnectionListener);
    public *;
}


-keep class com.sdk.remote.androidremote.hisense.MqttClientAnd {
    public <init>(android.content.Context, java.lang.String,com.sdk.remote.androidremote.ConnectionListener);
    public *;
}


-keep class com.sdk.remote.androidremote.samsung.SamsungTVWebSocket {
    public <init>(android.content.Context, java.lang.String,com.sdk.remote.androidremote.ConnectionListener);
    public *;
}

-keep class com.sdk.remote.androidremote.roku.RokuViewModel {
    public <init>( java.lang.String,com.sdk.remote.androidremote.ConnectionListener);
    public *;
}
-keep class com.sdk.remote.androidremote.android.PairingManager {
    public <init>( java.lang.String,com.sdk.remote.androidremote.ConnectionListener);
    public *;
}

-keep class com.sdk.remote.androidremote.android.remote.RemoteManager {
    public *;
}


# Gson specific classes
-keep class com.google.gson.stream.** { *; }
-keep class com.connectsdk.**       { * ; }
-keep class com.google.**{ *; }
-keep class org.bouncycastle.**{ *; }
-keep class com.connectsdk.**{ *; }

-dontwarn javax.naming.**

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.

-dontwarn com.google.**
-dontwarn org.bouncycastle.**

-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep class * extends com.google.protobuf.GeneratedMessage { *; }
-keep class * extends com.google.protobuf.AbstractMessageLite { *; }
-keep class com.google.protobuf.** { *; }  # Keep all protobuf internals
-keep class remote.Remotemessage$** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn sun.security.x509.X509Key
