#include <jni.h>
#include <string>
#include "opengl/WlOpengl.h"

WlOpengl *wlOpengl = NULL;


extern "C"
JNIEXPORT void JNICALL
Java_com_ywl5320_opengl_NativeOpengl_surfaceCreate(JNIEnv *env, jobject instance, jobject surface) {

    // TODO
    if (wlOpengl == NULL) {
        wlOpengl = new WlOpengl();
    }
    wlOpengl->onCreateSurface(env, surface);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ywl5320_opengl_NativeOpengl_surfaceChange(JNIEnv *env, jobject instance, jint width,
                                                   jint height) {

    if (wlOpengl != NULL) {
        LOGE("1 、width %d height %d", width, height);
        wlOpengl->onChangeSurface(width, height);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ywl5320_opengl_NativeOpengl_imgData(JNIEnv *env, jobject instance, jint width, jint height,
                                             jint length, jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    if (wlOpengl != NULL) {
        wlOpengl->setPilex(data, width, height, length);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ywl5320_opengl_NativeOpengl_surfaceDestroy(JNIEnv *env, jobject instance) {

    // TODO
    if (wlOpengl != NULL) {
        wlOpengl->onDestorySurface();
        delete wlOpengl;
        wlOpengl = NULL;
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ywl5320_opengl_NativeOpengl_surfaceChangeFilter(JNIEnv *env, jobject instance) {

    // TODO
    if (wlOpengl != NULL) {
        wlOpengl->onChangeFilter();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_ywl5320_opengl_NativeOpengl_setYuvData(JNIEnv *env, jobject instance, jbyteArray y_,
                                                jbyteArray u_, jbyteArray v_, jint w, jint h) {
    jbyte *y = env->GetByteArrayElements(y_, NULL);
    jbyte *u = env->GetByteArrayElements(u_, NULL);
    jbyte *v = env->GetByteArrayElements(v_, NULL);

    // TODO
    if (wlOpengl != NULL) {
        wlOpengl->setYuvData(y, u, v, w, h);
    }

    env->ReleaseByteArrayElements(y_, y, 0);
    env->ReleaseByteArrayElements(u_, u, 0);
    env->ReleaseByteArrayElements(v_, v, 0);
}