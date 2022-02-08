#ifndef LIVE_SHOW_FFMPEG_VIDEO_DECODER_H
#define LIVE_SHOW_FFMPEG_VIDEO_DECODER_H

#include "../../video_player/libvideoplayer/decoder/ffmpeg_video_decoder.h"

#define NET_WORK_STREAM_RETRY_TIME 3

class LiveShowFFMPEGVideoDecoder : public FFMPEGVideoDecoder {
private:
	char* rtmp_tcurl;

public:
	LiveShowFFMPEGVideoDecoder();
	LiveShowFFMPEGVideoDecoder(JavaVM *g_jvm, jobject obj);
	virtual ~LiveShowFFMPEGVideoDecoder();

	virtual int openFile(DecoderRequestHeader *requestHeader);

	void setRTMPCurl(char* rtmp_curl);
	virtual bool isNetwork(){
		return true;
	};
protected:
	virtual void initFFMpegContext();
	virtual int openFormatInput(char *videoSourceURI);
	virtual bool isNeedRetry();
	virtual int initAnalyzeDurationAndProbesize(int* max_analyze_durations, int analyzeDurationSize, int probesize, bool fpsProbeSizeConfigured);

	virtual void flushVideoFrames(AVPacket packet, int* decodeVideoErrorState);
};

#endif // LIVE_SHOW_FFMPEG_VIDEO_DECODER_H

