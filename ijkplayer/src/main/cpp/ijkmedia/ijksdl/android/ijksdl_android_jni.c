/*****************************************************************************
 * ijksdl_android.c
 *****************************************************************************
 *
 * Copyright (c) 2013 Bilibili
 * copyright (c) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * This file is part of ijkPlayer.
 *
 * ijkPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * ijkPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ijkPlayer; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

#include "ijksdl_android_jni.h"

#include <unistd.h>
#include "j4a/class/android/os/Build.h"
#include "ijksdl_inc_internal_android.h"
#include "ijksdl_codec_android_mediaformat_java.h"
#include "ijksdl_codec_android_mediacodec_java.h"

static JavaVM *g_jvm;

static pthread_key_t g_thread_key;
static pthread_once_t g_key_once = PTHREAD_ONCE_INIT;

JavaVM *SDL_JNI_GetJvm()
{
    return g_jvm;
}

static void SDL_JNI_ThreadDestroyed(void* value)
{
    JNIEnv *env = (JNIEnv*) value;
    if (env != NULL) {
        ALOGE("%s: [%d] didn't call SDL_JNI_DetachThreadEnv() explicity\n", __func__, (int)gettid());
        (*g_jvm)->DetachCurrentThread(g_jvm);
        pthread_setspecific(g_thread_key, NULL);
    }
}

static void make_thread_key()
{
    //不论哪个线程调用了 pthread_key_create()，所创建的 key 都是所有线程可以访问的，
    // 但各个线程可以根据自己的需要往 key 中填入不同的值，
    // 相当于提供了一个同名而不同值的全局变量(这个全局变量相对于拥有这个变量的线程来说)。
    pthread_key_create(&g_thread_key, SDL_JNI_ThreadDestroyed);
}

jint SDL_JNI_SetupThreadEnv(JNIEnv **p_env)
{
    JavaVM *jvm = g_jvm;
    if (!jvm) {
        ALOGE("SDL_JNI_GetJvm: AttachCurrentThread: NULL jvm");
        return -1;
    }
    //函数在本进程执行序列中仅执行一次g_key_once = PTHREAD_ONCE_INIT
    //即make_thread_key只走一遍
    pthread_once(&g_key_once, make_thread_key);

    JNIEnv *env = (JNIEnv*) pthread_getspecific(g_thread_key);
    if (env) {
        *p_env = env;
        return 0;
    }
    //当在一个线程里面调用AttachCurrentThread后，如果不需要用的时候一定要DetachCurrentThread，否则线程无法正常退出
    //在C++创建的子线程中获取JNIEnv，要通过调用JavaVM的AttachCurrentThread函数获得
    if ((*jvm)->AttachCurrentThread(jvm, &env, NULL) == JNI_OK) {
        //提供了在同一个线程中不同函数间共享数据即线程存储的一种方法
        //使用pthread_getspecific获取env
        pthread_setspecific(g_thread_key, env);
        *p_env = env;
        return 0;
    }

    return -1;
}

void SDL_JNI_DetachThreadEnv()
{
    JavaVM *jvm = g_jvm;

    ALOGI("%s: [%d]\n", __func__, (int)gettid());

    pthread_once(&g_key_once, make_thread_key);

    JNIEnv *env = pthread_getspecific(g_thread_key);
    if (!env)
        return;
    pthread_setspecific(g_thread_key, NULL);

    //从一个Java（Dalvik）虚拟机，分离当前线程。
    if ((*jvm)->DetachCurrentThread(jvm) == JNI_OK)
        return;

    return;
}

int SDL_JNI_ThrowException(JNIEnv* env, const char* className, const char* msg)
{
    //检查上次操作有没有出现异常
    if ((*env)->ExceptionCheck(env)) {
        //获得发生的异常
        jthrowable exception = (*env)->ExceptionOccurred(env);
        //清除异常
        (*env)->ExceptionClear(env);

        if (exception != NULL) {
            ALOGW("Discarding pending exception (%s) to throw", className);
            //删除局部引用，应该可以不需要删除？global的需要执行手动delete
            (*env)->DeleteLocalRef(env, exception);
        }
    }

    jclass exceptionClass = (*env)->FindClass(env, className);
    if (exceptionClass == NULL) {
        ALOGE("Unable to find exception class %s", className);
        /* ClassNotFoundException now pending */
        goto fail;
    }
    //抛出java层异常
    if ((*env)->ThrowNew(env, exceptionClass, msg) != JNI_OK) {
        ALOGE("Failed throwing '%s' '%s'", className, msg);
        /* an exception, most likely OOM, will now be pending */
        goto fail;
    }

    return 0;
fail:
    if (exceptionClass)
        (*env)->DeleteLocalRef(env, exceptionClass);
    return -1;
}

int SDL_JNI_ThrowIllegalStateException(JNIEnv *env, const char* msg)
{
    return SDL_JNI_ThrowException(env, "java/lang/IllegalStateException", msg);
}

jobject SDL_JNI_NewObjectAsGlobalRef(JNIEnv *env, jclass clazz, jmethodID methodID, ...)
{
    va_list args;
    va_start(args, methodID);

    jobject global_object = NULL;
    jobject local_object = (*env)->NewObjectV(env, clazz, methodID, args);
    if (!J4A_ExceptionCheck__throwAny(env) && local_object) {
        global_object = (*env)->NewGlobalRef(env, local_object);
        SDL_JNI_DeleteLocalRefP(env, &local_object);
    }

    va_end(args);
    return global_object;
}

void SDL_JNI_DeleteGlobalRefP(JNIEnv *env, jobject *obj_ptr)
{
    if (!obj_ptr || !*obj_ptr)
        return;

    (*env)->DeleteGlobalRef(env, *obj_ptr);
    *obj_ptr = NULL;
}

void SDL_JNI_DeleteLocalRefP(JNIEnv *env, jobject *obj_ptr)
{
    if (!obj_ptr || !*obj_ptr)
        return;

    (*env)->DeleteLocalRef(env, *obj_ptr);
    *obj_ptr = NULL;
}


int SDL_Android_GetApiLevel()
{
    static int SDK_INT = 0;
    if (SDK_INT > 0)
        return SDK_INT;

    JNIEnv *env = NULL;
    if (JNI_OK != SDL_JNI_SetupThreadEnv(&env)) {
        ALOGE("SDL_Android_GetApiLevel: SetupThreadEnv failed");
        return 0;
    }

    SDK_INT = J4AC_android_os_Build__VERSION__SDK_INT__get__catchAll(env);
    ALOGI("API-Level: %d\n", SDK_INT);
    return SDK_INT;
#if 0
    char value[PROP_VALUE_MAX];
    memset(value, 0, sizeof(value));
    __system_property_get("ro.build.version.sdk", value);
    SDK_INT = atoi(value);
    return SDK_INT;
#endif
}


JNIEXPORT jint JNICALL SDL_JNI_OnLoad(JavaVM *vm, void *reserved)
{
    int retval;
    JNIEnv* env = NULL;

    g_jvm = vm;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    retval = J4A_LoadAll__catchAll(env);
    JNI_CHECK_RET(retval == 0, env, NULL, NULL, -1);

    return JNI_VERSION_1_4;
}

