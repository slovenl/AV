#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include "JavaCallHelper.h"
extern  "C"{
#include "libavcodec/avcodec.h"
}
JavaCallHelper *javaCallHelper;
#include "WangYiFFmpeg.h"
ANativeWindow *window = 0;
WangYiFFmpeg *wangYiFFmpeg;

//线程  ----》javaVM
JavaVM *javaVM = NULL;
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    return JNI_VERSION_1_4;
}
void renderFrame(uint8_t *data, int linesize, int w, int h) {
//    渲染
    //设置窗口属性
    ANativeWindow_setBuffersGeometry(window, w,
                                     h,
                                     WINDOW_FORMAT_RGBA_8888);

    ANativeWindow_Buffer window_buffer;
    if (ANativeWindow_lock(window, &window_buffer, 0)) {
        ANativeWindow_release(window);
        window = 0;
        return;
    }
//    缓冲区  window_data[0] =255;
    uint8_t *window_data = static_cast<uint8_t *>(window_buffer.bits);
//    r     g   b  a int
    int window_linesize = window_buffer.stride * 4;
    uint8_t *src_data = data;
    for (int i = 0; i < window_buffer.height; ++i) {
        memcpy(window_data + i * window_linesize, src_data + i * linesize, window_linesize);
    }
    ANativeWindow_unlockAndPost(window);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_wangyi_palyerwangyi_player_WangyiPlayer_native_1prepare(JNIEnv *env, jobject instance,
                                                                 jstring dataSource_) {
    const char *dataSource = env->GetStringUTFChars(dataSource_, 0);
    javaCallHelper = new JavaCallHelper(javaVM, env, instance);
    wangYiFFmpeg = new WangYiFFmpeg(javaCallHelper, dataSource);
    wangYiFFmpeg->setRenderCallback(renderFrame);
    wangYiFFmpeg->prepare();
    env->ReleaseStringUTFChars(dataSource_, dataSource);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wangyi_palyerwangyi_player_WangyiPlayer_native_1start(JNIEnv *env, jobject instance) {
//    正是进入播放状态
    if (wangYiFFmpeg) {
        wangYiFFmpeg->start();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_wangyi_palyerwangyi_player_WangyiPlayer_native_1set_1surface(JNIEnv *env, jobject instance,
                                                                      jobject surface) {

    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
//创建新的窗口用于视频显示
    window = ANativeWindow_fromSurface(env, surface);

}