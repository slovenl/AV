package com.tencent.audiochanneldemo.player;

import android.annotation.SuppressLint;
import android.media.AudioTrack;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.audiochanneldemo.channelmanager.AudioOutputChannelManager;
import com.tencent.audiochanneldemo.channelmanager.AudioReceiverChannelManager;
import com.tencent.audiochanneldemo.utils.DemoUtil;
import com.tencent.karaoketv.audiochannel.AudioFrame;
import com.tencent.karaoketv.audiochannel.AudioOutput;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioPlayState;
import com.tencent.karaoketv.audiochannel.AudioReceiver;
import com.tencent.karaoketv.audiochannel.AudioReceiverCallback;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zoroweili on 2018-9-4.
 * K歌结束之后，用于用户回放的播放器
 */
@SuppressLint("NewApi")
public class KaraPlaybackPlayer extends AbsKaraPlaybackPlayer {
    private static final String TAG = KaraPlaybackPlayer.class.getSimpleName();
    /**
     * 当前回放音频的信息（主要就是时长）
     */
    private AudioParams mInfo = new AudioParams();
    final private PlayerConfig mConfig = new PlayerConfig();

    /**
     * 单声道PCM的大小，用来辅助计算播放位置
     */
    private long mPcmSize;

    /**
     * 音频播放线程
     */
    private Thread mAudioThread;

    protected volatile boolean mIsReleased = false;

    private int mProgressUpdateCount;

    private WavFileWriter mWavFile;

    private AudioOutput mAudioOutput;
    private AudioReceiver mAudioReceiver;

    /**
     * Class constructor，适用于伴奏文件是完整
     *
     * @param playerConfig
     *            播放配置
     */
    public KaraPlaybackPlayer(@NonNull PlayerConfig playerConfig) {
        super(playerConfig.mediaPcmPath);
        mConfig.copyFrom(playerConfig);
    }

    @Override
    public void init(OnPreparedListener preListener) {
        File obbFile = new File(mObbPcmPath);
        mPcmSize = obbFile.length();
        Log.i(TAG, "obbFile length " + mPcmSize);

        mCurrentState.transfer(PlayerState.PLAYER_STATE_INITIALIZED);

        if (!mConfig.isSearch) {
            mAudioThread =
                new PlaybackThread("KaraPcmPlayer-AudioThread-" + System.currentTimeMillis());
            mAudioThread.start();
        }

        if (preListener != null) {
            preListener.onPrepared(mInfo);
        }
    }

    /**
     * 开始播放音频 <br/>
     * <em>警告</em>：该方法是非阻塞的
     */
    @Override
    public void start() throws IllegalStateException{
        Log.d(TAG, "start");
        if (mConfig.isSearch) {
            AudioParams audioParams = new AudioParams();
            audioParams.channelCount = 2;
            audioParams.sampleRate = 44100;
            audioParams.bitDepth = 2;
            startReceiveMicData(audioParams);
        }

        synchronized (mCurrentState) {
            if (mCurrentState.equalState(PlayerState.PLAYER_STATE_STARTED)) {
                return;
            }
            if (mCurrentState.equalState(PlayerState.PLAYER_STATE_INITIALIZED, PlayerState.PLAYER_STATE_PAUSED)) {
                mCurrentState.transfer(PlayerState.PLAYER_STATE_STARTED);
                mCurrentState.notifyAll();
            } else {
                throw new IllegalStateException(mCurrentState.toString());
            }
        }
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        Log.d(TAG, "pause");
        if (mConfig.isSearch) {
            return;
        }

        synchronized (mCurrentState) {
            if (mCurrentState.equalState(PlayerState.PLAYER_STATE_PAUSED)) {
                return;
            }

            if (mCurrentState.equalState(PlayerState.PLAYER_STATE_STARTED)) {
                mCurrentState.transfer(PlayerState.PLAYER_STATE_PAUSED);
            } else {
                if (mCurrentState.equalState(PlayerState.PLAYER_STATE_COMPLETE)) {
                    Log.w(TAG, "pause -> current state:"+mCurrentState.toString());
                    return;
                }
                throw new IllegalStateException(mCurrentState.toString());
            }
        }
    }

    /**
     * 继续
     */
    @Override
    public void resume() {
        Log.d(TAG, "resume, delegate to start");
        if (mConfig.isSearch) {
            return;
        }
        start();
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        Log.d(TAG, "stop mCurrentState:" + mCurrentState);
        if (mConfig.isSearch) {
            stopReceiverMicData();
            try{
                for (OnProgressListener lis : mProListeners) {
                    lis.onStop();
                }
            }catch (Exception e){
                Log.e(TAG, "onStop fail: " + e.getMessage());
            }
            return;
        }
        synchronized (mCurrentState) {
            if (mCurrentState.equalState(PlayerState.PLAYER_STATE_STOPPED)) {
                return;
            }
            if (mCurrentState.equalState(PlayerState.PLAYER_STATE_ERROR,
                    PlayerState.PLAYER_STATE_INITIALIZED,
                    PlayerState.PLAYER_STATE_STARTED,
                    PlayerState.PLAYER_STATE_PAUSED,
                    PlayerState.PLAYER_STATE_COMPLETE)) {
                Log.d(TAG, "stop ------ 1 - 1");
                mCurrentState.transfer(PlayerState.PLAYER_STATE_STOPPED);
                mCurrentState.notifyAll();
            } else {
                Log.e(TAG, "stop error mCurrentState = " + mCurrentState);
                throw new IllegalStateException("Curent state: " + mCurrentState);
            }
        }

        if (mAudioThread != null && mAudioThread.isAlive() && Thread.currentThread().getId() != mAudioThread.getId()) {
            try {
                mAudioThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public int getPlayTime() {
        return mPlayTime;
    }

    /**
     * 用于回放的线程，其中通过一个循环不断地混音，然后输出
     */
    private class PlaybackThread extends Thread {

        public PlaybackThread(String threadName) {
            super(threadName);
        }

        @Override
        public void run() {
            super.run();
            Log.i(TAG, "playback thread begin");
            int channel = 2;
            int sampleRate = 44100;
            int bitDepth = 2;
            boolean switchRet = AudioOutputChannelManager.getInstance().switchAudioOutput(mConfig.useSystem);
            if (!switchRet) {
                Log.e(TAG, "run switch error");
                mCurrentState.transfer(PlayerState.PLAYER_STATE_ERROR);
                notifyError(EXCEPTION_TYPE_INIT_EXCEPTION);
                return;
            }
            if (TextUtils.isEmpty(mObbPcmPath)) {
                Log.e(TAG, "mFilePath is null");
                mCurrentState.transfer(PlayerState.PLAYER_STATE_ERROR);
                notifyError(EXCEPTION_TYPE_FILENOTFOUND);
                return;
            }

            RandomAccessFile leftFile = null;
            try {
                leftFile = new RandomAccessFile(mObbPcmPath, "r");
            } catch (IOException e) {
                mCurrentState.transfer(PlayerState.PLAYER_STATE_ERROR);
                Log.e(TAG, "error when ready for the RandomAccessFile ", e);
                notifyError(EXCEPTION_TYPE_FILENOTFOUND);
            }

            AudioParams audioParams = new AudioParams();
            audioParams.channelCount = channel;
            audioParams.sampleRate = sampleRate;
            audioParams.bitDepth = bitDepth;
            mAudioOutput = AudioOutputChannelManager.getInstance().createAudioOutput(audioParams);

            if (mAudioOutput == null) {
                Log.e(TAG, "createAudioOutput return null");
                mCurrentState.transfer(PlayerState.PLAYER_STATE_ERROR);
                notifyError(EXCEPTION_TYPE_INIT_EXCEPTION);
            } else {
                try {
                    mAudioOutput.setVolume(mConfig.mediaVol);
                    mAudioOutput.setMicVolume(mConfig.micVol);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            int playbackBufferSize = 0;
            try {
                playbackBufferSize = mAudioOutput.getPlaybackBufferSize();
            } catch (Exception e) {
                e.printStackTrace();
            }


            Log.i(TAG, "mTrack init playbackBufferSize " + playbackBufferSize);

            if (playbackBufferSize == AudioTrack.ERROR_BAD_VALUE || playbackBufferSize == AudioTrack.ERROR) {
                Log.w(TAG, "AudioTrack.getMinBufferSize failed: " + playbackBufferSize);
                mCurrentState.transfer(PlayerState.PLAYER_STATE_ERROR);
                notifyError(EXCEPTION_TYPE_INIT_EXCEPTION);
            }

            int shortSize = playbackBufferSize / 2;
            int byteSize = shortSize * 2;
            Log.i(TAG, "shortSize " + shortSize + "  byteSize " + byteSize);
            byte[] byteBuffer = new byte[byteSize];
            short[] shortBuffer = new short[shortSize];
            int leftRead = 0;

            while(true) {
                if (mCurrentState.equalState(PlayerState.PLAYER_STATE_STARTED)) {
                    int po = 0;
                    try {
                        leftRead = leftFile.read(byteBuffer);
                        po = (int)leftFile.getChannel().position();
                    } catch (IOException e) {
                        mCurrentState.transfer(PlayerState.PLAYER_STATE_ERROR);
                        Log.e(TAG, "read file error ", e);
                        notifyError(EXCEPTION_TYPE_IO);
                        continue;
                    }


                    if (leftRead == -1) {
                        Log.i(TAG, "PCM file eof");
                        mCurrentState.transfer(PlayerState.PLAYER_STATE_COMPLETE);
                        continue;
                    }

                    // 对最后一点不足4096字节的数据补0
                    if (leftRead < byteSize) {
                        Log.d(TAG, "file read count : " + leftRead);
                        for (int i = leftRead; i < byteSize; i++) {
                            byteBuffer[i] = 0;
                        }
                        leftRead = byteSize;
                    }

                    try {
                        if (mAudioOutput.getPlayState() <= AudioPlayState.PLAYSTATE_PAUSED) {
                            startReceiveMicData(audioParams);
                            mAudioOutput.start();
                            Log.i(TAG, "mAudioOutput.start()");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    short temp1 = -1;
                    short temp2 = -1;
                    for (int i = 0; i < shortSize; i++) {
                        temp1 = (short) (byteBuffer[i * 2] & 0xff);// 最低位
                        temp2 = (short) (byteBuffer[i * 2 + 1] & 0xff);
                        temp2 <<= 8;
                        shortBuffer[i] = (short) (temp1 | temp2);
                    }
                    AudioFrame audioFrame = new AudioFrame();
                    audioFrame.buffer = shortBuffer;
                    audioFrame.size = shortSize;
                    int temp = 0;

                    try {
                        temp = mAudioOutput.write(audioFrame);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (temp == AudioTrack.ERROR_INVALID_OPERATION || temp == AudioTrack.ERROR_BAD_VALUE) {
                        Log.w(TAG, "AudioTrack write fail: " + temp);
//                    for (OnErrorListener listener : mErrListeners) {
//                        listener.onError(MediaConstant.ERROR_PLAYER_AUDIO_TRACK_WRITE_FAIL);
//                    }
                    }

                    // 计算进度等
                    synchronized (mCurrentState) {
                        // 通知回调
                        try{
                            if(mProgressUpdateCount++ % 6 == 0) {
                                for (OnProgressListener lis : mProListeners) {
                                    int framePosition = mAudioOutput.getPlaybackHeadPosition();
                                    lis.onProgressUpdate(DemoUtil.frameSizeToTimeMillis(framePosition, sampleRate), 100000);
                                }
                            }
                        }catch (Exception e){
                            Log.e(TAG, "onProgressUpdate fail: " + e.getMessage());
                        }
                    }
                }

                if (mCurrentState.equalState(PlayerState.PLAYER_STATE_PAUSED)) {
                    try {
                        if (mAudioOutput.getPlayState() == AudioPlayState.PLAYSTATE_PLAYING) {
                            mAudioOutput.pause();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mCurrentState.waitState(PlayerState.PLAYER_STATE_PAUSED);
                }

                if (mCurrentState.equalState(PlayerState.PLAYER_STATE_COMPLETE)) {
                    mPlayStartTime = -1;
                    try{
                        for (OnProgressListener lis : mProListeners) {
                            lis.onComplete();
                        }
                    }catch (Exception e){
                        Log.e(TAG, "onComplete fail: " + e.getMessage());
                    }
                    mCurrentState.waitStateAlways(PlayerState.PLAYER_STATE_COMPLETE);
                }

                if (mCurrentState.equalState(PlayerState.PLAYER_STATE_STOPPED, PlayerState.PLAYER_STATE_ERROR)) {
                    mIsReleased = true;

                    if (leftFile != null) {
                        try {
                            leftFile.close();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }

                    try {
                        if(mAudioOutput != null) {
                            mAudioOutput.flush();
                            mAudioOutput.stop();
                            mAudioOutput.release();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mAudioOutput = null;

                    stopReceiverMicData();

                    try{
                        for (OnProgressListener lis : mProListeners) {
                            lis.onStop();
                        }
                    }catch (Exception e){
                        Log.e(TAG, "onStop fail: " + e.getMessage());
                    }

//                    if (mCrypto != null) {
//                        mCrypto.release();
//                    }

                    // 清空各类回调
                    mProListeners.clear();
                    mErrListeners.clear();
                    break;
                }
            }
        }
    }

    private void startReceiveMicData(AudioParams audioParams) {
        if (audioParams == null) {
            return;
        }
        boolean installRet = AudioReceiverChannelManager.getInstance().switchAudioReceiver(mConfig.useSystem);
        if (mAudioReceiver == null && mConfig.needSaveMicData && installRet) {
            mAudioReceiver = AudioReceiverChannelManager.getInstance()
                .createAudioReceiver(audioParams);
            if (mAudioReceiver != null) {
                mAudioReceiver.registerCallback(mAudioReceiverCallback);
                File tmpFile1 = new File(mConfig.saveMicPcmPath);

                mWavFile = new WavFileWriter(tmpFile1, (int)audioParams.sampleRate, audioParams.channelCount, audioParams.bitDepth * 8);
                try {
                    mWavFile.open(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mAudioReceiver.prepare();
                    mAudioReceiver.setReceiveAudioToOutput(!mConfig.isSearch);
                    mAudioReceiver.open(AudioReceiver.DEIVCE_TYPE_MIC);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopReceiverMicData() {
        try {
            if (mAudioReceiver != null) {
                mAudioReceiver.unRegisterCallback(mAudioReceiverCallback);
                mAudioReceiver.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAudioReceiver = null;

        if (mWavFile != null) {
            try {
                mWavFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mWavFile = null;
        }
    }

    private AudioReceiverCallback mAudioReceiverCallback = new AudioReceiverCallback() {
        @Override
        public void onAudioDataReceived(AudioFrame frame, int pos, float volume) {
            super.onAudioDataReceived(frame, pos, volume);
            //Log.i(TAG, "onAudioDataReceived  pos " + pos);
            if (mWavFile != null && frame != null) {
                if (frame.isByteData()) {
                    try {
                        String buf = Byte.toString(frame.byteBuffer[0]);
                        Log.i(TAG, "mWavFile.write  byte frame.size " + frame.size + "  buf " + buf);
                        mWavFile.write(frame.byteBuffer, 0, frame.size);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String buf = Short.toString(frame.buffer[0]);
                        Log.i(TAG, "mWavFile.write  short frame.size " + frame.size + "  buf " + buf);
                        mWavFile.write(frame.buffer, 0, frame.size);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onPhoneSocketPortsPrepared(int[] ports) {
            super.onPhoneSocketPortsPrepared(ports);
        }
    };
}
