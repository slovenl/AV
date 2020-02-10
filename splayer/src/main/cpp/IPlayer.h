//
// Created by sloven on 2020/2/10.
//
#ifndef AV_IPLAYER_H
#define AV_IPLAYER_H

class IPlayer {
public:
    virtual void setDataSource(char *url) = 0;

    virtual void prepare() = 0;

    virtual void start() = 0;

    virtual void pause() = 0;

    virtual void seek(long pos) = 0;

    virtual void stop() = 0;

    virtual void release() = 0;

private:

};

#endif //AV_IPLAYER_H
