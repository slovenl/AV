package com.ywl5320.wllivepusher.yuv;

import android.content.Context;
import android.util.AttributeSet;

import com.ywl5320.wllivepusher.egl.WLEGLSurfaceView;

public class WlYuvView extends WLEGLSurfaceView{

    private WlYuvRender wlYuvRender;

    public WlYuvView(Context context) {
        this(context, null);
    }

    public WlYuvView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlYuvView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wlYuvRender = new WlYuvRender(context);
        setRender(wlYuvRender);
        setRenderMode(WLEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setFrameData(int w, int h, byte[] by, byte[] bu, byte[] bv)
    {
        if(wlYuvRender != null)
        {
            wlYuvRender.setFrameData(w, h, by, bu, bv);
            requestRender();
        }
    }



}
