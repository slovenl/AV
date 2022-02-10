package com.ywl5320.wllivepusher.push;

import android.content.Context;

import com.ywl5320.wllivepusher.encodec.WlBaseMediaEncoder;

public class WlPushEncodec extends WlBasePushEncoder{

    private WlEncodecPushRender wlEncodecPushRender;

    public WlPushEncodec(Context context, int textureId) {
        super(context);
        wlEncodecPushRender = new WlEncodecPushRender(context, textureId);
        setRender(wlEncodecPushRender);
        setmRenderMode(WlBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
