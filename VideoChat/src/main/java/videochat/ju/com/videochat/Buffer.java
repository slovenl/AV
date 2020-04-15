package videochat.ju.com.videochat;

import java.nio.ByteBuffer;

/**
 * Created by mabin1 on 2017/10/16.
 */

public class Buffer{
    public ByteBuffer data;
    public int length;
    public long pts;
    public boolean isIFrame;
    public int colorFormat;
    @Override
    public String toString() {
        return "Buffer [data=" + data + ", length=" + length + ", pts=" + pts + ", isIFrame=" + isIFrame + "]";
    }
}