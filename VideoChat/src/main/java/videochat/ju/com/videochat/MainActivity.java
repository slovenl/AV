package videochat.ju.com.videochat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ICameraPreview mCamreaPreview;
    private RemotePreview mRemotePreview;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCamreaPreview = (ICameraPreview) this.findViewById(R.id.camera_preview);
        mRemotePreview = (RemotePreview) this.findViewById(R.id.remote_preview);
        this.findViewById(R.id.bt_test).setOnClickListener(this);
        VideoChatManager.getInstance().init(mCamreaPreview,mRemotePreview);
    }

    @Override
    public void onClick(View v) {
    }
}
