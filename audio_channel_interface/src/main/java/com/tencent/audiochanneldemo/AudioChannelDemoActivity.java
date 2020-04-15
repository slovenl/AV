package com.tencent.audiochanneldemo;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.audiochanneldemo.channelmanager.AudioChannelInitializer;
import com.tencent.audiochanneldemo.channelmanager.AudioOutputChannelManager;
import com.tencent.audiochanneldemo.channelmanager.AudioReceiverChannelManager;
import com.tencent.audiochanneldemo.player.KaraPlaybackPlayer;
import com.tencent.audiochanneldemo.player.OnPreparedListener;
import com.tencent.audiochanneldemo.player.OnProgressListener;
import com.tencent.audiochanneldemo.player.PlayerConfig;
import com.tencent.audiochanneldemo.utils.DemoUtil;
import com.tencent.karaoketv.audiochannel.AudioFrame;
import com.tencent.karaoketv.audiochannel.AudioOutput;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioReceiver;
import com.tencent.karaoketv.audiochannel.AudioReceiverCallback;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.text.format.Formatter.formatIpAddress;

public class AudioChannelDemoActivity extends Activity {
    private static final String TAG = "AudioChannelDemo";

    DemoHandler mThreadHandler;
    HandlerThread mHandlerThread = new HandlerThread("handler thread");

    private TextView mTopInfoTextView;
    private Button mPlayKaraokeBtn;
    private Button mVoiceSearchBtn;
    private Button mSwitchAudioOutputBtn;
    private Button mPauseBtn;
    private Button mResumeBtn;
    private Button mStopBtn;
    private TextView mSavedMicInfoView;
    private Button mPlaySavedMicBtn;
    private Button mGetDeviceTypeBtn;
    private TextView mDeviceTypeView;
    private TextView mPlayTimeView;
    private Button mMediaVolUpBtn;
    private Button mMediaVolDownBtn;
    private Button mMicVolUpBtn;
    private Button mMicVolDownBtn;
    private TextView mMediaVolView;
    private TextView mMicVolView;
    private TextView mIpView;
    private TextView mPortsView;

    private KaraPlaybackPlayer karaPlaybackPlayer;

    private boolean mIsSystem = true;

    private String mFilePath = "";
    private String mSaveMicPcmPath = "";
    private int mMediaVol = 100;
    private int mMicVol = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandlerThread.start();
        mThreadHandler = new DemoHandler(mHandlerThread.getLooper());
        initUI();
        init();
        initListener();
    }

    private void initUI() {
        mTopInfoTextView = findViewById(R.id.text_top_info);
        mPlayKaraokeBtn = findViewById(R.id.button_play_karaoke);
        mVoiceSearchBtn = findViewById(R.id.button_voice_search);
        mSwitchAudioOutputBtn = findViewById(R.id.button_switch_audiooutput);
        mPauseBtn = findViewById(R.id.button_pause);
        mResumeBtn = findViewById(R.id.button_resume);
        mStopBtn = findViewById(R.id.button_stop);
        mSavedMicInfoView = findViewById(R.id.text_saved_mic_info);
        mPlaySavedMicBtn = findViewById(R.id.button_play_save_mic);
        mGetDeviceTypeBtn = findViewById(R.id.button_get_device_type);
        mDeviceTypeView = findViewById(R.id.text_device_type);
        mIpView = findViewById(R.id.text_ip);
        mPortsView = findViewById(R.id.text_ports);
        mPlayTimeView = findViewById(R.id.text_play_time);
        mMediaVolUpBtn = findViewById(R.id.button_media_vol_up);
        mMediaVolDownBtn = findViewById(R.id.button_media_vol_down);
        mMicVolUpBtn = findViewById(R.id.button_mic_vol_up);
        mMicVolDownBtn = findViewById(R.id.button_mic_vol_down);
        mMediaVolView = findViewById(R.id.text_media_vol);
        mMicVolView = findViewById(R.id.text_mic_vol);


        mMediaVolView.setText(mMediaVol + "%");
        mMicVolView.setText(mMicVol + "%");
    }

    class DemoHandler extends Handler {
        public DemoHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private void init() {
        mTopInfoTextView.setText("初始化中...");
        Log.i(TAG, "初始化中...");
        mThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                AudioChannelInitializer.init();
                DemoUtil.copyFiletoData(AudioChannelDemoActivity.this);
                mFilePath = DemoUtil.getWavFilePath(AudioChannelDemoActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switchOutputChannel(false);
                        mTopInfoTextView.setText("初始化完成");
                        Log.i(TAG, "初始化完成");
                    }
                });
            }
        });
    }

    private void switchOutputChannel(boolean useSystem) {
        boolean targetUseSystem;
        if (AudioOutputChannelManager.getInstance().getSupportThirdAudioOutput()) {
            targetUseSystem = useSystem;
        } else {
            targetUseSystem = true;
        }
        if (AudioOutputChannelManager.getInstance().switchAudioOutput(targetUseSystem)) {
            mIsSystem = targetUseSystem;
        } else {
            return;
        }
        if (mIsSystem) {
            mSwitchAudioOutputBtn.setText("播放声道类型：系统，点击切换");
        } else {
            mSwitchAudioOutputBtn.setText("播放声道类型：第三方，点击切换");
        }
    }

    private void initListener() {
        mPlayKaraokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (karaPlaybackPlayer == null) {
                    mSaveMicPcmPath = DemoUtil.getMicFilePath(AudioChannelDemoActivity.this, System.currentTimeMillis() + ".wav");
                    PlayerConfig playerConfig = new PlayerConfig();
                    playerConfig.mediaPcmPath = mFilePath;
                    playerConfig.saveMicPcmPath = mSaveMicPcmPath;
                    playerConfig.useSystem = mIsSystem;
                    playerConfig.needSaveMicData = true;
                    playerConfig.isSearch = false;
                    playerConfig.mediaVol = mMediaVol/100f;
                    playerConfig.micVol = mMicVol/100;
                    karaPlaybackPlayer = new KaraPlaybackPlayer(playerConfig);
                    karaPlaybackPlayer.addOnProgressListener(mOnProgressListener);
                    karaPlaybackPlayer.init(mOnPreparedListener);
                }
            }
        });

        mVoiceSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (karaPlaybackPlayer == null) {
                    mSaveMicPcmPath = DemoUtil.getMicFilePath(AudioChannelDemoActivity.this, System.currentTimeMillis() + ".wav");
                    PlayerConfig playerConfig = new PlayerConfig();
                    playerConfig.mediaPcmPath = mFilePath;
                    playerConfig.saveMicPcmPath = mSaveMicPcmPath;
                    playerConfig.useSystem = mIsSystem;
                    playerConfig.needSaveMicData = true;
                    playerConfig.isSearch = true;
                    playerConfig.mediaVol = mMediaVol/100f;
                    playerConfig.micVol = mMicVol/100;
                    karaPlaybackPlayer = new KaraPlaybackPlayer(playerConfig);
                    karaPlaybackPlayer.addOnProgressListener(mOnProgressListener);
                    karaPlaybackPlayer.init(mOnPreparedListener);
                }
            }
        });

        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (karaPlaybackPlayer != null) {
                    karaPlaybackPlayer.pause();
                }
            }
        });

        mResumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (karaPlaybackPlayer != null) {
                    karaPlaybackPlayer.resume();
                }
            }
        });

        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (karaPlaybackPlayer != null) {
                    karaPlaybackPlayer.stop();
                }
            }
        });

        mSwitchAudioOutputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchOutputChannel(!mIsSystem);
            }
        });

        mPlaySavedMicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(mSaveMicPcmPath);
                if (file.exists()) {
                    if (karaPlaybackPlayer == null) {
                        PlayerConfig playerConfig = new PlayerConfig();
                        playerConfig.mediaPcmPath = mSaveMicPcmPath;
                        playerConfig.useSystem = true;
                        playerConfig.needSaveMicData = false;
                        playerConfig.isSearch = false;
                        playerConfig.mediaVol = mMediaVol/100f;
                        playerConfig.micVol = mMicVol/100f;
                        karaPlaybackPlayer = new KaraPlaybackPlayer(playerConfig);
                        karaPlaybackPlayer.addOnProgressListener(mOnProgressListener);
                        karaPlaybackPlayer.init(mOnPreparedListener);
                    }
                } else {
                    Toast.makeText(AudioChannelDemoActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGetDeviceTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioReceiver audioReceiver = AudioReceiverChannelManager.getInstance().getCurrentAudioReceiver();
                if (audioReceiver != null) {
                    int type = audioReceiver.getConnectedDeviceTypes();
                    mDeviceTypeView.setText("类型是 " + type);
                    audioReceiver.registerCallback(mAudioReceiverCallback);
                    if (type == AudioReceiver.DEIVCE_TYPE_PHONE) {
                        int[] ports = audioReceiver.getPreparedPhoneSocketPorts();
                        refreshPhoneServerAddress(ports);
                    }
                }
            }
        });

        mMediaVolUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMediaVol(true);
            }
        });

        mMediaVolDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMediaVol(false);
            }
        });

        mMicVolUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMicVol(true);
            }
        });

        mMicVolDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMicVol(false);
            }
        });
    }

    private void changeMediaVol(boolean up){
        if (up) {
            mMediaVol += 10;
            if (mMediaVol >= 100) {
                mMediaVol = 100;
            }
        } else {
            mMediaVol -= 10;
            if (mMediaVol <= 0) {
                mMediaVol = 0;
            }
        }
        AudioOutput output = AudioOutputChannelManager.getInstance().getCurrentAudioOutput();
        Log.i(TAG, "changeMediaVol output " + output + "  " + mMediaVol);
        if (output != null) {
            try {
                output.setVolume(mMediaVol / 100f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMediaVolView.setText(mMediaVol + "%");
    }

    private void changeMicVol(boolean up) {
        if (up) {
            mMicVol += 10;
            if (mMicVol >= 100) {
                mMicVol = 100;
            }
        } else {
            mMicVol -= 10;
            if (mMicVol <= 0) {
                mMicVol = 0;
            }
        }
        AudioOutput output = AudioOutputChannelManager.getInstance().getCurrentAudioOutput();
        Log.i(TAG, "changeMicVol output " + output + "  " + mMicVol);
        if (output != null) {
            try {
                output.setVolume(mMicVol / 100.f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMicVolView.setText(mMicVol + "%");
    }

    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(AudioParams info) {
            if (karaPlaybackPlayer != null) {
                karaPlaybackPlayer.start();
            }
        }
    };

    private OnProgressListener mOnProgressListener = new OnProgressListener() {
        @Override
        public void onProgressUpdate(final int now, int duration) {
            Log.i(TAG, "onProgressUpdate now " + now);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPlayTimeView.setText((now/1000) + " ");
                }
            });
        }

        @Override
        public void onComplete() {
            Log.i(TAG, "onStop");
            karaPlaybackPlayer = null;
        }

        @Override
        public void onStop() {
            Log.i(TAG, "onStop");
            karaPlaybackPlayer = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(mSaveMicPcmPath);
                    if (file.exists()) {
                        long length = file.length();
                        String info = " " + mSaveMicPcmPath + "  " + (length >> 8) + "  KB";
                        Log.i(TAG, "save mic info " + info);
                        mSavedMicInfoView.setText(info);
                    }
                }
            });
        }

        @Override
        public void onError(int ret) {
            Log.i(TAG, "onError  " + ret);
            karaPlaybackPlayer = null;
        }
    };

    private AudioReceiverCallback mAudioReceiverCallback = new AudioReceiverCallback() {
        @Override
        public void onAudioDataReceived(AudioFrame frame, int pos, float volume) {
            super.onAudioDataReceived(frame, pos, volume);
        }

        @Override
        public void onPhoneSocketPortsPrepared(final int[] ports) {
            super.onPhoneSocketPortsPrepared(ports);
            refreshPhoneServerAddress(ports);
        }
    };

    private void refreshPhoneServerAddress(final int[] ports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String ip = DemoUtil.getIp(AudioChannelDemoActivity.this);
                String portstr = "";
                if (ports != null) {
                    for (int i = 0 ; i < ports.length ; i++) {
                        portstr+=ports[i] + ", ";
                    }
                }
                Log.i(TAG, "onPhoneSocketPortsPrepared ip:" + ip);
                Log.i(TAG, "onPhoneSocketPortsPrepared ports:" + portstr);
                mIpView.setText("ip:" + ip);
                mPortsView.setText("ports:" + portstr);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (karaPlaybackPlayer != null) {
            karaPlaybackPlayer.stop();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_B) {
            if (karaPlaybackPlayer != null) {
                karaPlaybackPlayer.stop();
            }
            mTopInfoTextView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 500);
        }
        return super.onKeyDown(keyCode, event);
    }
}
