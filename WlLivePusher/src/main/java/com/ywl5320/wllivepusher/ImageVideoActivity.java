package com.ywl5320.wllivepusher;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;
import com.ywl5320.wllivepusher.encodec.WlMediaEncodec;
import com.ywl5320.wllivepusher.imgvideo.WlImgVideoView;

public class ImageVideoActivity extends AppCompatActivity{

    private WlImgVideoView wlImgVideoView;
    private WlMediaEncodec wlMediaEncodec;
    private WlMusic wlMusic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagevideo);
        wlImgVideoView = findViewById(R.id.imgvideoview);
//        wlImgVideoView.setCurrentImg(R.drawable.img_1);

        wlMusic = WlMusic.getInstance();
        wlMusic.setCallBackPcmData(true);

        wlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                wlMusic.playCutAudio(0, 60);
            }
        });

        wlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                wlMediaEncodec = new WlMediaEncodec(ImageVideoActivity.this, wlImgVideoView.getFbotextureid());
                wlMediaEncodec.initEncodec(wlImgVideoView.getEglContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_image_video.mp4",
                        720, 500, samplerate, channels);
                wlMediaEncodec.startRecord();
                startImgs();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if(wlMediaEncodec != null)
                {
                    wlMediaEncodec.putPCMData(pcmdata, size);
                }
            }
        });

    }

    public void start(View view) {

        wlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/the girl.m4a");
        wlMusic.prePared();

    }

    private void startImgs()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 1; i <= 257; i++)
                {
                    int imgsrc = getResources().getIdentifier("img_" + i, "drawable", "com.ywl5320.wllivepusher");
                    wlImgVideoView.setCurrentImg(imgsrc);
                    try {
                        Thread.sleep(80);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(wlMediaEncodec != null)
                {
                    wlMusic.stop();
                    wlMediaEncodec.stopRecord();
                    wlMediaEncodec = null;
                }
            }
        }).start();
    }

}
