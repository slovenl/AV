


//
// Created by Administrator on 2018-03-04.
//

#include "GLVideoView.h"
#include "XTexture.h"
#include "XLog.h"
void GLVideoView::SetRender(void *win)
{
    view = win;
}
void GLVideoView::Close()
{
        mux.lock();
        if(txt)
        {
            txt->Drop();
            txt = 0;
        }

    mux.unlock();
}
void GLVideoView::Render(XData data)
{

    if(!view) return;
    if(!txt)
    {
        txt = XTexture::Create();

        txt->Init(view,(XTextureType)data.format);
    }
    txt->Draw(data.datas,data.width,data.height);
}