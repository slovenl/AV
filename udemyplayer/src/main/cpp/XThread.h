


//
// Created by Administrator on 2018-03-01.
//

#ifndef XPLAY_XTHREAD_H
#define XPLAY_XTHREAD_H

//sleep 毫秒
void XSleep(int mis);

//c++ 11 线程库
class XThread
{
public:
    //启动线程
    virtual bool Start();

    //通过控制isExit安全停止线程（不一定成功）
    virtual void Stop();

    virtual void SetPause(bool isP);

    virtual bool IsPause()
    {
        isPausing = isPause;
        return isPause;
    }

    //入口主函数
    virtual void Main() {}

protected:
    bool isExit = false;
    bool isRuning = false;
    bool isPause = false;
    bool isPausing = false;
private:
    void ThreadMain();

};


#endif //XPLAY_XTHREAD_H
