#include "live_show_ffmpeg_video_decoder.h"

#define LOG_TAG "LiveShowFFMPEGVideoDecoder"

LiveShowFFMPEGVideoDecoder::LiveShowFFMPEGVideoDecoder() {
	rtmp_tcurl = NULL;
}

LiveShowFFMPEGVideoDecoder::LiveShowFFMPEGVideoDecoder(JavaVM *g_jvm, jobject obj)
		: FFMPEGVideoDecoder(g_jvm, obj) {
	rtmp_tcurl = NULL;
}

LiveShowFFMPEGVideoDecoder::~LiveShowFFMPEGVideoDecoder() {
	if (NULL != rtmp_tcurl) {
		delete rtmp_tcurl;
		rtmp_tcurl = NULL;
	}
}

void LiveShowFFMPEGVideoDecoder::setRTMPCurl(char* rtmp_tcurl) {
	if (NULL != rtmp_tcurl) {
		int length = strlen(rtmp_tcurl);
		this->rtmp_tcurl = new char[length];
		memset(this->rtmp_tcurl, 0, length + 1);
		memcpy(this->rtmp_tcurl, rtmp_tcurl, length);
	}
}

void LiveShowFFMPEGVideoDecoder::initFFMpegContext() {
	avformat_network_init();
	//注册所有支持的文件格式以及编解码器 之后就可以用所有ffmpeg支持的codec了
	avcodec_register_all();
	av_register_all();
	connectionRetry = 1;
}

int LiveShowFFMPEGVideoDecoder::openFile(DecoderRequestHeader *requestHeader){
	int ret = FFMPEGVideoDecoder::openFile(requestHeader);
	if(ret >= 0){
		//在网络的播放器中有可能会拉到长宽都为0 并且pix_fmt是None的流 这个时候我们需要重连
		int videoWidth = getVideoFrameWidth();
		int videoHeight = getVideoFrameHeight();
		int retryTimes = 5;
		while(((videoWidth <= 0 || videoHeight <= 0) && retryTimes > 0)){
			LOGI("because of videoWidth and videoHeight is Zero We will Retry...");
			usleep(500 * 1000);
			connectionRetry = 1;
			ret = FFMPEGVideoDecoder::openFile(requestHeader);
			if(ret < 0){
				break;
			}
			retryTimes--;
			videoWidth = getVideoFrameWidth();
			videoHeight = getVideoFrameHeight();
		}
	}
	return ret;
}

int LiveShowFFMPEGVideoDecoder::openFormatInput(char *videoSourceURI) {
	AVDictionary *options = NULL;
	if (NULL != rtmp_tcurl) {
		LOGI("ffmpeg decoder openFormat input rtmp_tcurl is %s", rtmp_tcurl);
		av_dict_set(&options, "rtmp_tcurl", rtmp_tcurl, 0);
	}
	//打开一个文件 只是读文件头，并不会填充流信息 需要注意的是，此处的pFormatContext必须为NULL或由avformat_alloc_context分配得到
	return avformat_open_input(&pFormatCtx, videoSourceURI, NULL, &options);
}

/**
 * 对于网络流 avformt_find_stream_info 函数默认需要花费较长的时间进行流格式探测
 * 可以通过设置AVFotmatContext的probesize和max_analyze_duration属性进行调节：
 */
int LiveShowFFMPEGVideoDecoder::initAnalyzeDurationAndProbesize(int* max_analyze_durations, int analyzeDurationSize, int probesize, bool fpsProbeSizeConfigured) {
	//这里注意了 比较坑人
	//	如果第一行的最大解析长度设置50000的话也有可能会出现解析不出视频帧的情况
	//	如果保留第二行代码就是设置探头尺寸为2048的话可能导致有一些流的codec探测不出来 最终经过测试下边这个还不错，比较折中
//		pFormatCtx->max_analyze_duration = 50000;
//		pFormatCtx->probesize = 2048;
//		pFormatCtx->max_analyze_duration = (0.5 + (double)pow(2.0, (double)connectionRetry) * 0.25) * AV_TIME_BASE;
//		pFormatCtx->probesize = 50 * 1024;
	int max_analyze_duration = -1;
	if (connectionRetry < analyzeDurationSize) {
		max_analyze_duration = max_analyze_durations[connectionRetry];
	}
	if (-1 == max_analyze_duration) {
		max_analyze_duration = AV_TIME_BASE;
	}
	pFormatCtx->max_analyze_duration = max_analyze_duration;
	pFormatCtx->probesize = probesize;
	if(fpsProbeSizeConfigured){
		pFormatCtx->fps_probe_size = 3;
	}
}

bool LiveShowFFMPEGVideoDecoder::isNeedRetry() {
	this->connectionRetry++;
	return VideoDecoder::isNeedRetry() && (this->connectionRetry <= NET_WORK_STREAM_RETRY_TIME);
}

void LiveShowFFMPEGVideoDecoder::flushVideoFrames(AVPacket packet, int* decodeVideoErrorState) {
	isVideoOutputEOF = true;
}
