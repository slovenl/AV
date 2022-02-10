//
// Created by yangw on 2019-2-17.
//

#ifndef NATIVEOPENGLDEMO_WLEGLTHREAD_H
#define NATIVEOPENGLDEMO_WLEGLTHREAD_H

#include <EGL/eglplatform.h>
#include "pthread.h"
#include "android/native_window.h"
#include "WlEglHelper.h"
#include <unistd.h>
#include "GLES2/gl2.h"

#define OPENGL_RENDER_AUTO 1
#define OPENGL_RENDER_HANDLE 2

class WlEglThread {

public:

    pthread_t eglThread = -1;
    ANativeWindow *nativeWindow = NULL;

    bool isCreate = false;
    bool isChange = false;
    bool isExit = false;
    bool isStart = false;
    bool isChangeFilter = false;

    int surfaceWidth = 0;
    int surfaceHeight = 0;

    typedef void(*OnCreate)(void *);
    OnCreate onCreate;
    void *onCreteCtx;

    typedef void(*OnChange)(int width, int height, void *);
    OnChange onChange;
    void *onChangeCtx;

    typedef void(*OnDraw)(void *);
    OnDraw onDraw;
    void *onDrawCtx;

    typedef void(*OnChangeFilter)(int width, int height, void *);
    OnChangeFilter onChangeFilter;
    void *onChangeFilterCtx;

    typedef void(*OnDestroy)(void *);
    OnDestroy onDestroy;
    void *onDestroyctx;


    int renderType = OPENGL_RENDER_AUTO;

    pthread_mutex_t pthread_mutex;
    pthread_cond_t pthread_cond;


public:
    WlEglThread();
    ~WlEglThread();

    void onSurfaceCreate(EGLNativeWindowType window);

    void onSurfaceChange(int width, int height);

    void onSurfaceChangeFilter();

    void callBackOnCreate(OnCreate onCreate, void *ctx);

    void callBackOnChange(OnChange onChange, void *ctx);

    void callBackOnDraw(OnDraw onDraw, void *ctx);

    void callBackOnChangeFilter(OnChangeFilter onChangeFilter, void *ctx);

    void callBAckOnDestroy(OnDestroy onDestroy, void *ctx);

    void setRenderType(int renderType);

    void notifyRender();

    void destroy();



};


#endif //NATIVEOPENGLDEMO_WLEGLTHREAD_H
