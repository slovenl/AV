package com.ywl5320.wllivepusher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ywl5320.wllivepusher.camera.WlCameraView;
import com.ywl5320.wllivepusher.push.WlBasePushEncoder;
import com.ywl5320.wllivepusher.push.WlConnectListenr;
import com.ywl5320.wllivepusher.push.WlPushEncodec;
import com.ywl5320.wllivepusher.push.WlPushVideo;

public class LivePushActivity extends AppCompatActivity{

    private WlPushVideo wlPushVideo;
    private WlCameraView wlCameraView;
    private boolean start = false;
    private WlPushEncodec wlPushEncodec;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livepush);
        wlCameraView = findViewById(R.id.cameraview);
        wlPushVideo = new WlPushVideo();
        wlPushVideo.setWlConnectListenr(new WlConnectListenr() {
            @Override
            public void onConnecting() {
                Log.d("ywl5320", "链接服务器中..");
            }

            @Override
            public void onConnectSuccess() {
                Log.d("ywl5320", "链接服务器成功，可以开始推流了");
                wlPushEncodec = new WlPushEncodec(LivePushActivity.this, wlCameraView.getTextureId());
                wlPushEncodec.initEncodec(wlCameraView.getEglContext(), 720 / 2, 1280 / 2);
                wlPushEncodec.startRecord();
                wlPushEncodec.setOnMediaInfoListener(new WlBasePushEncoder.OnMediaInfoListener() {
                    @Override
                    public void onMediaTime(int times) {

                    }

                    @Override
                    public void onSPSPPSInfo(byte[] sps, byte[] pps) {
                        wlPushVideo.pushSPSPPS(sps, pps);
                    }

                    @Override
                    public void onVideoInfo(byte[] data, boolean keyframe) {
                        wlPushVideo.pushVideoData(data, keyframe);
                    }

                    @Override
                    public void onAudioInfo(byte[] data) {
                        wlPushVideo.pushAudioData(data);
                    }
                });
            }

            @Override
            public void onConnectFail(String msg) {
                Log.d("ywl5320", msg);
            }
        });
    }

    public void startpush(View view) {
        start = !start;
        if(start)
        {
            wlPushVideo.initLivePush("rtmp://119.27.185.134/live/mystream");
        }
        else
        {
            if(wlPushEncodec != null)
            {
                wlPushEncodec.stopRecord();
                wlPushVideo.stopPush();
                wlPushEncodec = null;
            }
        }
    }
}
