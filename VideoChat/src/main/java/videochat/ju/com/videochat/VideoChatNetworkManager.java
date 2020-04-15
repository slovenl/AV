package videochat.ju.com.videochat;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by mabin1 on 2017/10/17.
 */

public class VideoChatNetworkManager {
    private static final String TAG="VideoChatNetworkManager";
    private final HandlerThread mSendThread;
    private final Handler mSendHandler;

    public static interface RemoteFrameCallback{
        void onRemoteConnect(int width,int height,String ip,int port);
        void onBeginEncode();
        void onRemoteFrame(byte[] data,int length);
    }
    private static VideoChatNetworkManager instance=null;

    public static synchronized VideoChatNetworkManager getInstance(){
        if(instance==null){
            instance = new VideoChatNetworkManager();
        }
        return instance;
    }
    private static final int CMD_CONNECT=0;
    private static final int CMD_BEGIN_ENCODE=1;
    private DatagramSocket mLocalSocket;
    private String remoteIp=null;
    private int remotePort=-1;
    private RemoteFrameCallback mRemoteFrameCallback;

    private Handler.Callback mSnedCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                mLocalSocket.send((DatagramPacket) msg.obj);
            } catch (IOException e) {
                Log.e(TAG,"",e);
            }
            return true;
        }
    };

    private VideoChatNetworkManager() {
        try {
            mLocalSocket = new DatagramSocket(3000);
        } catch (SocketException e) {
            Log.e(TAG,"",e);
        }

        mSendThread = new HandlerThread("Network Send");
        mSendThread.start();
        mSendHandler = new Handler(mSendThread.getLooper(),mSnedCallback);

        new Thread(){
            @Override
            public void run() {
                try{
                    byte[] buf = new byte[1024];
                    ByteBuffer frameBuffer=null;
                    //接收从客户端发送过来的数据
                    DatagramPacket receivePackage = new DatagramPacket(buf, 1024);
                    boolean f = true;
                    while(f){
                        mLocalSocket.receive(receivePackage);
                        remoteIp = receivePackage.getAddress().getHostAddress();
                        remotePort = receivePackage.getPort();
                        byte[] data = receivePackage.getData();
                        int length = receivePackage.getLength();
                        if(length >8){
                            if(data[0]==0 && data[1]==0 && data[2]==0 && data[3]==1 && data[4]==0 && data[5]==0 && data[6]==0 &&data[7]==1){
                                //连接命令
                                ByteBuffer cmdBuffer= ByteBuffer.wrap(data,8,length);
                                int cmd = cmdBuffer.getInt();
                                switch(cmd){
                                    case CMD_CONNECT:
                                        int width = cmdBuffer.getInt();
                                        int height = cmdBuffer.getInt();
                                        Log.d(TAG,"recive CONNECT width="+width+" height="+height);
                                        frameBuffer =ByteBuffer.allocate(width * height * 3/2);
                                        if(mRemoteFrameCallback!=null){
                                            mRemoteFrameCallback.onRemoteConnect(width,height,remoteIp,remotePort);
                                        }
                                        break;
                                    case CMD_BEGIN_ENCODE:
                                        Log.d(TAG,"recive BEGIN_ENCODE");
                                        if(mRemoteFrameCallback!=null){
                                            mRemoteFrameCallback.onBeginEncode();
                                        }
                                        break;
                                }
                            }else if(frameBuffer==null){
                            }else if(data[0]==0 && data[1]==0 && data[2]==0 && data[3]==1){
                                //视频帧数据
                                if(frameBuffer.position() >0){
                                    if(mRemoteFrameCallback!=null){
                                        mRemoteFrameCallback.onRemoteFrame(frameBuffer.array(),frameBuffer.position());
                                    }
                                }
                                frameBuffer.rewind();
                                frameBuffer.put(data,0,length);
                            }else{
                                frameBuffer.put(data,0,length);
                            }
                        }



                        receivePackage.setLength(1024);
                    }

                }catch (Exception e){
                    Log.e(TAG,"",e);
                }




            }
        }.start();

    }

    public void setRemoteFrameCallback(RemoteFrameCallback remoteFrameCallback) {
        mRemoteFrameCallback = remoteFrameCallback;
    }

    public void connect(String ip,int port,int width,int height) throws IOException {
        byte[] buf = new byte[20];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.putInt(1);
        byteBuffer.putInt(1);
        byteBuffer.putInt(CMD_CONNECT);
        byteBuffer.putInt(width);
        byteBuffer.putInt(height);
        DatagramPacket sendPackage = new DatagramPacket(buf,0, 20);
        sendPackage.setAddress(InetAddress.getByName(ip));
        sendPackage.setPort(port);
        mSendHandler.sendMessage(mSendHandler.obtainMessage(0,sendPackage));
        Log.d(TAG,"send connect width"+width+" height="+height);
    }
    public void beginEncode() throws IOException {
        byte[] buf = new byte[12];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.putInt(1);
        byteBuffer.putInt(1);
        byteBuffer.putInt(CMD_BEGIN_ENCODE);
        DatagramPacket sendPackage = new DatagramPacket(buf,0,12);
        sendPackage.setAddress(InetAddress.getByName(remoteIp));
        sendPackage.setPort(remotePort);
        mSendHandler.sendMessage(mSendHandler.obtainMessage(0,sendPackage));
        Log.d(TAG,"send begin encode");
    }
    public void disconnect(){
        remoteIp=null;
        remotePort = -1;
    }
    public void sendFrameData(ByteBuffer data,int offset,int length) throws IOException {
        long time1= System.currentTimeMillis();
        int mtu=500;
        byte[] temp = new byte[mtu];
        int position =offset;
        while(position < length){
            int packageLength = Math.min(mtu,length - position);
            data.get(temp,0,packageLength);
            DatagramPacket sendPackage = new DatagramPacket(temp,0, packageLength);
            sendPackage.setAddress(InetAddress.getByName(remoteIp));
            sendPackage.setPort(remotePort);
            mLocalSocket.send(sendPackage);
            position +=packageLength;
        }
        long time2= System.currentTimeMillis();
        //Log.d(TAG,"send frame "+(time2-time1));
    }

}
