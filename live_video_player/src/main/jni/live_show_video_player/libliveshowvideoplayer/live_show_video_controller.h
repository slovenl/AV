#ifndef LIVE_SHOW_VIDEO_CONTROLLER_H
#define LIVE_SHOW_VIDEO_CONTROLLER_H
#include "../../video_player/libvideoplayer/drag/video_player_drag_controller.h"
#include "live_show_av_synchronizer.h"


class LiveShowVideoController: public VideoPlayerController {
public:
	LiveShowVideoController();
	virtual ~LiveShowVideoController();

protected:
	virtual bool initAVSynchronizer();


private:
	jobject getArrayListObj(JNIEnv * env,
				vector<float>& data, jclass list_class, jmethodID list_construct,
				jmethodID list_add, jclass double_class, jmethodID double_init);

public:
	void setRTMPCurl(char* rtmp_curl);

	static const char* getBuriedPoints();
	void buriedStaticsDataCallback();
	//重写基类destory方法
	void destroy();
};

#endif 	// LIVE_SHOW_VIDEO_CONTROLLER_H
