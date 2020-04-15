package videochat.ju.com.videochat;

import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2017/10/17.
 */

public class VideoChatManager implements VideoChatNetworkManager.RemoteFrameCallback, RemotePreview.DecoderCallback,ICameraEncodeCallback {


    private static VideoChatManager instance=null;

    public static synchronized VideoChatManager getInstance(){
        if(instance==null){
            instance = new VideoChatManager();
        }
        return instance;
    }
    private int mRemoteWidth;
    private int mRemoteHeight;
    private String mRemoteIp;
    private int mRemotePort;

    private RemotePreview mRemotePreview;
    private ICameraPreview mCameraPreview;

    private boolean mBeginEncode=false;


    private VideoChatManager(){

    }

    public void init(ICameraPreview cameraPreview,RemotePreview remotePreview){
        cameraPreview.startPreview();
        cameraPreview.setEncodeCallback(this);
        remotePreview.setDecoderParam(mRemoteWidth,mRemoteHeight);
        remotePreview.setDecoderCallback(this);
        mCameraPreview = cameraPreview;
        mRemotePreview = remotePreview;
        if(mBeginEncode){
            mCameraPreview.beginEncode();
        }
    }

    public void release(){
        mCameraPreview = null;
        mRemotePreview = null;
    }




    @Override
    public void onRemoteConnect(int width, int height, String ip, int port) {
        mRemoteWidth = width;
        mRemoteHeight =height;
        mRemoteIp = ip;
        mRemotePort = port;
    }

    @Override
    public void onBeginEncode() {
        mBeginEncode = true;
        if(mCameraPreview!=null){
            mCameraPreview.beginEncode();
        }
    }
    @Override
    public void onRemoteFrame(byte[] data, int length) {
        mRemotePreview.decodeFrame(data,length);
    }

    @Override
    public void onDecoderReady() {
        try {
            VideoChatNetworkManager.getInstance().beginEncode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int encodeFrameCount =0;
    @Override
    public void onCameraFrameEncode(ByteBuffer data, int offset, int length) {
        try {
            VideoChatNetworkManager.getInstance().sendFrameData(data,offset,length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("ttttt","encode from No="+encodeFrameCount+" length="+length);
        encodeFrameCount++;
    }
}
