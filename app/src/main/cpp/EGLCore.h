/**
 * create by sloven
 * grafika-EGLCore中为java代码，实现到c++代码
 */

#ifndef AV_EGL_CORE_H
#define AV_EGL_CORE_H

#include <EGL/egl.h>
#include <cstdio>
#include "log.h"

class EGLCore {

public:
    EGLCore();

    virtual ~EGLCore();

    bool init(EGLContext shareContext);

    EGLSurface createWindowSurface(ANativeWindow *_window);

    EGLSurface createOffscreenSurface(int width, int height);

    void makeCurrent(EGLSurface surface);

    void makeCurrent(EGLSurface readSurface, EGLSurface drawSurface);

    void makeNothingCurrent();

    bool swapBuffers(EGLSurface eglSurface);
//    void setPresentationTime(EGLSurface eglSurface, long nsecs);

//    bool isCurrent(EGLSurface eglSurface);
    int querySurface(EGLSurface eglSurface, int what);

    void release();

    void releaseSurface(EGLSurface surface);

    EGLContext getContext();

    EGLDisplay getDisplay();

    EGLConfig getConfig();

private:
    EGLDisplay display;
    EGLConfig config;
    EGLContext context;

};

#endif //AV_EGL_CORE_H
