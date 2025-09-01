// Keep sensitive literals in native code
#include <jni.h>
#include <string>

static inline std::string obf(const char *a, const char *b) {
    std::string s(a);
    for (size_t i = 0; i < s.size(); ++i) s[i] ^= b[i % strlen(b)];
    return s;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getSecretKey(JNIEnv *env, jobject) {
    // example placeholder
    const char *k = "\x16\x1f\x0d\x5b\x5c\x1f\x0c\x11\x0f\x1e\x14\x17\x12\x00\x1c\x11\x10\x1d\x0c";
    const char *m = "super_mask";
    std::string v = obf(k, m);
    return env->NewStringUTF(v.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getXApi(JNIEnv *env, jobject) {
    const char *k = "eC1hcGkta2V5"; // base64("x-api-key")
    return env->NewStringUTF(k);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getApi(JNIEnv *env, jobject) {
    const char *k = "MDk4NzY1NDMyMQ"; // example value
    return env->NewStringUTF(k);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getXClient(JNIEnv *env, jobject) {
    const char *k = "eC1jbGllbnQtdG9rZW4"; // base64("x-client-token")
    return env->NewStringUTF(k);
}

// SSL/keystore sensitive pieces
extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getPfxPassword(JNIEnv *env, jobject) {
    const char *pass = "c21hcnRyZW1vdGU"; // base64("smartremote")
    return env->NewStringUTF(pass);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getPfxFileName(JNIEnv *env, jobject) {
    const char *name = "c21hcnRyZW1vdGUucGZ4"; // base64("smartremote.pfx")
    return env->NewStringUTF(name);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getPfxAlias(JNIEnv *env, jobject) {
    const char *alias = "c21hcnRyZW1vdGU"; // base64("smartremote")
    return env->NewStringUTF(alias);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getHisenseBksPassword(JNIEnv *env, jclass) {
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
Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/tv/ui_service/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVPSBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/tv/platform_service/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/mobile/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicBrcsBasePath(JNIEnv *env, jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicRemoBasePath(JNIEnv *env, jobject,
                                                                       jstring clientId) {
    const char *cid = env->GetStringUTFChars(clientId, nullptr);
    std::string path = std::string("/remoteapp/tv/remote_service/") + cid;
    env->ReleaseStringUTFChars(clientId, cid);
    return env->NewStringUTF(path.c_str());
}

// Leaf paths
#define JSTR(ret) extern "C" JNIEXPORT jstring JNICALL ret

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsSendKeyPath)(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicRemoBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/sendkey";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataAuthenticationPath)(JNIEnv *env,
                                                                                       jobject thiz,
                                                                                       jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/authentication";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataAuthenticationCodePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/authenticationcode";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataAuthenticationCodeClosePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/authenticationcodeclose";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsAuthenticationCodeClosePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/authenticationcodeclose";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsAuthenticationCodePath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/authenticationcode";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataHotelModeChangePath)(JNIEnv *env,
                                                                                        jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/ui_service/data/hotelmodechange");
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataTokenIssuancePath)(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/platform_service/data/tokenissuance";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsGetTVStatePath)(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/gettvstate";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicStatePath)(JNIEnv *env, jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/ui_service/state");
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsVidaaAppConnectPath)(
        JNIEnv *env, jobject thiz, jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/vidaa_app_connect";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsApplistPath)(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/applist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsLaunchAppPath)(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/launchapp";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataApplistPath)(JNIEnv *env,
                                                                                jobject thiz,
                                                                                jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/applist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsChangeSourcePath)(JNIEnv *env,
                                                                                        jobject thiz,
                                                                                        jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/changesource";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsSourcelistPath)(JNIEnv *env,
                                                                                      jobject thiz,
                                                                                      jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVUIBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/sourcelist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataSourcelistPath)(JNIEnv *env,
                                                                                   jobject thiz,
                                                                                   jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicMobiBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/ui_service/data/sourcelist";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataGetTokenPath)(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVPSBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/data/gettoken";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsSetVolumePath)(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVPSBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/changevolume";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsGetVolumePath)(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicTVPSBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/getvolume";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataGetVolumePath)(JNIEnv *env,
                                                                                  jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/platform_service/actions/volumechange");
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicActionsSendTextPath)(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jstring clientId) {
    jstring base = Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicRemoBasePath(env, thiz,
                                                                                          clientId);
    const char *b = env->GetStringUTFChars(base, nullptr);
    std::string path = std::string(b) + "/actions/input";
    env->ReleaseStringUTFChars(base, b);
    env->DeleteLocalRef(base);
    return env->NewStringUTF(path.c_str());
}

JSTR(Java_com_sdk_remote_androidremote_YourNDKHelper_getTopicDataGetTextPath)(JNIEnv *env,
                                                                                jobject) {
    return env->NewStringUTF("/remoteapp/mobile/broadcast/platform_service/actions/bwsinputdata");
}

// ----- Allowed package names -----
extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getAllowedPackages(JNIEnv *env, jobject) {
    const char *pkgs[] = {
            "Y29tLmZpcmV0di5hbWF6b24ubm9kZWpzaW5hbmRyb2lk",
            "Y29tLmFkbmFuLnJlbW90ZS5yZW1vdGVyZXBvMTIz",
            "Y29tLmFkbmFuLmxpdmUuZmlyZXJlbW90ZQ==",
            "dW5pdmVyc2FsLnR2LnJlbW90ZS5jb250cm9sLnNjcmVlbi5jYXN0aW5n",
            "amlsYW4uc21hcnR0di5yZW1vdGUudW5pdmVyc2Fs",//new client remote
            "Y29tLmZpcmUudHYuc3RpY2sucmVtb3RlLmNvbnRyb2w="//fire shams
    };
    size_t count = sizeof(pkgs) / sizeof(pkgs[0]);
    jobjectArray array = env->NewObjectArray((jsize) count, env->FindClass("java/lang/String"),
                                             nullptr);
    for (jsize i = 0; i < (jsize) count; ++i) {
        jstring s = env->NewStringUTF(pkgs[i]);
        env->SetObjectArrayElement(array, i, s);
        env->DeleteLocalRef(s);
    }
    return array;
}

// ----- SDK and allow checks -----
extern "C" JNIEXPORT jint JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_getSupportedSdk(JNIEnv *env, jobject) {
    return (jint) 36;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_isSdkSupported(JNIEnv *env, jobject,
                                                                 jint currentSdk) {
    return (jboolean) (currentSdk <= 36);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_isPackageAllowedEncoded(JNIEnv *env, jobject,
                                                                          jstring encodedPkg) {
    jobjectArray pkgs = Java_com_sdk_remote_androidremote_YourNDKHelper_getAllowedPackages(env,
                                                                                             nullptr);
    jsize len = env->GetArrayLength(pkgs);
    for (jsize i = 0; i < len; ++i) {
        jstring s = (jstring) env->GetObjectArrayElement(pkgs, i);
        const char *a = env->GetStringUTFChars(s, nullptr);
        const char *b = env->GetStringUTFChars(encodedPkg, nullptr);
        bool equal = strcmp(a, b) == 0;
        env->ReleaseStringUTFChars(s, a);
        env->ReleaseStringUTFChars(encodedPkg, b);
        env->DeleteLocalRef(s);
        if (equal) return JNI_TRUE;
    }
    return JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_sdk_remote_androidremote_YourNDKHelper_allowToUse(JNIEnv *env, jobject,
                                                             jstring encodedPkg, jint currentSdk) {
    if (Java_com_sdk_remote_androidremote_YourNDKHelper_isSdkSupported(env, nullptr,
                                                                         currentSdk) == JNI_FALSE) {
        int r = rand() % 11;
        if (r == 10) {
            jclass rt = env->FindClass("java/lang/IllegalArgumentException");
            if (rt) env->ThrowNew(rt, "SDK not supported");
            return JNI_FALSE;
        }
        return JNI_FALSE;
    }
    return Java_com_sdk_remote_androidremote_YourNDKHelper_isPackageAllowedEncoded(env, nullptr,
                                                                                     encodedPkg);
}