# Gson specific classes
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keep class com.google.gson.examples.android.model.** { *; }



-dontwarn com.google.**
-dontwarn org.bouncycastle.**

# Keep SSL/TLS related classes (if using custom TrustManager, SSLContext, etc.)
-keep class javax.net.ssl.** { *; }

# Keep MessageDigest and related
-keep class java.security.** { *; }
-keep class javax.security.** { *; }

# Prevent ProGuard from stripping public key classes
-keep class java.security.cert.X509Certificate { *; }
-keep class java.security.interfaces.RSAPublicKey { *; }



-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep class * extends com.google.protobuf.AbstractMessageLite { *; }
-keep class com.google.protobuf.** { *; }  # Keep all protobuf internals
-keep class remote.Remotemessage$** { *; }
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn sun.security.x509.X509Key

-keepclassmembers class * {
    public <fields>;
    public <methods>;
}


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.beans.PropertyEditorManager
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.management.MBeanServer
-dontwarn javax.management.MalformedObjectNameException
-dontwarn javax.management.ObjectInstance
-dontwarn javax.management.ObjectName
-dontwarn javax.management.openmbean.CompositeType
-dontwarn javax.management.openmbean.OpenDataException
-dontwarn javax.management.openmbean.OpenType
-dontwarn javax.management.openmbean.SimpleType
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.beans.PropertyEditor
-dontwarn javax.management.openmbean.CompositeData
-dontwarn javax.management.openmbean.CompositeDataSupport


# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn sun.security.x509.X509Key
