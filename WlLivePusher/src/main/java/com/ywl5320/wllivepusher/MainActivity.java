package com.ywl5320.wllivepusher;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void cameraPreview(View view) {

        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);

    }

    public void vodeorecord(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    public void imgvideo(View view) {
        Intent intent = new Intent(this, ImageVideoActivity.class);
        startActivity(intent);
    }

    public void yuvplayer(View view) {
        Intent intent = new Intent(this, YuvActivity.class);
        startActivity(intent);
    }

    public void livepush(View view) {

        Intent intent = new Intent(this, LivePushActivity.class);
        startActivity(intent);

    }
}
