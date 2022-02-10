package com.ywl5320.wllivepusher;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ywl5320.wllivepusher.camera.WlCameraView;

public class CameraActivity extends AppCompatActivity{

    private WlCameraView wlCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        wlCameraView = findViewById(R.id.cameraview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wlCameraView.onDestory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        wlCameraView.previewAngle(this);
    }
}
