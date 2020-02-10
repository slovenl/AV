//
// Created by sloven on 2020/2/10.
//

#ifndef AV_PLAYSTATUS_H
#define AV_PLAYSTATUS_H

enum PlayerEvent {
    IDLE            = 0,
    PREPARED        = 1,
    COMPLETE        = 2,
    BUFFERING_START = 3,
    BUFFERING_END   = 4,
    SEEK_COMPLETE   = 5,
    SET_VIDEO_SIZE  = 6,
    PAUSED          = 7,
    PLAYING         = 8,
    ERROR           = 100,
};
#endif //AV_PLAYSTATUS_H
