//
// Created by yangw on 2018-9-14.
//

#include "WlQueue.h"

WlQueue::WlQueue() {
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);
}

WlQueue::~WlQueue() {
    clearQueue();
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);

}

int WlQueue::putRtmpPacket(RTMPPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

RTMPPacket *WlQueue::getRtmpPacket() {
    pthread_mutex_lock(&mutexPacket);

    RTMPPacket *p = NULL;
    if(!queuePacket.empty())
    {
        p = queuePacket.front();
        queuePacket.pop();
    } else{
        pthread_cond_wait(&condPacket, &mutexPacket);
    }
    pthread_mutex_unlock(&mutexPacket);
    return p;
}

void WlQueue::clearQueue() {

    pthread_mutex_lock(&mutexPacket);
    while(true)
    {
        if(queuePacket.empty())
        {
            break;
        }
        RTMPPacket *p = queuePacket.front();
        queuePacket.pop();
        RTMPPacket_Free(p);
        p = NULL;
    }
    pthread_mutex_unlock(&mutexPacket);

}

void WlQueue::notifyQueue() {

    pthread_mutex_lock(&mutexPacket);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

}
