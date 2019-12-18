/**
 * create by sloven
 * grafika-EGLCore中为java代码，实现到c++代码
 */

#include "EGLCore.h"
#include "MessageQueue.h"


EGLCore::EGLCore() {}

EGLCore::~EGLCore() {}

bool EGLCore::init(EGLContext shareContext) {
    PRINT;
    const EGLint attribs[] = {
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_NONE
    };
    EGLint attribList[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE}; // OpenGL 2.0
    EGLint numConfigs, format;

    //获得默认的显示窗口
    if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay FAILED：%d", eglGetError());
        return false;
    };
    //初始化display
    if (!eglInitialize(display, 0, 0)) {
        LOGE("eglInitialize FAILED：%d", eglGetError());
        release();
        return false;
    }
//    eglChooseConfig(display, attribs, nullptr, 0, &numConfigs);
    if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs)) {
        LOGE("eglChooseConfig FAILED：%d", eglGetError());
        release();
        return false;
    }
    //EGL_NATIVE_VISUAL_ID:关于应原生窗口系统可视ID句柄
    if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {
        LOGE("eglGetConfigAttrib FAILED：%d", eglGetError());
        return false;
    }
    //创建渲染上下文（如果想创建屏幕外渲染区域EGL Pbuffer代码大同小异，但如果想要做的是渲染到一个纹理，则使用帧缓冲区对象更高效）
    if (!(context = eglCreateContext(display, config, shareContext, attribList))) {
        LOGE("eglCreateContext FAILED：%d", eglGetError());
        release();
        return false;
    }
    return true;
}

EGLContext EGLCore::getContext() {
    PRINT;
    return context;
}

EGLDisplay EGLCore::getDisplay() {
    PRINT;
    return display;
}

EGLConfig EGLCore::getConfig() {
    PRINT;
    return config;
}

EGLSurface EGLCore::createWindowSurface(ANativeWindow *window) {
    PRINT;
//    EGLint w, h;
    EGLSurface surface;
    if ((surface = eglCreateWindowSurface(display, config, window, NULL)) != EGL_NO_SURFACE) {
        LOGE("eglCreateWindowSurface FAILED：%d", eglGetError());
        release();
        return EGL_NO_SURFACE;
    }

    if (eglMakeCurrent(display, surface, surface, context) == EGL_FALSE) {
        LOGE("eglMakeCurrent FAILED：%d", eglGetError());
        release();
        return EGL_NO_SURFACE;
    }
//    eglQuerySurface(display, surface, EGL_WIDTH, &w);
//    eglQuerySurface(display, surface, EGL_HEIGHT, &h);

    return surface;
}


void EGLCore::release() {
    PRINT;
    if (EGL_NO_CONTEXT != context && EGL_NO_DISPLAY != display) {
        eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(display, context);
        eglTerminate(display);
    }
    display = EGL_NO_DISPLAY;
    context = EGL_NO_CONTEXT;
    config = NULL;
}

void EGLCore::releaseSurface(EGLSurface surface) {
    PRINT;
    eglDestroySurface(display, surface);
}

EGLSurface EGLCore::createOffscreenSurface(int width, int height) {
    PRINT;
    int surfaceAttribs[] = {
            EGL_WIDTH, width,
            EGL_HEIGHT, height,
            EGL_NONE
    };
    EGLSurface eglSurface;
    if (!(eglSurface = eglCreatePbufferSurface(display, config, surfaceAttribs))) {
        LOGE("eglCreatePbufferSurface FAILED：%d", eglGetError());
    }
    return eglSurface;
}

void EGLCore::makeCurrent(EGLSurface surface) {
    PRINT;
    if (display == EGL_NO_DISPLAY) {
        // called makeCurrent() before create?
        LOGE("makeCurrent FAILED：no display");
    }
    if (!eglMakeCurrent(display, surface, surface, context)) {
        LOGE("eglMakeCurrent FAILED：%d", eglGetError());
    }
}

void EGLCore::makeCurrent(EGLSurface readSurface, EGLSurface drawSurface) {
    PRINT;
    if (display == EGL_NO_DISPLAY) {
        // called makeCurrent() before create?
        LOGE("makeCurrent FAILED：no display");
    }
    if (!eglMakeCurrent(display, readSurface, drawSurface, context)) {
        LOGE("eglMakeCurrent FAILED：%d", eglGetError());
    }
}

void EGLCore::makeNothingCurrent() {
    PRINT;
    if (!eglMakeCurrent(display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)) {
        LOGE("makeNothingCurrent FAILED：%d", eglGetError());
    }
}

bool EGLCore::swapBuffers(EGLSurface eglSurface) {
    PRINT;
    return eglSwapBuffers(display, eglSurface) != 0;
}

int EGLCore::querySurface(EGLSurface eglSurface, int what) {
    PRINT;
    int value;
    eglQuerySurface(display, eglSurface, what, &value);
    return value;
}

//void EGLCore::setPresentationTime(EGLSurface eglSurface, long nsecs){
//    eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs);
//}




