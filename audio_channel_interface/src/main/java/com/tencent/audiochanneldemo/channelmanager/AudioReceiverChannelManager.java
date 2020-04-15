package com.tencent.audiochanneldemo.channelmanager;

import android.support.annotation.NonNull;
import android.util.Log;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioReceiver;
import com.tencent.karaoketv.audiochannel.AudioReceiverInstaller;
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

public class AudioReceiverChannelManager {
    private static final String TAG = "ReceiverChannelManager";
    private static final AudioReceiverChannelManager INSTANCE = new AudioReceiverChannelManager();
    //系统的输出和输入通道
    private AudioReceiverInstaller mSystemAudioReceiverInstaller;
    // 筛选出来的厂商和第三方的装载器
    private AudioReceiverInstaller mThirdAudioReceiverInstaller;

    // 各厂商或者第三方的输出和输入通道
    private final List<AudioReceiverInstaller> mThirdAudioReceiverInstallerList = new CopyOnWriteArrayList<>();

    @NonNull
    private AudioReceiverInstaller mCurrentAudioReceiverInstaller;
    private AudioReceiver mCurrentAudioReceiver;

    private boolean mIsSupportThirdOutput = false;

    public static AudioReceiverChannelManager getInstance() {
        return INSTANCE;
    }

    private AudioReceiverChannelManager() {}

    /**
     * 添加厂商或第三方的声音输出的装载器
     * @param installer
     * @return
     */
    public AudioReceiverChannelManager registerThirdAudioReceiverInstaller(AudioReceiverInstaller installer) {
        if (installer == null) {
            return this;
        }
        if (!mThirdAudioReceiverInstallerList.contains(installer)) {
            mThirdAudioReceiverInstallerList.add(installer);
        }
        return this;
    }

    AudioReceiverChannelManager setSystemAudioReceiverInstaller(AudioReceiverInstaller installer) {
        mSystemAudioReceiverInstaller = installer;
        return this;
    }

    public AudioReceiverChannelManager checkInstallers() {
        for (AudioReceiverInstaller outputInstaller : mThirdAudioReceiverInstallerList) {
            if (outputInstaller.checkInstallerEnable()) {
                mThirdAudioReceiverInstaller = outputInstaller;
                mIsSupportThirdOutput = true;
                break;
            }
        }
        return this;
    }

    public boolean getSupportThirdAudioReceiver() {
        return mIsSupportThirdOutput;
    }

    public final boolean isInstaller(Class<? extends AudioReceiverInstaller> clazz) {
        return clazz.isInstance(mCurrentAudioReceiverInstaller);
    }

    public final boolean isThirdInstaller(Class<? extends AudioReceiverInstaller> clazz) {
        return clazz.isInstance(mThirdAudioReceiverInstaller);
    }

    public boolean installAudioReceiver(boolean systemFirst) {
        if (systemFirst) {
            return switchAudioReceiver(true);
        } else {
            if (mIsSupportThirdOutput) {
                return switchAudioReceiver(false);
            } else {
                return switchAudioReceiver(true);
            }
        }
    }

    //todo
    public boolean switchAudioReceiver(boolean useSystem){
        AudioReceiverInstaller targetInstaller = useSystem ? mSystemAudioReceiverInstaller : mThirdAudioReceiverInstaller;
        // 如果没有目标installer，则失败
        if (targetInstaller == null) {
            Log.e(TAG, "target AudioReceiverInstaller is null, useSystem : " + useSystem);
            return false;
        }
        // 如果当前的installer就是目标installer，则不重复install了
        if (mCurrentAudioReceiverInstaller == targetInstaller) {
            return mCurrentAudioReceiverInstaller.isInstalled();
        }
        // 如果当前installer不为空，但是也不是目标的installer，则uninstall
        if (mCurrentAudioReceiverInstaller != null) {
            mCurrentAudioReceiverInstaller.unInstall();
        }
        mCurrentAudioReceiverInstaller = targetInstaller;
        Log.e(TAG, "switchAudioReceiver, mCurrentAudioReceiverInstaller : " + mCurrentAudioReceiverInstaller);
        return targetInstaller.install();
    }

    public AudioReceiver createAudioReceiver(AudioParams audioParams) {
        if (mCurrentAudioReceiverInstaller != null) {
            mCurrentAudioReceiver = mCurrentAudioReceiverInstaller.createAudioReceiver(audioParams);
            return mCurrentAudioReceiver;
        }
        return null;
    }

    public AudioReceiver getCurrentAudioReceiver() {
        return mCurrentAudioReceiver;
    }

    public void setInstallerListener(DeviceInstaller.InstallerListener installerListener) {
        Log.i(TAG, "setInstallerListener, installerListener " + installerListener + " ,  mThirdAudioReceiverInstaller " + mThirdAudioReceiverInstaller);
        if (mThirdAudioReceiverInstaller != null) {
            mThirdAudioReceiverInstaller.setInstallerListener(installerListener);
        }
    }
}
