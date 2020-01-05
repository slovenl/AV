


//
// Created by Administrator on 2018-03-04.
//

#include "IVideoView.h"
#include "XLog.h"

void IVideoView::Update(XData data)
{
    //("IVideoView->Update(data) %d",data.pts);
    this->Render(data);
}