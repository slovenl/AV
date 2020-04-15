package videochat.ju.com.videochat;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by user on 2017/10/19.
 */

public class CameraPreviewGlSurface extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener,ICameraPreview{
    private static final int MSG_START_PREVIEW=0;
    private static final int MSG_BEGIN_ENCODE=0;
    private Handler.Callback mCameraCaptureCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return true;
        }
    };
    private Handler.Callback mEncodeCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(mEncoder!=null && mCaptureStart){
                Buffer buffer = new Buffer();
                mEncoder.encode(buffer);
                if(buffer.data!=null){
                    /*byte[] temp = new byte[buffer.length];
                    buffer.data.get(temp);
                    try {
                        mFos.write(temp,0,buffer.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    if(mCameraEncodeCallback!= null){
                        mCameraEncodeCallback.onCameraFrameEncode(buffer.data,0,buffer.length);
                    }
                }
            }
            if(mBeginEncode){
                mEncodeHandler.sendEmptyMessageDelayed(MSG_BEGIN_ENCODE,1);
            }
            return true;
        }
    };


    private int mTextureID;
    private SurfaceTexture mSurfaceTexture;
    private OpenGLTextureDrawer mOpenGLTextureDrawer;
    private InputSurface mInputSurface;

    private EGLDisplay mSavedEglDisplay;
    private EGLSurface mSavedEglDrawSurface;
    private EGLSurface mSavedEglReadSurface;
    private EGLContext mSavedEglContext;
    private Camera mCamera;
    private VideoEncoder mEncoder;

    private HandlerThread mCamreaCaptureThread;
    private final Handler mCameraCaptureHandler;
    private HandlerThread mEncodeThread;
    private Handler mEncodeHandler;
    private boolean mBeginEncode=false;
    private ICameraEncodeCallback mCameraEncodeCallback;
    private boolean mCaptureStart=false;
    private int mCameraWidth;
    private int mCameraHeight;
    private int mViewWidth;
    private int mViewHeight;
    public CameraPreviewGlSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        mCamreaCaptureThread = new HandlerThread("CameraCapture");
        mCamreaCaptureThread.start();
        mCameraCaptureHandler = new Handler(mCamreaCaptureThread.getLooper(),mCameraCaptureCallback);

        mEncodeThread = new HandlerThread("encode thread");
        mEncodeThread.start();
        mEncodeHandler= new Handler(mEncodeThread.getLooper(),mEncodeCallback);

        setZOrderOnTop(true);

    }
    private void glSetup() {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        // Set the background color.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // Disable depth testing -- we're 2D only.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureID = createTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(CameraPreviewGlSurface.this);
        mOpenGLTextureDrawer = new OpenGLTextureDrawer(mTextureID);

        mCamera = CameraPreview.getCameraInstance();
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size size = params.getSupportedPreviewSizes().get(0);
        mCameraWidth = size.width;
        mCameraHeight = size.height;
        params.setPreviewFpsRange(30000,30000);
        params.setPreviewSize(mCameraWidth, mCameraHeight);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        mCamera.setParameters(params); // setting camera parameters
        mEncoder = new VideoEncoder();
        Surface surface=null;
        try {
            surface = mEncoder.initForSuface(mCameraWidth, mCameraHeight,30);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mInputSurface = new InputSurface(surface);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mCaptureStart = true;
        glSetup();
        saveRenderState();
        mInputSurface.makeCurrent();
        glSetup();
        restoreRenderState();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        Log.d("ttttt","onGLSurfaceChanged width="+width+" height="+height);
    }
    private int beginEncodeFrameCount=0;
    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
        mSurfaceTexture.updateTexImage();
        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);
        mOpenGLTextureDrawer.draw(mtx);

        if(mBeginEncode && mInputSurface!= null){
            saveRenderState();
            mInputSurface.makeCurrent();
            GLES20.glViewport(0, 0, mCameraWidth, mCameraHeight);
            mOpenGLTextureDrawer.draw(mtx);
            mInputSurface.swapBuffers();
            restoreRenderState();
            Log.d("ttttt","begin encode frame No="+beginEncodeFrameCount);
            beginEncodeFrameCount++;

        }
        long time3=System.currentTimeMillis();
    }
    private void saveRenderState() {
            mSavedEglDisplay = EGL14.eglGetCurrentDisplay();
            mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
            mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
            mSavedEglContext = EGL14.eglGetCurrentContext();
    }

    private void restoreRenderState() {
        // switch back to previous state
        if (!EGL14.eglMakeCurrent(mSavedEglDisplay, mSavedEglDrawSurface, mSavedEglReadSurface, mSavedEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }
    private int captureFrameCount;
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        long time1=System.currentTimeMillis();
        this.requestRender();
        Log.d("ttttt","capture frame No="+captureFrameCount+" time="+(System.currentTimeMillis() - time1));
        captureFrameCount++;
    }
    private int createTextureID()
    {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }


    @Override
    public void setEncodeCallback(ICameraEncodeCallback callback) {
        mCameraEncodeCallback= callback;
    }

    @Override
    public void startPreview() {
        mCameraCaptureHandler.removeMessages(MSG_START_PREVIEW);
        mCameraCaptureHandler.sendEmptyMessage(MSG_START_PREVIEW);
    }

    @Override
    public void beginEncode() {
        mBeginEncode = true;
        mEncodeHandler.removeMessages(MSG_BEGIN_ENCODE);
        mEncodeHandler.sendEmptyMessage(MSG_BEGIN_ENCODE);
    }

    public void testBitrate(){
        mEncoder.testBitrate();
    }
}
