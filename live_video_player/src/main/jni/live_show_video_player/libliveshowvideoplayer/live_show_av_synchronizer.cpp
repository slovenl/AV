#include "live_show_av_synchronizer.h"

#define LOG_TAG "LiveShowAVSynchronizer"

extern bool isNeedBuriedPoint;
extern BuriedPoint buriedPoint;
extern long buriedPointStart;

LiveShowAVSynchronizer::LiveShowAVSynchronizer() {
	decoder = NULL;
	rtmp_tcurl = NULL;
	isFirstScreen = true;

//	videoEffectProcessor = NULL;
//	outputTexId = -1;
//	sourceVideoFrame = NULL;
//	targetVideoFrame = NULL;
}

LiveShowAVSynchronizer::~LiveShowAVSynchronizer() {
	if (NULL != rtmp_tcurl) {
		delete rtmp_tcurl;
		rtmp_tcurl = NULL;
	}
}

void LiveShowAVSynchronizer::setRTMPCurl(char* rtmp_tcurl){
	if(NULL != rtmp_tcurl){
		int length = strlen(rtmp_tcurl);
		this->rtmp_tcurl = new char[length];
		memset(this->rtmp_tcurl, 0, length + 1);
		memcpy(this->rtmp_tcurl, rtmp_tcurl, length);
	}
}

void LiveShowAVSynchronizer::initMeta() {
	if (decoder->isNetwork()) {
		//这是服务器返回的minBufferedDuration与maxBufferedDuration，如果不合法的话 在使用我们自己定义的min与max
		if (this->minBufferedDuration <= 0 || this->maxBufferedDuration <= 0 || this->maxBufferedDuration < this->minBufferedDuration){
			this->maxBufferedDuration = NETWORK_MAX_BUFFERED_DURATION;
			this->minBufferedDuration = NETWORK_MIN_BUFFERED_DURATION;
		}
		this->syncMaxTimeDiff = NETWORK_AV_SYNC_MAX_TIME_DIFF;
	} else {
		AVSynchronizer::initMeta();
	}
	mPreviousVideoQueueStatus = QUEUE_STATUS_UNKNOWN;
}

void LiveShowAVSynchronizer::createDecoderInstance() {
	//TODO:目前查看到问题:硬件解码在直播中的首屏时间会变慢, 目前先使用软件解码策略，等后期我们在调整软硬件解码并行的终极方案
//	if (this->isHWCodecAvaliable()){
//		decoder = new LiveShowMediaCodecVideoDecoder(g_jvm, obj);
//		((LiveShowMediaCodecVideoDecoder*)decoder)->setRTMPCurl(rtmp_tcurl);
//	} else {
//		decoder = new LiveShowFFMPEGVideoDecoder(g_jvm, obj);
//		((LiveShowFFMPEGVideoDecoder*)decoder)->setRTMPCurl(rtmp_tcurl);
//	}
	decoder = new LiveShowFFMPEGVideoDecoder(g_jvm, obj);
	((LiveShowFFMPEGVideoDecoder*)decoder)->setRTMPCurl(rtmp_tcurl);
}

FrameTexture* LiveShowAVSynchronizer::getCorrectRenderTexture(bool forceGetFrame) {
	FrameTexture *texture = AVSynchronizer::getCorrectRenderTexture(forceGetFrame);
	if (NULL != texture && isNeedBuriedPoint && isFirstScreen) {
		buriedPoint.firstScreenTimeMills = (getCurrentTime() - buriedPointStart)/1000.0f;
		LOGI("LiveShowAVSynchronizer::getCorrectRenderTexture %f", buriedPoint.firstScreenTimeMills);
		isFirstScreen = false;
	}
	return texture;
}

void LiveShowAVSynchronizer::useForstatistic(int leftVideoFrames) {
	if (isNeedBuriedPoint) {
		VideoQueueStatus videoQueueStatus = QUEUE_STATUS_UNKNOWN;

		if (leftVideoFrames <= 1)
			videoQueueStatus = QUEUE_STATUS_EMPTY;
		else if (leftVideoFrames < maxBufferedDuration * getVideoFPS()) {
			videoQueueStatus = QUEUE_STATUS_NORMAL;
		} else
			videoQueueStatus = QUEUE_STATUS_FULL;

		if (mPreviousVideoQueueStatus == QUEUE_STATUS_UNKNOWN)
			mPreviousVideoQueueStatus = videoQueueStatus;
		else {
			if ((videoQueueStatus == QUEUE_STATUS_EMPTY) && (videoQueueStatus != mPreviousVideoQueueStatus)) {
				long curTime = getCurrentTime();
				float emptyTime = (curTime - buriedPointStart) / 1000.0f;

				if (buriedPoint.videoQueueEmpty.size() < 50)
					buriedPoint.videoQueueEmpty.push_back(emptyTime);

				mPreviousVideoQueueStatus = videoQueueStatus;
			}

			if ((videoQueueStatus == QUEUE_STATUS_FULL) && (videoQueueStatus != mPreviousVideoQueueStatus)) {
				LOGI("video queue is full");

				long curTime = getCurrentTime();
				float fullTime = (curTime - buriedPointStart) / 1000.0f;

				if (buriedPoint.videoQueueFull.size() < 50)
					buriedPoint.videoQueueFull.push_back(fullTime);

				mPreviousVideoQueueStatus = videoQueueStatus;
			}
		}
	}
}

void LiveShowAVSynchronizer::processVideoFrame(GLuint inputTexId, int width, int height, float position){
//	LOGI("LiveShowAVSynchronizer::processVideoFrame()");
	GLuint texId = inputTexId;
//	if(videoEffectProcessor){
//		texId = outputTexId;
//		sourceVideoFrame->setTextureId(inputTexId);
//		videoEffectProcessor->process(sourceVideoFrame, position, targetVideoFrame);
//	}
	AVSynchronizer::processVideoFrame(texId, width, height, position);
}

void LiveShowAVSynchronizer::OnInitFromUploaderGLContext(EGLCore* eglCore, int videoFrameWidth, int videoFrameHeight) {
	LOGI("LiveShowAVSynchronizer::OnInitFromUploaderGLContext");
//	videoEffectProcessor = new VideoEffectProcessor();
//	videoEffectProcessor->init();
//	int filterId = videoEffectProcessor->addFilter(EFFECT_PROCESSOR_VIDEO_TRACK_INDEX, 0, 1000000 * 10 * 60 * 60, PLAYER_CONTRAST_FILTER_NAME);
//	if(filterId >= 0){
//		videoEffectProcessor->invokeFilterOnReady(0, filterId);
//	}
//
//	ImagePosition imagePosition(0, 0, GLsizei(videoFrameWidth), GLsizei(videoFrameHeight));
//	//初始化sourceVideoFrame
//	sourceVideoFrame = new OpenglVideoFrame(-1, imagePosition);
//	//初始化outputTexId与targetVideoFrame
//	glGenTextures(1, &outputTexId);
//	glBindTexture(GL_TEXTURE_2D, outputTexId);
//	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, imagePosition.width, imagePosition.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
//	glBindTexture(GL_TEXTURE_2D, 0);
//	targetVideoFrame = new OpenglVideoFrame(outputTexId, imagePosition);
	AVSynchronizer::OnInitFromUploaderGLContext(eglCore, videoFrameWidth, videoFrameHeight);
}

void LiveShowAVSynchronizer::onDestroyFromUploaderGLContext() {
	LOGI("LiveShowAVSynchronizer::onDestroyFromUploaderGLContext()");
//	if (NULL != videoEffectProcessor) {
//		videoEffectProcessor->dealloc();
//		delete videoEffectProcessor;
//		videoEffectProcessor = NULL;
//	}
//	if (-1 != outputTexId) {
//		glDeleteTextures(1, &outputTexId);
//	}
//	if (NULL != targetVideoFrame) {
//		delete targetVideoFrame;
//		targetVideoFrame = NULL;
//	}
//	if (NULL != sourceVideoFrame) {
//		delete sourceVideoFrame;
//		sourceVideoFrame = NULL;
//	}

	AVSynchronizer::onDestroyFromUploaderGLContext();
}
