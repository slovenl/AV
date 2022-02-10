package com.ywl5320.wllivepusher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ywl5320.wllivepusher.yuv.WlYuvView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class YuvActivity extends AppCompatActivity{

    private WlYuvView wlYuvView;

    private FileInputStream fis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuv);
        wlYuvView = findViewById(R.id.yuvview);
    }

    public void start(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int w = 640;
                    int h = 360;
                    fis = new FileInputStream(new File("/mnt/shared/Other/sintel_640_360.yuv"));
                    byte []y = new byte[w * h];
                    byte []u = new byte[w * h / 4];
                    byte []v = new byte[w * h / 4];

                    while (true)
                    {
                        int ry = fis.read(y);
                        int ru = fis.read(u);
                        int rv = fis.read(v);
                        if(ry > 0 && ru > 0 && rv > 0)
                        {
                            wlYuvView.setFrameData(w, h, y, u, v);
                            Thread.sleep(40);
                        }
                        else
                        {
                            Log.d("ywl5320", "完成");
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
