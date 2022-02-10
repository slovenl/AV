package com.ywl5320.nativeopengldemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.ywl5320.opengl.NativeOpengl;
import com.ywl5320.opengl.WlSurfaceView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private WlSurfaceView wlSurfaceView;
    private NativeOpengl nativeOpengl;
    byte[] pixels;

    private List<Integer> imgs = new ArrayList<>();
    private int index = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wlSurfaceView = findViewById(R.id.wlsurfaceview);
        nativeOpengl = new NativeOpengl();
        wlSurfaceView.setNativeOpengl(nativeOpengl);

        imgs.add(R.drawable.mingren);
        imgs.add(R.drawable.img_1);
        imgs.add(R.drawable.img_2);
        imgs.add(R.drawable.img_3);

        wlSurfaceView.setOnSurfaceListener(new WlSurfaceView.OnSurfaceListener() {
            @Override
            public void init() {
                readPliex();
            }
        });
    }

    public void changeFilter(View view) {

        if(nativeOpengl != null)
        {
            nativeOpengl.surfaceChangeFilter();
        }
    }

    public void changeTexture(View view) {
        readPliex();
    }

    private void readPliex()
    {
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                getImgeIds());
        ByteBuffer fcbuffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getWidth() * 4);
        bitmap.copyPixelsToBuffer(fcbuffer);
        fcbuffer.flip();
        pixels = fcbuffer.array();
        nativeOpengl.imgData(bitmap.getWidth(), bitmap.getHeight(), pixels.length, pixels);
    }

    private int getImgeIds()
    {
        index ++;
        if(index >= imgs.size())
        {
            index = 0;
        }
        return imgs.get(index);
    }

}
