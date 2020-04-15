package com.tencent.audiochanneldemo.channelmanager;

import android.support.annotation.NonNull;
import android.util.Log;
import com.tencent.karaoketv.audiochannel.AudioOutput;
import com.tencent.karaoketv.audiochannel.AudioOutputInstaller;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioPlayState;
import com.tencent.karaoketv.audiochannel.DeviceInstaller;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by zoroweili on 2019-2-19.
 *  * 管理器需要对外提供几个功能：
 1. 业务层真正使用通道的时候，通过管理器来使用，或者从管理区获取通道来自己直接使用
 2. 多个备选通道，和系统通道，需要加入到管理器来，相当于通道的注册过程
 3. 打开通道
 4. 切换通道

 管理器需要实现功能：
 1. 通道的选择，需要判断是否可用，相当于一个初始化的过程
 2. 要支持选择系统输出，其实只需要在打开的时候传一个参数，确定是否使用系统的通道
 */

public class AudioOutputChannelManager implements AudioPlayState {
    private static final String TAG = "OutputChannelManager";
    private static final AudioOutputChannelManager INSTANCE = new AudioOutputChannelManager();
    //系统的输出和输入通道
    private AudioOutputInstaller mSystemAudioOutputInstaller;
    // 筛选出来的厂商和第三方的装载器
    private AudioOutputInstaller mThirdAudioOutputInstaller;

    // 各厂商或者第三方的输出和输入通道
    private final List<AudioOutputInstaller> mThirdAudioOutputInstallerList = new CopyOnWriteArrayList<>();

    @NonNull
    private AudioOutputInstaller mCurrentAudioOutputInstaller;
    private AudioOutput mCurrentAudioOutput;

    //TODO
    private boolean mIsSupportThirdOutput = false;

    public static AudioOutputChannelManager getInstance() {
        return INSTANCE;
    }

    private AudioOutputChannelManager() {}

    /**
     * 添加厂商或第三方的声音输出的装载器
     * @param installer
     * @return
     */
    public AudioOutputChannelManager registerThirdAudioOutputInstaller(AudioOutputInstaller installer) {
        if (installer == null) {
            return this;
        }
        if (!mThirdAudioOutputInstallerList.contains(installer)) {
            mThirdAudioOutputInstallerList.add(installer);
        }
        return this;
    }

    AudioOutputChannelManager setSystemAudioOutputInstaller(AudioOutputInstaller installer) {
        mSystemAudioOutputInstaller = installer;
        return this;
    }

    public AudioOutputChannelManager checkInstallers() {
        for (AudioOutputInstaller outputInstaller : mThirdAudioOutputInstallerList) {
            if (outputInstaller.checkInstallerEnable()) {
                mThirdAudioOutputInstaller = outputInstaller;
                mIsSupportThirdOutput = true;
                break;
            }
        }
        return this;
    }

    public boolean getSupportThirdAudioOutput() {
        return mIsSupportThirdOutput;
    }

    public final boolean isInstaller(Class<? extends AudioOutputInstaller> clazz) {
        return clazz.isInstance(mCurrentAudioOutputInstaller);
    }

    public final boolean isThirdInstaller(Class<? extends AudioOutputInstaller> clazz) {
        return clazz.isInstance(mThirdAudioOutputInstaller);
    }

    public boolean installAudioOutput(boolean systemFirst) {
        if (systemFirst) {
            return switchAudioOutput(true);
        } else {
            if (mIsSupportThirdOutput) {
                return switchAudioOutput(false);
            } else {
                return switchAudioOutput(true);
            }
        }
    }

    public boolean switchAudioOutput(boolean useSystem){
        AudioOutputInstaller targetInstaller = useSystem ? mSystemAudioOutputInstaller : mThirdAudioOutputInstaller;
        // 如果没有目标installer，则失败
        if (targetInstaller == null) {
            Log.e(TAG, "target AudioOutputInstaller is null, useSystem : " + useSystem);
            return false;
        }
        // 如果当前的installer就是目标installer，则不重复install了
        if (mCurrentAudioOutputInstaller == targetInstaller) {
            return mCurrentAudioOutputInstaller.isInstalled();
        }
        // 如果当前installer不为空，但是也不是目标的installer，则uninstall
        if (mCurrentAudioOutputInstaller != null) {
            mCurrentAudioOutputInstaller.unInstall();
        }
        mCurrentAudioOutputInstaller = targetInstaller;
        Log.e(TAG, "switchAudioOutput, mCurrentAudioOutputInstaller : " + mCurrentAudioOutputInstaller);
        return targetInstaller.install();
    }

    public AudioOutput createAudioOutput(AudioParams audioParams) {
        if (mCurrentAudioOutputInstaller != null) {
            mCurrentAudioOutput = mCurrentAudioOutputInstaller.createAudioOutput(audioParams);
            return mCurrentAudioOutput;
        }
        return null;
    }

    public AudioOutput getCurrentAudioOutput() {
        return mCurrentAudioOutput;
    }

    public void setInstallerListener(DeviceInstaller.InstallerListener installerListener) {
        Log.i(TAG, "setInstallerListener, installerListener " + installerListener + " ,  mThirdAudioOutputInstaller " + mThirdAudioOutputInstaller);
        if (mThirdAudioOutputInstaller != null) {
            mThirdAudioOutputInstaller.setInstallerListener(installerListener);
        }
    }
}
