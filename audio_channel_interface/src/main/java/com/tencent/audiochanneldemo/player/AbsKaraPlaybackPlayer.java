package com.tencent.audiochanneldemo.player;

import android.util.Log;
import android.view.Surface;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zoroweili on 2018-9-4.
 * K歌结束之后，用于用户回放的播放器
 */

public abstract class AbsKaraPlaybackPlayer implements PlayerException {
    private static final String TAG = AbsKaraPlaybackPlayer.class.getSimpleName();

    /**
     * 状态
     */
    public class PlayerState {
        public final static int PLAYER_STATE_IDLE = 1 << 0;

        public final static int PLAYER_STATE_INITIALIZED = 1 << 1;

        public final static int PLAYER_STATE_PREPARING = 1 << 2;

        public final static int PLAYER_STATE_PREPARED = 1 << 3;

        public final static int PLAYER_STATE_STARTED = 1 << 4;

        public final static int PLAYER_STATE_PAUSED = 1 << 5;

        public final static int PLAYER_STATE_COMPLETE = 1 << 6;

        public final static int PLAYER_STATE_STOPPED = 1 << 7;

        public final static int PLAYER_STATE_ERROR = 1 << 8;

        private int state;

        /**
         * 默认状态为{@link #PLAYER_STATE_IDLE}
         */
        public PlayerState() {
            super();
            this.state = PLAYER_STATE_IDLE;
        }

        /**
         * 获取当前状态
         *
         * @return 当前状态
         */
        public synchronized int state() {
            return state;
        }

        /**
         * 状态迁移
         *
         * @param state
         *            新状态
         */
        public synchronized void transfer(int state) {
            Log.i(TAG, "[" +  AbsKaraPlaybackPlayer.this + "] switch state: " + this.state + " -> " + state);
            this.state = state;
        }

        /**
         * 判断状态
         *
         * @param states
         *            预期的一个或多个状态
         * @return true，符合；false，不符合
         */
        public synchronized boolean equalState(int... states) {
            int temp = 0;
            for (int i = 0; i < states.length; i++) {
                temp |= states[i];
            }
            return (state & temp) != 0;
        }

        /**
         * 当满足状态条件时，进行wait。
         * 一旦被notify就退出，但是退出并不一定表示其不满足状态条件
         *
         * @param states 进入wait的条件状态
         */
        public synchronized void waitState(int... states) {
            if (equalState(states)) {
                Log.d(TAG, "[" +  AbsKaraPlaybackPlayer.this + "] wait, actual: " + this.state + ", expected: " + Arrays.toString(states));
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.w(TAG, e.toString());
                }
                Log.d(TAG, "[" +  AbsKaraPlaybackPlayer.this + "] wake, actual: " + this.state + ", expected: " + Arrays.toString(states));
            }
        }

        /**
         * 当满足状态条件时，进行wait，只有不满足状态条件，才会退出
         *
         * @param states 进入wait的条件状态
         */
        public synchronized void waitStateAlways(int... states) {
            while (equalState(states)) {
                Log.d(TAG, "[" +  AbsKaraPlaybackPlayer.this + "] wait, actual: " + this.state + ", expected: " + Arrays.toString(states));
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.w(TAG, e.toString());
                }
                Log.d(TAG, "[" +  AbsKaraPlaybackPlayer.this + "] wake, actual: " + this.state + ", expected: " + Arrays.toString(states));
            }
        }

        @Override
        public String toString() {
            return "State[" + state + "]";
        }

    }

    /**
     * 状态
     */
    final protected PlayerState mCurrentState = new PlayerState();

    /**
     * 仅仅包含了伴唱的PCM数据的文件路径
     */
    protected final String mObbPcmPath;

    /**
     * 当前播放开始时间，主要用来精确计算播放时间
     */
    protected long mPlayStartTime = -1;

    /**
     * 当前播放进度
     */
    protected int mPlayTime;

    /**
     * 开始播放的时间位置，单位毫秒，最低位为0
     */
    protected int mStartTime = 0;

    protected Surface mSurface;

    /**
     * 进度回调
     */
    final protected List<OnProgressListener> mProListeners = new CopyOnWriteArrayList<OnProgressListener>();
    /**
     * 出错回调
     */
    final protected List<OnErrorListener> mErrListeners = new CopyOnWriteArrayList<OnErrorListener>();

    /**
     * 增加{@link OnProgressListener}
     *
     * @param listener
     */
    public void addOnProgressListener(OnProgressListener listener) {
        if (!mProListeners.contains(listener)) {
            mProListeners.add(listener);
        }
    }

    /**
     * 移除{@link OnProgressListener}
     *
     * @param listener
     */
    public void removeOnProgressListener(OnProgressListener listener) {
        mProListeners.remove(listener);
    }

    /**
     * 初始化播放器，并在初始化完成后，回调{@code OnPreparedListener}。
     *
     * @param preListener
     *            初始化完成监听器，有关音频的信息可以从接口获得
     */
    public abstract void init(final OnPreparedListener preListener);

    /**
     * 开始播放音频 <br/>
     * <em>警告</em>：该方法是非阻塞的
     */
    public abstract void start();

    /**
     * 暂停
     */
    public abstract void pause();

    /**
     * 继续
     */
    public abstract void resume();

    /**
     * 停止
     */
    public abstract void stop();

    /**
     * 获取当前播放位置。
     * <p>
     * 如果开始位置不为0，则该值为相对于开始位置的相对时间
     *
     * @return 播放位置，单位毫秒
     */
    public abstract int getPlayTime();

    /**
     * Class constructor，适用于伴奏文件是完整
     * @param obbPcmPath
     *            伴奏PCM的路径
     */
    public AbsKaraPlaybackPlayer(String obbPcmPath) {
        mObbPcmPath = obbPcmPath;
    }

    /**
     * 通知出错
     *
     * @param error
     */
    protected void notifyError(int error) {
        for (OnErrorListener listener : mErrListeners) {
            listener.onError(error);
        }
    }
}
