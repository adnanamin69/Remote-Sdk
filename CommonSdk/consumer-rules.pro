# Keep everything from your bundled jars
-keep class com.amazon.whisperplay.fling.** { *; }
-dontwarn com.amazon.whisperplay.fling.**
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.amazon.whisperlink.security.service.AuthDaemonInternal$Client$Factory
-dontwarn com.amazon.whisperlink.security.service.AuthDaemonInternal$Iface


-keep class com.connectsdk.**{ * ; }