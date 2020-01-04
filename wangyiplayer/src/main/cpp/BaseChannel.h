//
// Created by Administrator on 2019/6/4.
//

#ifndef PALYERWANGYI_BASECHANNEL_H
#define PALYERWANGYI_BASECHANNEL_H
extern "C"{
#include <libavcodec/avcodec.h>
};

#include "safe_queue.h"
#include "JavaCallHelper.h"

class BaseChannel {

public:
    BaseChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext, AVRational time_base
                 ) : channelId(id),javaCallHelper(javaCallHelper),
                                   avCodecContext(avCodecContext),time_base(time_base)
    {

        pkt_queue.setReleaseHandle(releaseAvPacket);
        frame_queue.setReleaseHandle(releaseAvFrame);
    };
    static void releaseAvPacket(AVPacket *&packet) {
        if (packet) {
            av_packet_free(&packet);
            packet = 0;
        }
    }

    static void releaseAvFrame(AVFrame *&frame) {
        if (frame) {
            av_frame_free(&frame);
            frame = 0;
        }
    }

    virtual ~BaseChannel() {
        if (avCodecContext) {
            avcodec_close(avCodecContext);
            avcodec_free_context(&avCodecContext);
            avCodecContext = 0;
        }
        pkt_queue.clear();
        frame_queue.clear();
        LOGE("释放channel:%d %d", pkt_queue.size(), frame_queue.size());
    };

    virtual void play()=0;
    virtual void stop()=0;
    SafeQueue<AVPacket *> pkt_queue;
    SafeQueue<AVFrame *> frame_queue;
    volatile int channelId;
    volatile bool isPlaying  ;
    AVCodecContext *avCodecContext;
    JavaCallHelper *javaCallHelper;
    AVRational time_base;
    double clock = 0;

};
#endif //PALYERWANGYI_BASECHANNEL_H
