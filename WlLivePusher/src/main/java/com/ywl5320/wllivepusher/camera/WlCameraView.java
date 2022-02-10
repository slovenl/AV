package com.ywl5320.wllivepusher.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.ywl5320.wllivepusher.egl.WLEGLSurfaceView;

public class WlCameraView extends WLEGLSurfaceView{

    private WlCameraRender wlCameraRender;
    private WlCamera wlCamera;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int textureId = -1;

    public WlCameraView(Context context) {
        this(context, null);
    }

    public WlCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        wlCameraRender = new WlCameraRender(context);
        wlCamera = new WlCamera(context);
        setRender(wlCameraRender);
        previewAngle(context);
        wlCameraRender.setOnSurfaceCreateListener(new WlCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int tid) {
                wlCamera.initCamera(surfaceTexture, cameraId);
                textureId = tid;
            }
        });
    }

    public void onDestory()
    {
        if(wlCamera != null)
        {
            wlCamera.stopPreview();
        }
    }

    public void previewAngle(Context context)
    {
        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        wlCameraRender.resetMatrix();
        switch (angle)
        {
            case Surface.ROTATION_0:
                Log.d("ywl5320", "0");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(90, 0, 0, 1);
                    wlCameraRender.setAngle(180, 1, 0, 0);
                }
                else
                {
                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                }

                break;
            case Surface.ROTATION_90:
                Log.d("ywl5320", "90");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(180, 0, 0, 1);
                    wlCameraRender.setAngle(180, 0, 1, 0);
                }
                else
                {
                    wlCameraRender.setAngle(90f, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_180:
                Log.d("ywl5320", "180");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(90f, 0.0f, 0f, 1f);
                    wlCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                }
                else
                {
                    wlCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                Log.d("ywl5320", "270");
                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    wlCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                }
                else
                {
                    wlCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextureId()
    {
        return textureId;
    }
}
