package videochat.ju.com.videochat;

import java.nio.ByteBuffer;

/**
 * Created by mabin1 on 2017/10/19.
 */

public interface ICameraEncodeCallback {
    void onCameraFrameEncode(ByteBuffer data,int offset,int length);
}
