package com.tencent.audiochanneldemo.player;

/**
 * 进度监听器
 *
 * @author Harvey Xu
 */
public interface OnProgressListener {

    /**
     * 进度更新
     *
     * @param now
     *            当前进度。如果播放器开始位置不为0，则该值为相对于开始位置的相对时间
     * @param duration
     *            总体时长
     */
    public void onProgressUpdate(int now, int duration);

    /**
     * 进度完成，注意，不一定是正常完成， 也就是说在此之前{@link #onProgressUpdate(int, int)}中{@code now}和{@code duration}不一定相等
     */
    public void onComplete();

    public void onStop();

    void onError(int ret);
}

