#include "av_drag_synchronizer.h"

#define LOG_TAG "AVDragSynchronizer"

AVDragSynchronizer::AVDragSynchronizer(void *context, onSignalFrameAvailableCallback signalFrameAvailableCallback) {
	afterSeek = false;
	isSeeking = false;

	pthread_mutex_init(&mSeekMutex, NULL);
	mPendingSeekRequest.clear();

	mContext = context;
	mSignalFrameAvailableCallback = signalFrameAvailableCallback;
}

void AVDragSynchronizer::frameAvailable(){
	if (isSeeking) {
		mSignalFrameAvailableCallback(mContext);
	}
}

void AVDragSynchronizer::signalDecodeThread() {
	AVSynchronizer::signalDecodeThread();
	isSeeking = false;	// driven by play
}

void AVDragSynchronizer::decodeFrames(){
	bool good = true;
	float duration = decoder->isNetwork() ? 0.0f : 0.1f;
	while (good) {
		good = false;
		if (canDecode()) {
			if (isSeeking) {	// 如果开始seek，立刻停止解码
				clearAudioFrameQueue();
				break;
			}

			processDecodingFrame(good, duration);
		} else {
			break;
		}
	}
}

AVDragSynchronizer::~AVDragSynchronizer() {

}

FrameTexture* AVDragSynchronizer::getSeekRenderTexture() {
	FrameTexture *texture = NULL;

	circleFrameTextureQueue->front(&texture);

	return texture;
}

void* AVDragSynchronizer::startDecoderThread(void* ptr) {
	AVDragSynchronizer* synchronizer = (AVDragSynchronizer *) ptr;

	while (synchronizer->isOnDecoding) {
		if (synchronizer->isSeeking) {
			pthread_mutex_lock(&synchronizer->mSeekMutex);	// must lock here
			int sizeRemain = synchronizer->mPendingSeekRequest.size();
			pthread_mutex_unlock(&synchronizer->mSeekMutex);

			if (sizeRemain > 0) {
				float pos = synchronizer->mPendingSeekRequest.front();
				synchronizer->mPendingSeekRequest.pop_front();
				pthread_mutex_unlock(&synchronizer->mSeekMutex);

				synchronizer->decodeFrameByPosition(pos);

				pthread_mutex_lock(&synchronizer->mSeekMutex);	// must lock here
				sizeRemain = synchronizer->mPendingSeekRequest.size();
				pthread_mutex_unlock(&synchronizer->mSeekMutex);

				if (sizeRemain == 0) {
					if (synchronizer->afterSeek) {
						synchronizer->isSeeking = false;
						synchronizer->afterSeek = false;
					}

					pthread_mutex_lock(&synchronizer->videoDecoderLock);
					pthread_cond_wait(&synchronizer->videoDecoderCondition, &synchronizer->videoDecoderLock);
					pthread_mutex_unlock(&synchronizer->videoDecoderLock);
				}
			}
			else {
//				LOGI("the remain seek request is 0");
			}
		}

		else {
			synchronizer->decode();

			if (synchronizer->isSeeking)
				continue;
		}
	}
}

// this method always called by UI thread, do the least work and don't block UI
void AVDragSynchronizer::seekCurrent(float position) {
	isSeeking = true;

	moviePosition = position;

	pthread_mutex_lock(&mSeekMutex);	// must protect mPendingSeekRequest
	if (mPendingSeekRequest.size() >= SEEK_REQUEST_LIST_MAX_SIZE) {
		mPendingSeekRequest.pop_front();	// remove the older request
	}

	mPendingSeekRequest.push_back(position);
	pthread_mutex_unlock(&mSeekMutex);

	// if decoder thread is waiting, signal it
	pthread_mutex_lock(&videoDecoderLock);
	pthread_cond_signal(&videoDecoderCondition);
	pthread_mutex_unlock(&videoDecoderLock);
}

void AVDragSynchronizer::beforeSeekCurrent() {
	clearCircleFrameTextureQueue();
}

void AVDragSynchronizer::afterSeekCurrent() {
	clearAudioFrameQueue();

	afterSeek = false;	// 不得不加这个状态量，某些烂手机，如果只seek一下，可能isSeeking都来不及反应就被置为false了
}

void AVDragSynchronizer::onSeek(float seek_seconds){
	clearCircleFrameTextureQueue();
}

void AVDragSynchronizer::destroy(){
	AVSynchronizer::destroy();

	isSeeking = false;
	pthread_mutex_lock(&mSeekMutex);
	mPendingSeekRequest.clear();
	pthread_mutex_unlock(&mSeekMutex);
	pthread_mutex_destroy(&mSeekMutex);
}
