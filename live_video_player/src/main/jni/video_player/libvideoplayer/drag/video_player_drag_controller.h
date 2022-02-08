#ifndef DRAG_VIDEO_PLAYER_CONTROLLER_H
#define DRAG_VIDEO_PLAYER_CONTROLLER_H

#include "../video_player_controller.h"

/**
 * Video Player Drag Controller
 */
class VideoPlayerDragController : public VideoPlayerController{
public:
	int getCorrectRenderTexture(FrameTexture** frameTex, bool forceGetFrame);
	bool initAVSynchronizer();

	/** 拖动seekbar的同时对video进行seek，只做seek，不包括后面的play等操作，实际上，seekToPosition可以由该操作实现 **/
	void seekCurrent(float position);

	void beforeSeekCurrent();
	void afterSeekCurrent();
};

static void pfnSignalOutputFrameAvailableCallback(void *context) {
	VideoPlayerController* controller = (VideoPlayerController*) context;
	controller->signalOutputFrameAvailable();
}

#endif //DRAG_VIDEO_PLAYER_CONTROLLER_H
