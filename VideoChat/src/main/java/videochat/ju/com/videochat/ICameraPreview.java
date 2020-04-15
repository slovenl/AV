package videochat.ju.com.videochat;

/**
 * Created by user on 2017/10/19.
 */

public interface ICameraPreview {
    void setEncodeCallback(ICameraEncodeCallback callback);
    void startPreview();
    void beginEncode();

}
