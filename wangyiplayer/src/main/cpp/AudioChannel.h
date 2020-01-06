//
// Created by Administrator on 2019/6/4.
//

#ifndef PALYERWANGYI_AUDIOCHANNEL_H
#define PALYERWANGYI_AUDIOCHANNEL_H

#include <SLES/OpenSLES_Android.h>
#include "BaseChannel.h"
extern "C" {
#include <libswresample/swresample.h>
}
class AudioChannel :public BaseChannel {

public:
    AudioChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,AVRational time_base);

    ~AudioChannel();
    virtual void play();

    virtual void stop();

    void initOpenSL();

    void decode();
    int getPcm();
private:
    pthread_t pid_audio_play;
    pthread_t pid_audio_decode;
    SwrContext *swr_ctx = NULL;
    int out_channels;
    int out_samplesize;
    int out_sample_rate;
public:
    uint8_t *buffer;
};


#endif //PALYERWANGYI_AUDIOCHANNEL_H
