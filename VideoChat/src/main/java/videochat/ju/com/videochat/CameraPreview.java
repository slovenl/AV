package videochat.ju.com.videochat;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;


/**
 * Created by mabin1 on 2017/10/13.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback,
       ICameraPreview {
    private static final int MSG_START_PREVIEW=0;
    private static final int MSG_GET_CAMREA_FRAME=0;

    private static final String TAG ="CameraPreview" ;
    private boolean mBeginEncode=false;
    private VideoEncoder mVideoEncoder;
    private HandlerThread mEncodeThread;
    private Handler mEncodeHandler;
    private Handler.Callback mEncodeCallback=new Handler.Callback() {
        int frameCount=0;
        long yuvtime=0;
        long encodetime=0;
        @Override
        public boolean handleMessage(Message msg) {
            Object[] temp= (Object[]) msg.obj;
            Camera camrea= (Camera) temp[0];
            byte[] data = (byte[]) temp[1];
            Log.d("ttttt","encode frame No="+frameCount);
            long time1= System.currentTimeMillis();
            //Yuv.NV21ToYUV420sp(data,1920,1080);
            //Yuv.nativeNV21ToYUV420sp(data);
            long time2= System.currentTimeMillis();
            //saveYuv2File(data);
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            Buffer buffer = new Buffer();
            mVideoEncoder.encode(byteBuffer,frameCount*30,false,buffer);
            long time3= System.currentTimeMillis();
            if(buffer.data!=null){
                if(mCameraEncodeCallback != null){
                    mCameraEncodeCallback.onCameraFrameEncode(buffer.data,0,buffer.length);
                }
            }
            frameCount++;
            yuvtime+=time2-time1;
            encodetime += time3-time2;

            if(frameCount%10==0){
                Log.d("ttttt","yuvtime="+yuvtime+" encodetime="+encodetime);
            }
            camrea.addCallbackBuffer(data);
            return false;
        }
        private void saveYuv2File(byte[] data) {File output = new File("/tmp/dump/"+frameCount+".yuv");
            FileOutputStream fps=null;
            try {
                fps = new FileOutputStream(output);
                fps.write(data);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(fps!=null){
                    try {
                        fps.flush();
                        fps.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private Handler.Callback mCameraCaptureCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_START_PREVIEW:
                    //这句一定要在looper线程中执行，确保framedata回调在looper线程
                    mCamera = getCameraInstance();
                    Camera.Parameters params = mCamera.getParameters();
                    Camera.Size size = params.getSupportedPreviewSizes().get(0);
                    int width = size.width;
                    int height = size.height;
                    params.setPreviewFpsRange(30000,30000);
                    params.setPreviewSize(width, height);
                    params.setPreviewFormat(ImageFormat.NV21);
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    mCamera.setParameters(params); // setting camera parameters
                    mCamera.setPreviewCallbackWithBuffer(CameraPreview.this);
                    mCamera.addCallbackBuffer(new byte[width*height*3/2]);
                    mCamera.addCallbackBuffer(new byte[width*height*3/2]);
                    mCamera.addCallbackBuffer(new byte[width*height*3/2]);
                    try {
                        mCamera.setPreviewDisplay(mHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mCamera.startPreview();
                    mVideoEncoder = new VideoEncoder();
                    try {
                        mVideoEncoder.init(width,height, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar,30);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
            }
            return false;
        }
    };

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private HandlerThread mCamreaCaptureThread;
    private final Handler mCameraCaptureHandler;
    private int captureFrameNo=0;
    private ICameraEncodeCallback mCameraEncodeCallback;
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mCamreaCaptureThread = new HandlerThread("CameraCapture");
        mCamreaCaptureThread.start();
        mCameraCaptureHandler = new Handler(mCamreaCaptureThread.getLooper(),mCameraCaptureCallback);

        mEncodeThread = new HandlerThread("encode thread");
        mEncodeThread.start();
        mEncodeHandler= new Handler(mEncodeThread.getLooper(),mEncodeCallback);
        this.setZOrderOnTop(true);
    }



    @Override
    public void setEncodeCallback(ICameraEncodeCallback callback) {
        mCameraEncodeCallback = callback;
    }
    @Override
    public void startPreview(){
        mCameraCaptureHandler.removeMessages(MSG_START_PREVIEW);
        mCameraCaptureHandler.sendEmptyMessage(MSG_START_PREVIEW);
    }
    @Override
    public void beginEncode() {
        mBeginEncode = true;
    }
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamreaCaptureThread.getLooper().quit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }
        if(mCamera==null){
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }
        try {
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }



    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d("ttttt","onPreviewFrame "+data.length);
        if(!mBeginEncode){
            camera.addCallbackBuffer(data);
            return;
        }
        Log.d("ttttt","capture frame No="+captureFrameNo);
        captureFrameNo++;
        mEncodeHandler.sendMessage(mEncodeHandler.obtainMessage(MSG_GET_CAMREA_FRAME,new Object[]{camera,data}));
    }


    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("ttttt","",e);
        }
        return c; // returns null if camera is unavailable
    }

}