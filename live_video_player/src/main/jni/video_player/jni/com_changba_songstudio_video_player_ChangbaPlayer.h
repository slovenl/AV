/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include "../libvideoplayer/drag/video_player_drag_controller.h"
/* Header for class com_changba_songstudio_video_player_ChangbaPlayer */

#ifndef _Included_com_changba_songstudio_video_player_ChangbaPlayer
#define _Included_com_changba_songstudio_video_player_ChangbaPlayer
#ifdef __cplusplus
extern "C" {
#endif

/* for test */
JNIEXPORT jint JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_getdemuxedbuffer(JNIEnv *,jobject, jbyteArray);

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_onSurfaceCreated(JNIEnv * env, jobject obj, jobject surface);

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_onSurfaceDestroyed(JNIEnv * env, jobject obj, jobject surface);
/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    prepare
 * Signature: (Ljava/lang/String;[IIIFFII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare
  (JNIEnv *, jobject, jstring, jintArray, jint, jint, jboolean, jfloat, jfloat, jint, jint, jobject);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    pause
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_pause
  (JNIEnv *, jobject);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    play
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_play
  (JNIEnv *, jobject);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_stop
  (JNIEnv *, jobject);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    getBufferedProgress
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_getBufferedProgress
  (JNIEnv *, jobject);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    getPlayProgress
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_getPlayProgress
  (JNIEnv *, jobject);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    seekToPosition
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_seekToPosition
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_changba_songstudio_video_player_ChangbaPlayer
 * Method:    resetRenderSize
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_resetRenderSize
  (JNIEnv *, jobject, jint, jint, jint, jint);

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_seekCurrent
  (JNIEnv *, jobject, jfloat);

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_beforeSeekCurrent
  (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_afterSeekCurrent
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
