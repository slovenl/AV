package com.tencent.audiochanneldemo.systemchannel;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import com.tencent.karaoketv.audiochannel.AudioFrame;
import com.tencent.karaoketv.audiochannel.AudioOutput;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioPlayState;
import java.io.IOException;

/**
 * Created by zoroweili on 2019-2-14.
 * 系统自己的声音输出
 */

public class SystemAudioOutput extends AudioOutput implements AudioPlayState {
    private static final String TAG = "SystemAudioOutput";
    private AudioTrack mAudioTrack;
    private int mPlaybackBufferSize;
    private int mPlayState = PLAYSTATE_NEW;

    public int init(AudioParams audioParams) {
        if (audioParams == null) {
            Log.e(TAG, "init audioParams is null");
            return -1;
        }
        if (mPlaybackBufferSize > 0) {
            return mPlaybackBufferSize;
        }
        long sampleRate = audioParams.sampleRate;
        int channelCount = audioParams.channelCount;
        int bitDept = audioParams.bitDepth;
        Log.i(TAG, "init audiotrack sampleRate = " + sampleRate + ",channelCount = " + channelCount + ",bitDept = " + bitDept);
        int channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO;
        switch (channelCount) {
            case 1:
                channelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            case 6:
                channelConfiguration = AudioFormat.CHANNEL_OUT_5POINT1;
                break;
            case 8:
                channelConfiguration = AudioFormat.CHANNEL_OUT_7POINT1;
                break;
        }
        int audioFormat = bitDept == AudioParams.BIT_DEPTH_8BIT ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        mPlaybackBufferSize = AudioTrack.getMinBufferSize((int) sampleRate, channelConfiguration, audioFormat);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, (int) sampleRate,
            channelConfiguration, audioFormat, mPlaybackBufferSize, AudioTrack.MODE_STREAM);
        Log.i(TAG, "init audioTrack success,buffer size = " + mPlaybackBufferSize + ", channelConfiguration = " + channelConfiguration);
        return mPlaybackBufferSize;
    }

    @Override
    public void start() throws IOException {
        Log.i(TAG, "start");
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.play();
            mPlayState = PLAYSTATE_PLAYING;
            notifyPlayStateChanged(mPlayState);
        }
    }

    @Override
    public void stop() throws IOException {
        Log.i(TAG, "stop");
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.stop();
            mPlayState = PLAYSTATE_STOPPED;
            notifyPlayStateChanged(mPlayState);
        }
    }

    @Override
    public void resume() throws IOException {
        Log.i(TAG, "resume");
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.play();
            mPlayState = PLAYSTATE_PLAYING;
            notifyPlayStateChanged(mPlayState);
        }
    }

    @Override
    public void pause() throws IOException {
        Log.i(TAG, "pause");
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.pause();
            mPlayState = PLAYSTATE_PAUSED;
            notifyPlayStateChanged(mPlayState);
        }
    }

    @Override
    public void flush() throws IOException {
        Log.i(TAG, "flush");
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.flush();
        }
    }

    @Override
    public void release() throws IOException {
        Log.i(TAG, "release");
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.release();
            mPlayState = PLAYSTATE_NEW;
            notifyPlayStateChanged(mPlayState);
        }
        mAudioTrack = null;
        mPlaybackBufferSize = 0;
    }

    @Override
    public int write(AudioFrame audioFrame) throws IOException {
        if (audioFrame == null) {
            Log.e(TAG, "write, but audioFrame is null");
            return 0;
        }

        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            if (audioFrame.isByteData()) {
                return audioTrack.write(audioFrame.byteBuffer, 0, audioFrame.size);
            } else {
                return audioTrack.write(audioFrame.buffer, 0, audioFrame.size);
            }
        }
        return 0;
    }

    @Override
    public void setVolume(float volume) throws IOException {
        Log.e(TAG, "setVolume, volume = " + volume);
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            audioTrack.setStereoVolume(volume, volume);
        }
    }

    @Override
    public void setMicVolume(float volume) throws IOException {

    }

    @Override
    public int getPlaybackHeadPosition() throws IOException {
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            return audioTrack.getPlaybackHeadPosition();
        }
        return 0;
    }

    @Override
    public int getPlaybackBufferSize() throws IOException {
        return mPlaybackBufferSize;
    }

    @Override
    public int getAudioSessionId() {
        AudioTrack audioTrack = mAudioTrack;
        if (audioTrack != null) {
            return audioTrack.getAudioSessionId();
        }
        return 0;
    }

    @Override
    public int getPlayState() {
        return mPlayState;
    }
}
