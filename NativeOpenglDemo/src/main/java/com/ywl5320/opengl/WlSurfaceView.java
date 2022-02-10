package com.ywl5320.opengl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WlSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private NativeOpengl nativeOpengl;
    private OnSurfaceListener onSurfaceListener;

    public void setOnSurfaceListener(OnSurfaceListener onSurfaceListener) {
        this.onSurfaceListener = onSurfaceListener;
    }

    public void setNativeOpengl(NativeOpengl nativeOpengl) {
        this.nativeOpengl = nativeOpengl;
    }

    public WlSurfaceView(Context context) {
        this(context, null);
    }

    public WlSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WlSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if(nativeOpengl != null)
        {
            nativeOpengl.surfaceCreate(surfaceHolder.getSurface());
            if(onSurfaceListener != null)
            {
                onSurfaceListener.init();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if(nativeOpengl != null)
        {
            nativeOpengl.surfaceChange(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(nativeOpengl != null)
        {
            nativeOpengl.surfaceDestroy();
        }
    }

    public interface OnSurfaceListener
    {
        void init();

    }

}
