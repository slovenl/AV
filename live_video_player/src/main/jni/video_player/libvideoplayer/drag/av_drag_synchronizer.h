#ifndef VIDEO_PLAYER_AV_SEEK_SYNCHRONIZER_
#define VIDEO_PLAYER_AV_SEEK_SYNCHRONIZER_


#include "../sync/av_synchronizer.h"

using namespace std;

class AVDragSynchronizer : public AVSynchronizer{
public:
	AVDragSynchronizer(void *context, onSignalFrameAvailableCallback signalFrameAvailableCallback);
	virtual ~AVDragSynchronizer();

	void onSeek(float seek_seconds);

	bool getIsSeeking() {
		return isSeeking;
	}

	void frameAvailable();

	void seekCurrent(float position);
	void beforeSeekCurrent();
	void afterSeekCurrent();
	FrameTexture* getSeekRenderTexture();

	void decodeFrames();
	void signalDecodeThread();
	static void* startDecoderThread(void* ptr);

	void destroy();

private:
	void*	mContext;
	onSignalFrameAvailableCallback mSignalFrameAvailableCallback;

	// seekCurrent related
	bool 							isSeeking;	// all boolean read and write is atomic, needn't lock
	bool 							afterSeek;

	pthread_mutex_t 				mSeekMutex;
	list<float>						mPendingSeekRequest;
};
#endif // VIDEO_PLAYER_AV_SYNCHRONIZER_
