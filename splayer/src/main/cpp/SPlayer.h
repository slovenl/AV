//
// Created by sloven on 2020/2/10.
//

#ifndef AV_SPLAYER_H
#define AV_SPLAYER_H

#include "IPlayer.h"

class SPlayer : public IPlayer {
public:
    void setDataSource(char *url);

    void prepare();

    void start();

    void pause();

    void seek(long pos);

    void stop();

    void release();

private:

};

#endif //AV_SPLAYER_H
