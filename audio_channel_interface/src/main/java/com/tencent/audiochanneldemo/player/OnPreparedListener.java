package com.tencent.audiochanneldemo.player;

import com.tencent.karaoketv.audiochannel.AudioParams;

/**
 * 播放器准备OK后进行回调
 * 
 * @author Harvey Xu
 */
public interface OnPreparedListener {

    /**
     * 准备OK
     * 
     * @param info
     */
    public void onPrepared(AudioParams info);
}
