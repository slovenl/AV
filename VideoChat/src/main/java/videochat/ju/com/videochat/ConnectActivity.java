package videochat.ju.com.videochat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mabin1 on 2017/10/18.
 */

public class ConnectActivity extends AppCompatActivity implements VideoChatNetworkManager.RemoteFrameCallback {
    private static final String CAMERA_PERMISSION="android.permission.CAMERA";
    private boolean mIsCaller=false;
    private int mLocalWidth;
    private int mLocalHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dumpCodecInfo();
        if(ActivityCompat.checkSelfPermission(this,CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{CAMERA_PERMISSION}, 0);
            return;
        }
        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int result :grantResults){
            if(result == PackageManager.PERMISSION_DENIED){
                return;
            }
        }
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsCaller = false;
    }

    private void init(){
        VideoChatNetworkManager.getInstance().setRemoteFrameCallback(this);

        Camera camera = CameraPreview.getCameraInstance();
        Camera.Parameters params = camera.getParameters();
        dumpCameraInfo(params);
        Camera.Size size = params.getSupportedPreviewSizes().get(0);
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        int count = Camera.getNumberOfCameras();
        for(int i=0;i<count;i++){
            android.hardware.Camera.getCameraInfo(i, info);
            Log.d("ttttt","camera "+i+" "+((info.facing == Camera.CameraInfo.CAMERA_FACING_BACK)?"Facing "
                    + "back":"Facing front")+" orientation="+info.orientation);
        }
        camera.release();
        mLocalWidth =size.width;
        mLocalHeight =size.height;

        setContentView(R.layout.connect);
        final EditText ipText = (EditText) this.findViewById(R.id.ed_ip);
        TextView resolutionText = (TextView) this.findViewById(R.id.tv_resolution);
        Button connectButton = (Button) this.findViewById(R.id.bt_connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ipText.getText().toString().trim();
                int port =3000;

                try {
                    VideoChatNetworkManager.getInstance().connect(ip,port, mLocalWidth, mLocalHeight);
                    mIsCaller = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        resolutionText.setText("Width="+ mLocalWidth +" Height="+ mLocalHeight);

    }
    private void dumpCameraInfo(Camera.Parameters params) {
        List<Integer> supportFormat = params.getSupportedPreviewFormats();
        List<int[]> supportFpfs= params.getSupportedPreviewFpsRange();
        List<Camera.Size> supportSize = params.getSupportedPreviewSizes();
        for(Integer format : supportFormat){
            switch (format.intValue()){
                case ImageFormat.NV21:
                    Log.d("ttttt","support format NV21");
                    break;
                case ImageFormat.NV16:
                    Log.d("ttttt","support format NV16");
                    break;
                case ImageFormat.YV12:
                    Log.d("ttttt","support format YV12");
                    break;
                case ImageFormat.YUY2:
                    Log.d("ttttt","support format YUY2");
                    break;
                case ImageFormat.YUV_420_888:
                    Log.d("ttttt","support format YUV_420_888");
                    break;
                case ImageFormat.YUV_422_888:
                    Log.d("ttttt","support format YUV_422_888");
                    break;
                case ImageFormat.YUV_444_888:
                    Log.d("ttttt","support format YUV_444_888");
                    break;
            }
        }
        for(int[] fps:supportFpfs){
            Log.d("ttttt","support fps "+ Arrays.toString(fps));
        }
        for(Camera.Size size:supportSize){
            Log.d("ttttt","support size H="+size.height+" W="+size.width);
        }
    }
    private void dumpCodecInfo() {
        String mimeType="video/avc";
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            String[] types = codecInfo.getSupportedTypes();
            Log.d("ttttt","codec="+codecInfo.getName()+" support type="+ Arrays.toString(codecInfo
                    .getSupportedTypes()));
            for (int j = 0; j < types.length; j++) {
                if(mimeType.equals(types[j])){
                    MediaCodecInfo.CodecCapabilities cap = codecInfo.getCapabilitiesForType(mimeType);
                    Log.d("ttttt","support color="+Arrays.toString(cap.colorFormats));
                    for(int k=0;k<cap.colorFormats.length;k++){
                        int colorFormat = cap.colorFormats[k];
                        switch (colorFormat){
                            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                                Log.d("ttttt","support COLOR_FormatYUV420Planar");
                                break;
                            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                                Log.d("ttttt","support COLOR_FormatYUV420PackedPlanar");
                                break;
                            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                                Log.d("ttttt","support COLOR_FormatYUV420SemiPlanar");
                                break;
                            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible:
                                Log.d("ttttt","support COLOR_FormatYUV420Flexible");
                                break;

                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRemoteConnect(int width, int height, String ip, int port) {
        VideoChatManager.getInstance().onRemoteConnect(width,height,ip,port);
        VideoChatNetworkManager.getInstance().setRemoteFrameCallback(VideoChatManager.getInstance());
        if(!mIsCaller){
            try {
                VideoChatNetworkManager.getInstance().connect(ip,port, mLocalWidth, mLocalHeight);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBeginEncode() {
        //do nothing
    }

    @Override
    public void onRemoteFrame(byte[] data, int length) {
        //do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }catch (Exception e){
            Log.d("ttttt","",e);
        }
    }
}
