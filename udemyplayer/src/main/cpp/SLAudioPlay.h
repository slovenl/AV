


//
// Created by Administrator on 2018-03-05.
//

#ifndef XPLAY_SLAUDIOPLAY_H
#define XPLAY_SLAUDIOPLAY_H


#include "IAudioPlay.h"

class SLAudioPlay: public IAudioPlay
{
public:
    virtual bool StartPlay(XParameter out);
    virtual void Close();
    void PlayCall(void *bufq);

    SLAudioPlay();
    virtual ~SLAudioPlay();
protected:
    unsigned char *buf = 0;
    std::mutex mux;
};


#endif //XPLAY_SLAUDIOPLAY_H
