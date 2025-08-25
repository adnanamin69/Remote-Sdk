#include <jni.h>
#include <string>

// This is the native C++ function that will return the API key
extern "C"
JNIEXPORT jstring JNICALL
Java_com_adnan_remote_androidremote_YourNDKHelper_getSecretKey(JNIEnv *env, jobject /* this */) {
    std::string secret = "sk-test-abcdef123456";
    return env->NewStringUTF(secret.c_str());
}


#include <jni.h>
#include <string>

// Function to return xapi
extern "C"
JNIEXPORT jstring JNICALL
Java_com_adnan_remote_androidremote_YourNDKHelper_getXApi(JNIEnv *env, jobject /* this */) {
    std::string xapi = "eC1hcGkta2V5";
    return env->NewStringUTF(xapi.c_str());
}

// Function to return api
extern "C"
JNIEXPORT jstring JNICALL
Java_com_adnan_remote_androidremote_YourNDKHelper_getApi(JNIEnv *env, jobject /* this */) {
    std::string api = "MDk4NzY1NDMyMQ";
    return env->NewStringUTF(api.c_str());
}

// Function to return xclient
extern "C"
JNIEXPORT jstring JNICALL
Java_com_adnan_remote_androidremote_YourNDKHelper_getXClient(JNIEnv *env, jobject /* this */) {
    std::string xclient = "eC1jbGllbnQtdG9rZW4";
    return env->NewStringUTF(xclient.c_str());
}