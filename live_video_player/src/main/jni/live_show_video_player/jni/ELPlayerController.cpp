#include "./com_changba_songstudio_video_player_ELPlayerController.h"

#define LOG_TAG "ChangbaPlayer_JNI_Layer"
typedef struct VideoPlayerHandle{
    LiveShowVideoController* playerController;
    ANativeWindow *surfaceWindow;
    jobject globalObj;
    jobject surfaceObj;

    VideoPlayerHandle(){
        playerController = NULL;
        surfaceWindow = NULL;
        globalObj = 0;
        surfaceObj = 0;
    }

} LiveShowVideoPlayerHandle;
const int PLAYER_HANDLE_NUMBER = 5;

static LiveShowVideoPlayerHandle* playerHandles[PLAYER_HANDLE_NUMBER];
static int mCurrentIndex = -1;
static int findPlayerControllerHandle() {
    LOGI("findPlayerControllerHandle current index is %d", mCurrentIndex);

    if (mCurrentIndex >= (PLAYER_HANDLE_NUMBER-1)){
        mCurrentIndex = -1;
    }
    for (int i = 0; i < PLAYER_HANDLE_NUMBER; i++) {
        int index = (mCurrentIndex + i + 1) % PLAYER_HANDLE_NUMBER;
        if (playerHandles[index] == 0){
            mCurrentIndex = index;
            LOGI("findPlayerControllerHandle  index is %d", index);
            return index;
        }
    }

    LOGI("findPlayerControllerHandle  index is -1");
    return -1;
}


JNIEXPORT jint JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_retry(
        JNIEnv * env, jobject obj, jint index, jstring videoMergeFilePathParam, jintArray max_analyze_duration, jint size, jint probesize, jboolean fpsProbeSizeConfigured, jfloat minBufferedDuration, jfloat maxBufferedDuration) {
    LOGI("Enter mELPlayerController retry... index is %d", index);
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return -1;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if(NULL != playerHandle){
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        int screenWidth = mELPlayerController->getScreenWidth();
        int screenHeight = mELPlayerController->getScreenHeight();
        LOGI("screenWidth is %d screenHeight is %d", screenWidth, screenHeight);
        mELPlayerController->destroy();
        JavaVM *g_jvm = NULL;
        env->GetJavaVM(&g_jvm);
        jobject g_obj = env->NewGlobalRef(obj);
        playerHandle->globalObj = g_obj;
        char* videoMergeFilePath = (char*) env->GetStringUTFChars(videoMergeFilePathParam, NULL);
        jint* max_analyze_duration_params = env->GetIntArrayElements(max_analyze_duration, 0);
        mELPlayerController->init(videoMergeFilePath, g_jvm, g_obj, max_analyze_duration_params, size, probesize, fpsProbeSizeConfigured, minBufferedDuration, maxBufferedDuration);

        if (playerHandle->surfaceObj){
			ANativeWindow *surfaceWindow = ANativeWindow_fromSurface(env,playerHandle->surfaceObj);
			playerHandle->surfaceWindow = surfaceWindow;
			mELPlayerController->onSurfaceCreated(surfaceWindow, screenWidth,screenHeight);
        }

        env->ReleaseIntArrayElements(max_analyze_duration, max_analyze_duration_params, 0);
        env->ReleaseStringUTFChars(videoMergeFilePathParam, videoMergeFilePath);
    }
    LOGI("leave mELPlayerController retry...");
    return index;
}

JNIEXPORT jint JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_prepare(
        JNIEnv * env, jobject obj, jstring videoMergeFilePathParam,jstring rtmpUrl,
        jintArray max_analyze_duration, jint size, jint probesize,
        jboolean fpsProbeSizeConfigured, jfloat minBufferedDuration, jfloat maxBufferedDuration) {
    LOGI("Enter mELPlayerController prepare...");
    int index = findPlayerControllerHandle();
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return -1;
    }
    JavaVM *g_jvm = NULL;
    env->GetJavaVM(&g_jvm);
    jobject g_obj = env->NewGlobalRef(obj);

    char* videoMergeFilePath = (char*) env->GetStringUTFChars(videoMergeFilePathParam, NULL);
    LiveShowVideoPlayerHandle* playerHandle = new LiveShowVideoPlayerHandle();
    LiveShowVideoController* mELPlayerController = new LiveShowVideoController();

    //在init之前调用setRtmpUrl
    char* rtmpUrlChar = (char*) env->GetStringUTFChars(rtmpUrl, NULL);
    mELPlayerController->setRTMPCurl(rtmpUrlChar);

    jint* max_analyze_duration_params = env->GetIntArrayElements(max_analyze_duration, 0);
    mELPlayerController->init(videoMergeFilePath, g_jvm, g_obj, max_analyze_duration_params, size, probesize, fpsProbeSizeConfigured, minBufferedDuration, maxBufferedDuration);

    env->ReleaseIntArrayElements(max_analyze_duration, max_analyze_duration_params, 0);
    env->ReleaseStringUTFChars(videoMergeFilePathParam, videoMergeFilePath);
    env->ReleaseStringUTFChars(rtmpUrl, rtmpUrlChar);

    playerHandle->playerController = mELPlayerController;
    playerHandle->surfaceWindow = 0;
    playerHandle->globalObj = g_obj;
    playerHandles[index] = playerHandle;
    LOGI("leave mELPlayerController prepare...");
    return index;
}


JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_onSurfaceCreated(JNIEnv * env, jobject obj, jint index, jobject surface, jint width, jint height) {
    LOGI("enter Java_com_changba_songstudio_video_player_ELPlayerController_onSurfaceCreated...");
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if (NULL != mELPlayerController && playerHandle->surfaceWindow == 0) {
            playerHandle->surfaceWindow = ANativeWindow_fromSurface(env, surface);
            playerHandle->surfaceObj = env->NewGlobalRef(surface);
            LOGI("enter Called mELPlayerController->onSurfaceCreated...");

            if (playerHandle->surfaceWindow != 0){
                mELPlayerController->onSurfaceCreated(playerHandle->surfaceWindow, width, height);
            }
        }
    }
    LOGI("leave Java_com_changba_songstudio_video_player_ELPlayerController_onSurfaceCreated...");
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_onSurfaceDestroyed(JNIEnv * env, jobject obj, jint index, jobject surface) {
    LOGI("enter Java_com_changba_songstudio_video_player_ELPlayerController_onSurfaceDestroyed...");
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if (NULL != mELPlayerController) {
            mELPlayerController->onSurfaceDestroyed();
        }
        if(playerHandle->surfaceWindow != 0){
            LOGI("ELPlayerController onSurfaceDestroyed : Releasing surfaceWindow");
            playerHandle->surfaceWindow = 0;
        }
        if(playerHandle->surfaceObj != 0){
            LOGI("ELPlayerController onSurfaceDestroyed : Releasing Surface");
            env->DeleteGlobalRef(playerHandle->surfaceObj);
            playerHandle->surfaceObj = 0;
        }
    }
    LOGI("leave Java_com_changba_songstudio_video_player_ELPlayerController_onSurfaceDestroyed...");
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_pause(JNIEnv * env, jobject obj, jint index) {
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            mELPlayerController->pause();
        }
    }
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_play(JNIEnv * env, jobject obj, jint index) {
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            mELPlayerController->play();
        }
    }
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_stop(JNIEnv * env, jobject obj, jint index) {
    LOGI("enter mELPlayerController Stop...");
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER){
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            LOGI("Before mELPlayerController->destroy()...");
            mELPlayerController->destroy();
            LOGI("after mELPlayerController->destroy()...");
            delete mELPlayerController;
            mELPlayerController = NULL;
            LOGI("mELPlayerController Assign To NULL...");
        }
        if(playerHandle->surfaceWindow != 0){
            LOGI("ELPlayerController stop : Releasing surfaceWindow");
            playerHandle->surfaceWindow = 0;
        }

        if (playerHandle->globalObj != 0){
            LOGI("ELPlayerController stop : Releasing Global Ref");
            env->DeleteGlobalRef(playerHandle->globalObj);
            playerHandle->globalObj = 0;
        }
        if(playerHandle->surfaceObj != 0) {
            env->DeleteGlobalRef(playerHandle->surfaceObj);
            playerHandle->surfaceObj = 0;
        }

        playerHandles[index] = 0;
    }
    LOGI("leave mELPlayerController Stop index:%d", index);
}

JNIEXPORT jfloat JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_getBufferedProgress(JNIEnv * env, jobject obj, jint index) {
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER) {
        LOGI("invalid player controller index");
        return 0.0f;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            return mELPlayerController->getBufferedProgress();
        }
    }
    return 0.0f;
}

JNIEXPORT jfloat JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_getPlayProgress(JNIEnv * env, jobject obj, jint index) {
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER) {
        LOGI("invalid player controller index");
        return 0.0f;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if (NULL != mELPlayerController) {
            return mELPlayerController->getPlayProgress();
        }
    }
    return 0.0f;
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_resetRenderSize(JNIEnv * env, jobject obj, jint index, jint left, jint top, jint width, jint height) {
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER) {
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            mELPlayerController->resetRenderSize(left, top, width, height);
        }
    }
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_seekToPosition(JNIEnv * env, jobject obj, jint index, jfloat position) {
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER) {
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            mELPlayerController->seekToPosition(position);
        }
    }
}

JNIEXPORT void JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_setRTMPCurl(JNIEnv * env, jobject obj, jint index, jstring url){
    if (index < 0 || index >= PLAYER_HANDLE_NUMBER) {
        LOGI("invalid player controller index");
        return;
    }
    LiveShowVideoPlayerHandle* playerHandle = playerHandles[index];
    if (NULL != playerHandle) {
        LiveShowVideoController* mELPlayerController = playerHandle->playerController;
        if(NULL != mELPlayerController) {
            if (url != NULL) {
                char* rtmpUrl = (char*) env->GetStringUTFChars(url, NULL);
                mELPlayerController->setRTMPCurl(rtmpUrl);
            }
        }
    }
}

JNIEXPORT jstring JNICALL Java_com_changba_songstudio_video_player_ELPlayerController_getBuriedPoints(JNIEnv * env, jobject obj) {
    const char * buriedPoints = LiveShowVideoController::getBuriedPoints();
    jclass strClass = env->FindClass("java/lang/String");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    int len = strlen(buriedPoints);
    if(len > 0){
        jbyteArray bytes = env->NewByteArray(len);
        env->SetByteArrayRegion(bytes, 0, strlen(buriedPoints), (jbyte*)buriedPoints);
        jstring encoding = env->NewStringUTF("utf-8");
        return (jstring)env->NewObject(strClass, ctorID, bytes, encoding);
    } else{
        return NULL;
    }
}
