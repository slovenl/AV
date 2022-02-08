#ifndef LIVE_SHOW_AV_SYNCHRONIZER_H
#define LIVE_SHOW_AV_SYNCHRONIZER_H
#include "../../video_player/libvideoplayer/sync/av_synchronizer.h"
#include "live_show_ffmpeg_video_decoder.h"
#include "live_show_mediacodec_video_decoder.h"
//#include "video_effect_processor.h"
#include <string>

#define NETWORK_FIRST_BUFFERED_DURATION 			0.1
#define NETWORK_MIN_BUFFERED_DURATION 			2.0
#define NETWORK_MAX_BUFFERED_DURATION 			4.0
#define NETWORK_AV_SYNC_MAX_TIME_DIFF         	0.3

class LiveShowAVSynchronizer: public AVSynchronizer {
private:
	char* rtmp_tcurl;
	bool isFirstScreen;
public:
	LiveShowAVSynchronizer();
	virtual ~LiveShowAVSynchronizer();

	void setRTMPCurl(char* rtmp_tcurl);

	virtual FrameTexture* getCorrectRenderTexture(bool forceGetFrame);

	virtual void onDestroyFromUploaderGLContext();
	virtual void OnInitFromUploaderGLContext(EGLCore* eglCore, int videoFrameWidth, int videoFrameHeight);
	virtual void processVideoFrame(GLuint inputTexId, int width, int height, float position);
protected:
	/** 覆盖父类的实例化decoder与初始化decoder方法，添加上自己的音效行为 **/
	virtual void createDecoderInstance();
	virtual void initMeta();

	virtual void useForstatistic(int leftVideoFrames);

	enum VideoQueueStatus {
	    QUEUE_STATUS_UNKNOWN = -1,  ///< Usually treated as AVMEDIA_TYPE_DATA
		QUEUE_STATUS_EMPTY,
		QUEUE_STATUS_NORMAL,
		QUEUE_STATUS_FULL
	};

	VideoQueueStatus 				mPreviousVideoQueueStatus;

private:
	// maintain model
//	VideoEffectProcessor* 	videoEffectProcessor;
//	GLuint 					outputTexId;
//	OpenglVideoFrame* 		sourceVideoFrame;
//	OpenglVideoFrame* 		targetVideoFrame;
};

#endif // LIVE_SHOW_AV_SYNCHRONIZER_H
