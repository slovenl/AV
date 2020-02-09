#include <jni.h>
#include <string>
#include <android/log.h>

#ifndef NELEM
#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

#define LOG_TAG "SLOVEN"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

static void SPlayer_start(JNIEnv* env, jobject thiz){
    LOGD("SPlayer_start");
}

static void SPlayer_stop(JNIEnv* env, jobject thiz){
    LOGD("SPlayer_stop");
}

static void SPlayer_seekTo(JNIEnv* env, jobject thiz){
    LOGD("SPlayer_seekTo");
}

static void SPlayer_pause(JNIEnv* env, jobject thiz){
    LOGD("SPlayer_pause");
}

static JNINativeMethod g_methods[] = {
        { "_start",                 "()V",      (void *) SPlayer_start },
        { "_stop",                  "()V",      (void *) SPlayer_stop },
        { "seekTo",                 "(J)V",     (void *) SPlayer_seekTo },
        { "_pause",                 "()V",      (void *) SPlayer_pause }
};

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    jclass clazz = env->FindClass("com/sloven/player/SPlayer");
    env->RegisterNatives(clazz, g_methods, NELEM(g_methods));
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){

}
