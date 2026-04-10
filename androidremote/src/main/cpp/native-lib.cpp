// Keep sensitive literals in native code
#include <jni.h>
#include <string>

static inline std::string obf(const char *a, const char *b) {
    std::string s(a);
    for (size_t i = 0; i < s.size(); ++i) s[i] ^= b[i % strlen(b)];
    return s;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getSecretKey(JNIEnv *env, jobject) {
    // example placeholder
    const char *k = "\x16\x1f\x0d\x5b\x5c\x1f\x0c\x11\x0f\x1e\x14\x17\x12\x00\x1c\x11\x10\x1d\x0c";
    const char *m = "super_mask";
    std::string v = obf(k, m);
    return env->NewStringUTF(v.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getXApi(JNIEnv *env, jobject) {
    const char *k = "eC1hcGkta2V5"; // base64("x-api-key")
    return env->NewStringUTF(k);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getApi(JNIEnv *env, jobject) {
    const char *k = "MDk4NzY1NDMyMQ"; // example value
    return env->NewStringUTF(k);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getXClient(JNIEnv *env, jobject) {
    const char *k = "eC1jbGllbnQtdG9rZW4"; // base64("x-client-token")
    return env->NewStringUTF(k);
}

// SSL/keystore sensitive pieces
extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getPfxPassword(JNIEnv *env, jobject) {
    const char *pass = "c21hcnRyZW1vdGU"; // base64("smartremote")
    return env->NewStringUTF(pass);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getPfxFileName(JNIEnv *env, jobject) {
    const char *name = "c21hcnRyZW1vdGUucGZ4"; // base64("smartremote.pfx")
    return env->NewStringUTF(name);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getPfxAlias(JNIEnv *env, jobject) {
    const char *alias = "c21hcnRyZW1vdGU"; // base64("smartremote")
    return env->NewStringUTF(alias);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getHisenseBksPassword(JNIEnv *env, jclass) {
    const char *pass = "a3JhZnR3ZXJrOQ"; // base64("kraftwerk9")
    return env->NewStringUTF(pass);
}

// ----- Hisense topic utilities -----
static std::string build(const std::string &a, const std::string &b) {
    std::string r;
    r.reserve(a.size() + b.size());
    r.append(a).append(b);
    return r;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/tv/ui_service/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVPSBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/tv/platform_service/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/mobile/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicBrcsBasePath(JNIEnv *env, jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicRemoBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/tv/remote_service/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

// Leaf paths
#define JSTR(ret) extern "C" JNIEXPORT jstring JNICALL ret

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsSendKeyPath)(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicRemoBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/sendkey";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataAuthenticationPath)(JNIEnv *env,
                                                                                       jobject thiz,
                                                                                       jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/authentication";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataAuthenticationCodePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/authenticationcode";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataAuthenticationCodeClosePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/authenticationcodeclose";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsAuthenticationCodeClosePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/authenticationcodeclose";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsAuthenticationCodePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/authenticationcode";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataHotelModeChangePath)(JNIEnv *env,
                                                                                        jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/ui_service/data/hotelmodechange");
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataTokenIssuancePath)(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/platform_service/data/tokenissuance";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsGetTVStatePath)(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/gettvstate";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicStatePath)(JNIEnv *env, jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/ui_service/state");
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsVidaaAppConnectPath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/vidaa_app_connect";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsApplistPath)(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/applist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsLaunchAppPath)(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/launchapp";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataApplistPath)(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/applist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsChangeSourcePath)(JNIEnv *env,
                                                                                        jobject thiz,
                                                                                        jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/changesource";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsSourcelistPath)(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/sourcelist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataSourcelistPath)(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/sourcelist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataGetTokenPath)(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVPSBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/data/gettoken";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsSetVolumePath)(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVPSBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/changevolume";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsGetVolumePath)(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicTVPSBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/getvolume";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataGetVolumePath)(JNIEnv *env,
                                                                                  jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/platform_service/actions/volumechange");
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicActionsSendTextPath)(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicRemoBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/input";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_RemoteNDKHelper_getTopicDataGetTextPath)(JNIEnv *env,
                                                                                jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/platform_service/actions/bwsinputdata");
}


// ----- SDK and allow checks -----
extern "C" JNIEXPORT jint JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_getSupportedSdk(JNIEnv *env, jobject) {
    return (jint) 36;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sdk_remote_androidremote_RemoteNDKHelper_isSdkSupported(JNIEnv *env, jobject,
                                                                 jint currentSdk) {
    return (jboolean) (currentSdk <= 36);
}

