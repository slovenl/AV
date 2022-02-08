#include "./com_changba_songstudio_video_player_ChangbaPlayer.h"

#define LOG_TAG "ChangbaPlayer_JNI_Layer"

VideoPlayerDragController* videoPlayerController = NULL;

static ANativeWindow *window = 0;

JNIEXPORT jboolean JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare(
		JNIEnv * env, jobject obj, jstring videoMergeFilePathParam, jintArray max_analyze_duration, jint size, jint probesize, jboolean fpsProbeSizeConfigured, jfloat minBufferedDuration, jfloat maxBufferedDuration,
		jint width, jint height, jobject surface) {
	LOGI("Enter Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare...");
	JavaVM *g_jvm = NULL;
	env->GetJavaVM(&g_jvm);
	jobject g_obj = env->NewGlobalRef(obj);
	char* videoMergeFilePath = (char*) env->GetStringUTFChars(videoMergeFilePathParam, NULL);
	if(NULL == videoPlayerController) {
		videoPlayerController = new VideoPlayerDragController();
	}
	LOGI("Enter Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare 2...");
	window = ANativeWindow_fromSurface(env, surface);
	jint* max_analyze_duration_params = env->GetIntArrayElements(max_analyze_duration, 0);
	LOGI("Enter Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare 3...");
	jboolean initCode = videoPlayerController->init(videoMergeFilePath, g_jvm, g_obj, max_analyze_duration_params,
			size, probesize, fpsProbeSizeConfigured, minBufferedDuration, maxBufferedDuration);
	LOGI("Enter Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare 4...");
	videoPlayerController->onSurfaceCreated(window, width, height);
	LOGI("Enter Java_com_changba_songstudio_video_player_ChangbaPlayer_prepare 5...");
	env->ReleaseIntArrayElements(max_analyze_duration, max_analyze_duration_params, 0);
	env->ReleaseStringUTFChars(videoMergeFilePathParam, videoMergeFilePath);

	return initCode;
}


JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_onSurfaceCreated(JNIEnv * env, jobject obj, jobject surface, jint width, jint height) {
	if (NULL != videoPlayerController) {
		window = ANativeWindow_fromSurface(env, surface);
		videoPlayerController->onSurfaceCreated(window, width, height);
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_onSurfaceDestroyed(JNIEnv * env, jobject obj, jobject surface) {
	if (NULL != videoPlayerController) {
		videoPlayerController->onSurfaceDestroyed();
		if(window != 0){
			LOGI("Releasing surfaceWindow");
			ANativeWindow_release(window);
			window = 0;
		}
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_pause(JNIEnv * env, jobject obj) {
	if(NULL != videoPlayerController) {
		videoPlayerController->pause();
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_play(JNIEnv * env, jobject obj) {
	if(NULL != videoPlayerController) {
		videoPlayerController->play();
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_stop(JNIEnv * env, jobject obj) {
	if(NULL != videoPlayerController) {
		videoPlayerController->destroy();
		delete videoPlayerController;
		videoPlayerController = NULL;
	}
	if(window != 0){
		LOGI("Releasing window");
		ANativeWindow_release(window);
		window = 0;
	}
}

JNIEXPORT jfloat JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_getBufferedProgress(JNIEnv * env, jobject obj) {
	if (NULL != videoPlayerController) {
		return videoPlayerController->getBufferedProgress();
	}
	return 0.0f;
}

JNIEXPORT jfloat JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_getPlayProgress(JNIEnv * env, jobject obj) {
	if (NULL != videoPlayerController) {
		return videoPlayerController->getPlayProgress();
	}
	return 0.0f;
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_resetRenderSize(JNIEnv * env, jobject obj, jint left, jint top, jint width, jint height) {
	if(NULL != videoPlayerController) {
		videoPlayerController->resetRenderSize(left, top, width, height);
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_seekToPosition(JNIEnv * env, jobject obj, jfloat position) {
	if(NULL != videoPlayerController) {
		videoPlayerController->seekToPosition(position);
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_seekCurrent(JNIEnv * env, jobject obj, jfloat position) {
	if(NULL != videoPlayerController) {
		videoPlayerController->seekCurrent(position);
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_beforeSeekCurrent(JNIEnv * env, jobject obj) {
	if(NULL != videoPlayerController) {
		videoPlayerController->beforeSeekCurrent();
	}
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ChangbaPlayer_afterSeekCurrent(JNIEnv * env, jobject obj) {
	if(NULL != videoPlayerController) {
		videoPlayerController->afterSeekCurrent();
	}
}



