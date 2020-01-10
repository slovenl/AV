


//
// Created by Administrator on 2018-03-01.
//

#ifndef XPLAY_FFDEMUX_H
#define XPLAY_FFDEMUX_H


#include "IDemux.h"
#include <mutex>

//指针声明即可，不需要引入ffmpeg的头文件
struct AVFormatContext;

class FFDemux: public IDemux {
public:

    //打开文件，或者流媒体 rmtp http rtsp
    virtual bool Open(const char *url);
    //seek 位置 pos 0.0~1.0
    virtual bool Seek(double pos);
    virtual void Close();

    //获取视频参数
    virtual XParameter GetVPara();

    //获取音频参数
    virtual XParameter GetAPara();

    //读取一帧数据，数据由调用者清理
    virtual XData Read();

    FFDemux();

private:
    //初始化值，无参数构造函数才会生效，有参构造函数无效，c++11的bug？特性？
    AVFormatContext *ic = 0;
    std::mutex mux;
    int audioStream = 1;
    int videoStream = 0;
};


#endif //XPLAY_FFDEMUX_H
