package com.wangyi.palyerwangyi;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.wangyi.palyerwangyi.player.WangyiPlayer;

import java.io.File;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener, WangyiPlayer.OnProgressListener {


    WangyiPlayer wangyiPlayer;
    private SeekBar seekBar;
    private int progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        checkPermission();
         wangyiPlayer = new WangyiPlayer();
        wangyiPlayer.setSurfaceView(surfaceView);
        File file = new File(Environment.getExternalStorageDirectory(), "input2.mp4");
        wangyiPlayer.setDataSource(file.getAbsolutePath());
        wangyiPlayer.setOnProgressListener(this);
        wangyiPlayer.setOnPrepareListener(new WangyiPlayer.OnPrepareListener() {
            @Override
            public void onPrepared() {
                wangyiPlayer.start();
                final int duration = wangyiPlayer.getDuration();
                Log.i("slvoen", "onPrepared: " + duration);
                seekBar.post(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setMax(duration);
                        seekBar.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }
    public void checkPermission() {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //如果没有写sd卡权限
                isGranted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            Log.i("cbs","isGranted == "+isGranted);
            if (!isGranted) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
                                .ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        102);
            }
        }

    }



    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
//        isSeeking = true;
    }

    boolean isSeeking = false;
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        wangyiPlayer.seek(progress);
        isSeeking = true;
    }

    public void play(View view) {
        wangyiPlayer.prepare();
    }

    @Override
    public void onProgress(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isSeeking) {
                    isSeeking = false;
                }
                seekBar.setProgress(progress);
            }
        });
    }

    public void stop(View view) {
        wangyiPlayer.stop();
        wangyiPlayer.release();
    }
}
