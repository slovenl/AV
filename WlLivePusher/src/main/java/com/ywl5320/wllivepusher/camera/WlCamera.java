package com.ywl5320.wllivepusher.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.ywl5320.wllivepusher.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class WlCamera {

    private Camera camera;



    private SurfaceTexture surfaceTexture;

    private int width;
    private int height;

    public WlCamera(Context context){

        this.width = DisplayUtil.getScreenWidth(context);
        this.height = DisplayUtil.getScreenHeight(context);

    }

    public void initCamera(SurfaceTexture surfaceTexture, int cameraId)
    {
        this.surfaceTexture = surfaceTexture;
        setCameraParm(cameraId);

    }

    private void setCameraParm(int cameraId)
    {
        try {
            camera = Camera.open(cameraId);
            camera.setPreviewTexture(surfaceTexture);
            Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode("off");
            parameters.setPreviewFormat(ImageFormat.NV21);

            Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width, size.height);

            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width, size.height);

            camera.setParameters(parameters);
            camera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview()
    {
        if(camera != null)
        {
            camera.startPreview();
            camera.release();
            camera = null;
        }
    }

    public void changeCamera(int cameraId)
    {
        if(camera != null)
        {
            stopPreview();
        }
        setCameraParm(cameraId);
    }

    private Camera.Size getFitSize(List<Camera.Size> sizes)
    {
        if(width < height)
        {
            int t = height;
            height = width;
            width = t;
        }

        for(Camera.Size size : sizes)
        {
            if(1.0f * size.width / size.height == 1.0f * width / height)
            {
                return size;
            }
        }
        return sizes.get(0);
    }

}
