package com.tencent.audiochanneldemo.player;

/**
 * 非阻塞工作时，如果发生错误了则回调该接口
 * 
 * @author Harvey Xu
 */
public interface OnErrorListener {

    /**
     * 发生错误时回调，错误码见{@link PlayerException}
     * 
     * @param what 错误码
     */
    public void onError(int what);
}
