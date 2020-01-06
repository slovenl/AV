


//
// Created by Administrator on 2018-03-04.
//

#ifndef XPLAY_XEGL_H
#define XPLAY_XEGL_H


class XEGL
{
public:
    virtual bool Init(void *win) = 0;
    virtual void Close() = 0;
    virtual void Draw() = 0;
    static XEGL *Get();

protected:
    XEGL(){}
};


#endif //XPLAY_XEGL_H
