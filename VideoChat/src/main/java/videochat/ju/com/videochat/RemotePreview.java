package videochat.ju.com.videochat;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by user on 2017/10/17.
 */

public class RemotePreview extends SurfaceView implements SurfaceHolder.Callback {

    private HandlerThread mDecodeThread;
    private Handler mDecodeHandler;

    public interface DecoderCallback{
        void onDecoderReady();
    }
    private Handler.Callback mDecoderHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mVideoDecoder.checkoutOutPut();
            mDecodeHandler.sendEmptyMessageDelayed(0,1);
            return true;
        }
    };
    private int mDecoderWidth;
    private int mDecoderHeight;
    private VideoDecoder mVideoDecoder;
    private DecoderCallback mDecoderCallback;
    public RemotePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVideoDecoder=new VideoDecoder();
        getHolder().addCallback(this);


    }
    public void setDecoderParam(int width,int height){
        mDecoderWidth = width;
        mDecoderHeight = height;
    }
    public void setDecoderCallback(DecoderCallback decoderCallback) {
        mDecoderCallback = decoderCallback;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mVideoDecoder.init(mDecoderWidth,mDecoderHeight, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible,holder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(mDecoderCallback!=null){
            mDecoderCallback.onDecoderReady();
        }
        mDecodeThread = new HandlerThread("DecodeThread");
        mDecodeThread.start();
        mDecodeHandler = new Handler(mDecodeThread.getLooper(),mDecoderHandlerCallback);
        mDecodeHandler.sendEmptyMessage(0);
        //testDecodeFile();

    }

    private int reciveFrameNo=0;
    public void decodeFrame(byte[] frameData,int length){
        long time1= System.currentTimeMillis();
        mVideoDecoder.decodeOnly(frameData,length,reciveFrameNo*30,false);
        long time2 = System.currentTimeMillis();
        Log.d("ttttt","reciveFrameNo="+reciveFrameNo+" length="+length+" time="+(time2-time1));
        reciveFrameNo++;
    }
    private void testDecodeFile() {
        new Thread(){
            @Override
            public void run() {
                byte[] frameData = new byte[1920*1080*3/2];//按照yuv420大小申请缓存
                File file = new File("/tmp/h264/stream.h264");
                try {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    DataInputStream dis = new DataInputStream(bis);
                    while(true){
                        int length = dis.readInt();
                        dis.readFully(frameData,0,length);
                        mVideoDecoder.decode(frameData,length,0,false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    mVideoDecoder.close();
                }

            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
