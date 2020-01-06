//
// Created by Administrator on 2019/6/4.
//

#include "WangYiFFmpeg.h"
#include "JavaCallHelper.h"
#include "macro.h"

void *prepareFFmpeg_(void *args) {
    WangYiFFmpeg *wangYiFFmpeg = static_cast<WangYiFFmpeg *>(args);
    wangYiFFmpeg->prepareFFmpeg();
    return 0;
}
WangYiFFmpeg::WangYiFFmpeg(JavaCallHelper *javaCallHelper, const char *dataSource) {
    url = new char[strlen(dataSource) + 1];
    this->javaCallHelper = javaCallHelper;
    strcpy(url, dataSource);
    duration = 0;
    pthread_mutex_init(&mutex, NULL);
}

WangYiFFmpeg::~WangYiFFmpeg() {
    pthread_mutex_destroy(&mutex);
    javaCallHelper = 0;
    DELETE(url);
}

void WangYiFFmpeg::prepare() {
//
    pthread_create(&pid_prepare, NULL, prepareFFmpeg_, this);
}

void WangYiFFmpeg::prepareFFmpeg() {
    /*子线程
     * 访问到对象的属性*/
    avformat_network_init();
    // 代表一个 视频/音频 包含了视频、音频的各种信息
    formatContext = avformat_alloc_context();
    //1、打开URL
    AVDictionary *opts = NULL;
    //设置超时3秒(单位us)
    av_dict_set(&opts, "timeout", "3000000", 0);
    //强制指定AVFormatContext中AVInputFormat的。这个参数一般情况下可以设置为NULL，这样FFmpeg可以自动检测AVInputFormat。
    //输入文件的封装格式
//    av_find_input_format("avi")  rtmp
    int ret = avformat_open_input(&formatContext, url, NULL, &opts);
    if (ret != 0) {
        javaCallHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_OPEN_URL);
        return;
    }
    //2.查找流
    if (avformat_find_stream_info(formatContext, NULL) < 0) {
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_FIND_STREAMS);
        }
        return;
    }
    duration = formatContext->duration / 1000000;
    for (int i = 0; i < formatContext->nb_streams; ++i) {
        //获取解码器参数信息
        AVCodecParameters *codecpar = formatContext->streams[i]->codecpar;
        //获取视频流信息
        AVStream *stream=formatContext->streams[i];

        //找到解码器
        AVCodec *dec = avcodec_find_decoder(codecpar->codec_id);
        if (!dec) {
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_FIND_DECODER_FAIL);
            }
            return;
        }
        //创建上下文，context3是版本升级的结果
        AVCodecContext *codecContext = avcodec_alloc_context3(dec);
        if (!codecContext) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_ALLOC_CODEC_CONTEXT_FAIL);
            return;
        }
        //复制参数到解码上下文
        if (avcodec_parameters_to_context(codecContext, codecpar) < 0) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL);
            return;
        }
        //打开解码器，为什么这里判断！=0，ffmpeg中返回int类型的时候0都代表success，非0为错误码
        if (avcodec_open2(codecContext, dec, 0) != 0) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_OPEN_DECODER_FAIL);
            return;
        }
        //音频
        if (codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
//音频
            audioChannel = new AudioChannel(i, javaCallHelper, codecContext,stream->time_base);

        } else if (codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            AVRational frame_rate=  stream->avg_frame_rate;
//视频
//            int fps = frame_rate.num / (double)frame_rate.den;
            int fps =av_q2d(frame_rate);
//            1/fps
            videoChannel = new VideoChannel(i, javaCallHelper, codecContext,stream->time_base);
            videoChannel->setRenderCallback(renderFrame);
            videoChannel->setFps(fps);

        }
    }

    //音视频都没有
    if (!audioChannel && !videoChannel) {
        if (javaCallHelper)
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_NOMEDIA);
        return;
    }
    videoChannel->audioChannel = audioChannel;
    if (javaCallHelper)
        javaCallHelper->onParpare(THREAD_CHILD);

}
void *startThread(void *args) {

    WangYiFFmpeg *ffmpeg = static_cast<WangYiFFmpeg *>(args);
    ffmpeg->play();
    return 0;
}

void WangYiFFmpeg::start() {
    isPlaying = true;
    if (audioChannel) {
        audioChannel->play();
    }
    if (videoChannel) {
        videoChannel->play();
    }
    pthread_create(&pid_play, NULL, startThread, this);
}

void WangYiFFmpeg::play() {
    int ret = 0;
    while (isPlaying) {
        //限制队列长度，防止内存爆掉
//        100帧,
        if (audioChannel && audioChannel->pkt_queue.size() > 100) {
//            思想    队列    生产者的生产速度 远远大于消费者的速度  10ms   100packet     12ms  10ms
            av_usleep(1000 * 10);
            continue;
        }

        if (videoChannel && videoChannel->pkt_queue.size() > 100) {
            av_usleep(1000 * 10);
            continue;
        }

        //读取包
        AVPacket *packet = av_packet_alloc();
        // 从媒体中读取音频、视频包
        ret = av_read_frame(formatContext, packet);

//         是你读文件packet   看录播课程   FFmpeg  代码  5s  奔溃       1000帧   渲染  延时
        if (ret == 0) {
            //将数据包加入队列
            if (audioChannel && packet->stream_index == audioChannel->channelId) {
                audioChannel->pkt_queue.enQueue(packet);
            } else if (videoChannel && packet->stream_index == videoChannel->channelId) {
                videoChannel->pkt_queue.enQueue(packet);
            }
        }else if (ret == AVERROR_EOF) {
            //读取完毕 但是不一定播放完毕
            if (videoChannel->pkt_queue.empty() && videoChannel->frame_queue.empty() &&
                audioChannel->pkt_queue.empty() && audioChannel->frame_queue.empty()) {
                LOGE("播放完毕。。。");
                break;
            }
            //因为seek 的存在，就算读取完毕，依然要循环 去执行av_read_frame(否则seek了没用...)
        } else {
            break;
        }
    }

    isPlaying = 0;
    audioChannel->stop();
    videoChannel->stop();

}

void WangYiFFmpeg::setRenderCallback(RenderFrame renderFrame) {
    this->renderFrame = renderFrame;
}

int WangYiFFmpeg::getDuration() {
    return duration;
}

void WangYiFFmpeg::seek(int position) {
    if (position < 0 || position > duration) {
        return;
    }

    if (!formatContext) {
        return;
    }

    pthread_mutex_lock(&mutex);
    av_seek_frame(formatContext, -1,position * 1000000, AVSEEK_FLAG_BACKWARD);

    if (audioChannel) {
        audioChannel->stopWork();
        audioChannel->clear();
        audioChannel->startWork();
    }

    if (videoChannel) {
        videoChannel->stopWork();
        videoChannel->clear();
        videoChannel->startWork();
    }
    pthread_mutex_unlock(&mutex);
}

void *async_stop(void *args) {

    WangYiFFmpeg *fFmpeg = static_cast<WangYiFFmpeg *>(args);
    pthread_join(fFmpeg->pid_prepare, NULL);
    fFmpeg->isPlaying = 0;
    pthread_join(fFmpeg->pid_play, NULL);
    if (fFmpeg->audioChannel) {
        fFmpeg->audioChannel->stopWork();
        fFmpeg->audioChannel->clear();
        fFmpeg->audioChannel->stop();
    }

    if (fFmpeg->videoChannel) {
        fFmpeg->videoChannel->stopWork();
        fFmpeg->videoChannel->clear();
        fFmpeg->videoChannel->stop();
    }
    DELETE(fFmpeg->audioChannel);
    DELETE(fFmpeg->videoChannel);
    if (fFmpeg->formatContext) {
        avformat_close_input(&fFmpeg->formatContext);
        avformat_free_context(fFmpeg->formatContext);
        fFmpeg->formatContext = NULL;
    }
    DELETE(fFmpeg);
    return 0;
}

void WangYiFFmpeg::stop() {
    javaCallHelper = 0;
    if (audioChannel) {
        audioChannel->javaCallHelper = 0;
    }
    isPlaying = 0;
    if (videoChannel) {
        videoChannel->javaCallHelper = 0;
    }
    pthread_create(&pid_stop, NULL, async_stop, this);
}





