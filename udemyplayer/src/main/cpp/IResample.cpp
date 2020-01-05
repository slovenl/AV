


//
// Created by Administrator on 2018-03-05.
//

#include "IResample.h"
#include "XLog.h"

void IResample::Update(XData data)
{

    XData d = this->Resample(data);
    //XLOGE("this->Resample(data) %d",d.pts);
    if(d.size > 0)
    {
        this->Notify(d);
    }
}