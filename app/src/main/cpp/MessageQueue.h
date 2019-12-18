//
// Created by sloven
// 参考ijkplayer中消息队列
//

#ifndef AV_MESSAGEQUEUE_H
#define AV_MESSAGEQUEUE_H

#include <pthread.h>
#include <assert.h>
#include <string.h>

// #define FFP_SHOW_MSG_RECYCLE
extern "C" {

typedef struct AVMessage {
    int what;
    int arg1;
    int arg2;
    void *obj;

    void (*free_l)(void *obj);

    struct AVMessage *next;
} AVMessage;

typedef struct MessageQueue {
    AVMessage *first_msg, *last_msg;
    int nb_messages;
    int abort_request;
    pthread_mutex_t *mutex;
    pthread_cond_t *cond;

    AVMessage *recycle_msg;
    int recycle_count;
    int alloc_count;
} MessageQueue;

void msg_free_res(AVMessage *msg) {
    if (!msg || !msg->obj)
        return;
    assert(msg->free_l);
    msg->free_l(msg->obj);
    msg->obj = NULL;
}

int msg_queue_put_private(MessageQueue *q, AVMessage *msg) {
    AVMessage *msg1;

    if (q->abort_request)
        return -1;

#ifdef FFP_MERGE
    msg1 = av_malloc(sizeof(AVMessage));
#else
    msg1 = q->recycle_msg;
    if (msg1) {
        q->recycle_msg = msg1->next;
        q->recycle_count++;
    } else {
        q->alloc_count++;
        msg1 = malloc(sizeof(AVMessage));
    }
#ifdef FFP_SHOW_MSG_RECYCLE
    int total_count = q->recycle_count + q->alloc_count;
    if (!(total_count % 10)) {
        av_log(NULL, AV_LOG_DEBUG, "msg-recycle \t%d + \t%d = \t%d\n", q->recycle_count, q->alloc_count, total_count);
    }
#endif
#endif
    if (!msg1)
        return -1;

    *msg1 = *msg;
    msg1->next = NULL;

    if (!q->last_msg)
        q->first_msg = msg1;
    else
        q->last_msg->next = msg1;
    q->last_msg = msg1;
    q->nb_messages++;
    pthread_cond_signal(q->cond);
    return 0;
}


int msg_queue_put(MessageQueue *q, AVMessage *msg) {
    int ret;

    pthread_mutex_lock(q->mutex);
    ret = msg_queue_put_private(q, msg);
    pthread_mutex_unlock(q->mutex);

    return ret;
}

void msg_init_msg(AVMessage *msg) {
    memset(msg, 0, sizeof(AVMessage));
}

void msg_queue_put_simple1(MessageQueue *q, int what) {
    AVMessage msg;
    msg_init_msg(&msg);
    msg.what = what;
    msg_queue_put(q, &msg);
}

void msg_queue_put_simple2(MessageQueue *q, int what, int arg1) {
    AVMessage msg;
    msg_init_msg(&msg);
    msg.what = what;
    msg.arg1 = arg1;
    msg_queue_put(q, &msg);
}

void msg_queue_put_simple3(MessageQueue *q, int what, int arg1, int arg2) {
    AVMessage msg;
    msg_init_msg(&msg);
    msg.what = what;
    msg.arg1 = arg1;
    msg.arg2 = arg2;
    msg_queue_put(q, &msg);
}

void msg_obj_free_l(void *obj) {
    av_free(obj);
}

void
msg_queue_put_simple4(MessageQueue *q, int what, int arg1, int arg2, void *obj, int obj_len) {
    AVMessage msg;
    msg_init_msg(&msg);
    msg.what = what;
    msg.arg1 = arg1;
    msg.arg2 = arg2;
    msg.obj = av_malloc(obj_len);
    memcpy(msg.obj, obj, obj_len);
    msg.free_l = msg_obj_free_l;
    msg_queue_put(q, &msg);
}

void msg_queue_init(MessageQueue *q) {
    memset(q, 0, sizeof(MessageQueue));
    q->mutex = SDL_CreateMutex();
    q->cond = SDL_CreateCond();
    q->abort_request = 1;
}

void msg_queue_flush(MessageQueue *q) {
    AVMessage *msg, *msg1;

    SDL_LockMutex(q->mutex);
    for (msg = q->first_msg; msg != NULL; msg = msg1) {
        msg1 = msg->next;
#ifdef FFP_MERGE
        av_freep(&msg);
#else
        msg->next = q->recycle_msg;
        q->recycle_msg = msg;
#endif
    }
    q->last_msg = NULL;
    q->first_msg = NULL;
    q->nb_messages = 0;
    SDL_UnlockMutex(q->mutex);
}

void msg_queue_destroy(MessageQueue *q) {
    msg_queue_flush(q);

    pthread_mutex_lock(q->mutex);
    while (q->recycle_msg) {
        AVMessage *msg = q->recycle_msg;
        if (msg)
            q->recycle_msg = msg->next;
        msg_free_res(msg);
        av_freep(&msg);
    }
    pthread_mutex_unlock(q->mutex);

    pthread_mutex_destroy(q->mutex);
    pthread_cond_destroy(q->cond);
}

void msg_queue_abort(MessageQueue *q) {
    pthread_mutex_lock(q->mutex);

    q->abort_request = 1;

    pthread_cond_signal(q->cond);

    pthread_mutex_unlock(q->mutex);
}

void msg_queue_start(MessageQueue *q) {
    pthread_mutex_lock(q->mutex);
    q->abort_request = 0;

    AVMessage msg;
    msg_init_msg(&msg);
    msg.what = FFP_MSG_FLUSH;
    msg_queue_put_private(q, &msg);
    pthread_mutex_unlock(q->mutex);
}

/* return < 0 if aborted, 0 if no msg and > 0 if msg.  */
int msg_queue_get(MessageQueue *q, AVMessage *msg, int block) {
    AVMessage *msg1;
    int ret;

    pthread_mutex_lock(q->mutex);

    for (;;) {
        if (q->abort_request) {
            ret = -1;
            break;
        }

        msg1 = q->first_msg;
        if (msg1) {
            q->first_msg = msg1->next;
            if (!q->first_msg)
                q->last_msg = NULL;
            q->nb_messages--;
            *msg = *msg1;
            msg1->obj = NULL;
#ifdef FFP_MERGE
            av_free(msg1);
#else
            msg1->next = q->recycle_msg;
            q->recycle_msg = msg1;
#endif
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
            pthread_cond_wait(q->cond, q->mutex);
        }
    }
    pthread_mutex_unlock(q->mutex);
    return ret;
}

void msg_queue_remove(MessageQueue *q, int what) {
    AVMessage **p_msg, *msg, *last_msg;
    pthread_mutex_lock(q->mutex);

    last_msg = q->first_msg;

    if (!q->abort_request && q->first_msg) {
        p_msg = &q->first_msg;
        while (*p_msg) {
            msg = *p_msg;

            if (msg->what == what) {
                *p_msg = msg->next;
#ifdef FFP_MERGE
                av_free(msg);
#else
                msg_free_res(msg);
                msg->next = q->recycle_msg;
                q->recycle_msg = msg;
#endif
                q->nb_messages--;
            } else {
                last_msg = msg;
                p_msg = &msg->next;
            }
        }

        if (q->first_msg) {
            q->last_msg = last_msg;
        } else {
            q->last_msg = NULL;
        }
    }

    pthread_mutex_unlock(q->mutex);
}
}
#endif


#endif //AV_MESSAGEQUEUE_H
