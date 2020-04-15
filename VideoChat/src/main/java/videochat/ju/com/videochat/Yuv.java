package videochat.ju.com.videochat;

/**
 * Created by user on 2017/10/17.
 */

public class Yuv {
    static {
        System.loadLibrary("yuv");   //defaultConfig.ndk.moduleName
    }

    //参考资料 NV12就是YUV420sp  http://www.cnblogs.com/samaritan/p/YUV.html
    public static native void nativeNV21ToYUV420sp(byte[] nv21);
    public static native void nativeYV12ToI420(byte[] yv21);
}
