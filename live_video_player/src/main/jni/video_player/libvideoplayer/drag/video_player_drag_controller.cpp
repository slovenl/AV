#include "video_player_drag_controller.h"

#define LOG_TAG "VideoPlayerDragController"

bool VideoPlayerDragController::initAVSynchronizer(){
	synchronizer = new AVDragSynchronizer(this, pfnSignalOutputFrameAvailableCallback);
	return synchronizer->init(requestHeader, g_jvm, obj, minBufferedDuration, maxBufferedDuration);
}

int VideoPlayerDragController::getCorrectRenderTexture(FrameTexture** frameTex, bool forceGetFrame){
	int ret = -1;
	bool isSeeking = false;

	AVDragSynchronizer *seekSynchronizer = (AVDragSynchronizer *) (synchronizer);
	if (seekSynchronizer){
		isSeeking = seekSynchronizer->getIsSeeking();
	}

	if (isSeeking) {
		(*frameTex) = synchronizer->getSeekRenderTexture();
		ret = 0;
	} else {
		ret = VideoPlayerController::getCorrectRenderTexture(frameTex, forceGetFrame);
	}
	return ret;
}

void VideoPlayerDragController::seekCurrent(float position) {
	AVDragSynchronizer *seekSynchronizer = (AVDragSynchronizer *) (synchronizer);

	if (seekSynchronizer) {
		seekSynchronizer->seekCurrent(position);
	}
}

void VideoPlayerDragController::beforeSeekCurrent() {
	pause();	// pause first

	AVDragSynchronizer *seekSynchronizer = (AVDragSynchronizer *) (synchronizer);
	if (seekSynchronizer) {
		seekSynchronizer->beforeSeekCurrent();
	}
}

void VideoPlayerDragController::afterSeekCurrent() {
	AVDragSynchronizer *seekSynchronizer = (AVDragSynchronizer *) (synchronizer);
	if (seekSynchronizer) {
		seekSynchronizer->afterSeekCurrent();
	}
}

