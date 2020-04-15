package com.tencent.audiochanneldemo.systemchannel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.tencent.karaoketv.audiochannel.AudioFrame;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioReceiver;
import java.io.IOException;

/**
 * Created by zoroweili on 2019-2-26.
 */

public class SystemAudioReceiver extends AudioReceiver{
    private static final String TAG = "SystemAudioReceiver";
    private AudioRecord mAudioRecord = null;
    private int mRecordBufferSize = 0;

    private HandlerThread mRecordThread = new HandlerThread("record thread");
    private HandlerThread mOutThread = new HandlerThread("record out thread");
    private RecordHandler mRecordHandler;
    private OutHandler mOutHandler;
    private static final int RECORD_START = 1;
    private static final int RECORD_READ = 2;
    private static final int RECORD_STOP = 3;
    private static final int OUT_NOTIFY = 1;

    int init(AudioParams audioParams) {
        if (audioParams == null) {
            Log.e(TAG, "init audioParams is null");
            return -1;
        }
        if (mRecordBufferSize > 0) {
            return mRecordBufferSize;
        }
        long sampleRate = audioParams.sampleRate;
        int channelCount = audioParams.channelCount;
        int bitDept = audioParams.bitDepth;
        Log.i(TAG, "init audiorecord sampleRate = " + sampleRate + ",channelCount = " + channelCount + ",bitDept = " + bitDept);
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        switch (channelCount) {
            case 1:
                channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
                break;
            case 2:
                channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
                break;
        }
        int audioFormat = bitDept == AudioParams.BIT_DEPTH_8BIT ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        mRecordBufferSize = AudioRecord.getMinBufferSize((int)sampleRate, channelConfiguration, audioFormat);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, (int)sampleRate, channelConfiguration, audioFormat, mRecordBufferSize);
        Log.i(TAG, "init audiorecord success,buffer size = " + mRecordBufferSize + ", channelConfiguration = " + channelConfiguration);
        return mRecordBufferSize;
    }

    @Override
    public void prepare() throws IOException {
        // do something for prepare if you need
        mRecordThread.start();
        mOutThread.start();
        mRecordHandler = new RecordHandler(mRecordThread.getLooper());
        mOutHandler = new OutHandler(mOutThread.getLooper());

//        notifyPhoneSocketPortsPrepared(getPreparedPhoneSocketPorts());
    }

    @Override
    public int getConnectedDeviceTypes() {
        return DEIVCE_TYPE_PHONE;
    }

    @Override
    public void open(int i) throws IOException {
        if (mRecordHandler != null) {
            mRecordHandler.sendEmptyMessage(RECORD_START);
        }
    }

    @Override
    public void close() throws IOException {
        if (mRecordHandler != null) {
            mRecordHandler.sendEmptyMessage(RECORD_STOP);
        }
    }

    @Override
    public void setReceiveAudioToOutput(boolean b) {

    }

//    @Override
//    public int[] getPreparedPhoneSocketPorts() {
//        int[] ports = new int[3];
//        ports[0] = 123;
//        ports[1] = 456;
//        ports[2] = 789;
//        return ports;
//    }

    class RecordHandler extends Handler {
        private static final int STATE_NEW = 0;
        private static final int STATE_RECORDING = 1;
        private static final int STATE_STOPPED = 2;
        private int recordState = STATE_NEW;
        public RecordHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECORD_START:
                    if (recordState == STATE_NEW) {
                        if (mAudioRecord != null) {
                            mAudioRecord.startRecording();
                            recordState = STATE_RECORDING;
                            if (mRecordHandler != null) {
                                mRecordHandler.sendEmptyMessage(RECORD_READ);
                            }
                        }
                    }
                    break;
                case RECORD_READ:
                    if (recordState == STATE_RECORDING) {
                        if (mAudioRecord != null) {
                            short[] recordBuffer = new short[mRecordBufferSize];
                            mAudioRecord.read(recordBuffer, 0, mRecordBufferSize);
                            if (mRecordHandler != null) {
                                mRecordHandler.sendEmptyMessage(RECORD_READ);
                            }

                            AudioFrame audioFrame = new AudioFrame();
                            audioFrame.buffer = recordBuffer;
                            audioFrame.size = mRecordBufferSize;
                            Message tmpMsg = new Message();
                            tmpMsg.what = OUT_NOTIFY;
                            tmpMsg.obj = audioFrame;
                            mOutHandler.sendMessage(tmpMsg);
                        }
                    }
                    break;
                case RECORD_STOP:
                    if (recordState == STATE_RECORDING) {
                        recordState = STATE_STOPPED;
                        if (mAudioRecord != null) {
                            mAudioRecord.stop();
                            mAudioRecord.release();
                        }
                        mAudioRecord = null;
                    }
                    break;
            }
        }
    }

    class OutHandler extends Handler {
        public OutHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == OUT_NOTIFY) {
                Object obj = msg.obj;
                if (obj != null && obj instanceof AudioFrame) {
                    AudioFrame  audioFrame = (AudioFrame) obj;
                    int playbackPosition = 0; // 如果当前正在播放伴奏，这里应该是人声输入时对应的伴奏位置
                    float voiceVolume = 1; // 人声的瞬时音量大小
                    // 因为这只是demo，所以playbackPosition和voiceVolume都是假数据，希望开发者能给到真正的数据过来
                    notifyReceiveAudioFrame(audioFrame, playbackPosition, voiceVolume);
                }
            }
        }
    }
}
