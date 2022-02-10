package com.ywl5320.wllivepusher.push;

import android.text.TextUtils;

public class WlPushVideo {


    private WlConnectListenr wlConnectListenr;

    static {
        System.loadLibrary("wlpush");
    }

    public void setWlConnectListenr(WlConnectListenr wlConnectListenr) {
        this.wlConnectListenr = wlConnectListenr;
    }


    private void onConnecting()
    {
        if(wlConnectListenr != null)
        {
            wlConnectListenr.onConnecting();
        }
    }

    private void onConnectSuccess()
    {
        if(wlConnectListenr != null)
        {
            wlConnectListenr.onConnectSuccess();
        }
    }

    private void onConnectFial(String msg)
    {
        if(wlConnectListenr != null)
        {
            wlConnectListenr.onConnectFail(msg);
        }
    }




    public void initLivePush(String url)
    {
        if(!TextUtils.isEmpty(url))
        {
            initPush(url);
        }
    }

    public void pushSPSPPS(byte[] sps, byte[] pps)
    {
        if(sps != null && pps != null)
        {
            pushSPSPPS(sps, sps.length, pps, pps.length);
        }
    }

    public void pushVideoData(byte[] data, boolean keyframe)
    {
        if(data != null)
        {
            pushVideoData(data, data.length, keyframe);
        }
    }

    public void pushAudioData(byte[] data)
    {
        if(data != null)
        {
            pushAudioData(data, data.length);
        }
    }

    public void stopPush()
    {
        pushStop();
    }





    private native void initPush(String pushUrl);

    private native void pushSPSPPS(byte[] sps, int sps_len, byte[] pps, int pps_len);

    private native void pushVideoData(byte[] data, int data_len, boolean keyframe);

    private native void pushAudioData(byte[] data, int data_len);

    private native void pushStop();


}
