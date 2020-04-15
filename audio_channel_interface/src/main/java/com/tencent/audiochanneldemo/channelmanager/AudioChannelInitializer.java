package com.tencent.audiochanneldemo.channelmanager;

import android.util.Log;
import com.tencent.audiochanneldemo.systemchannel.SystemAudioReceiver;
import com.tencent.audiochanneldemo.systemchannel.SystemOutputInstaller;
import com.tencent.audiochanneldemo.systemchannel.SystemReceiverInstaller;

/**
 * Created by zoroweili on 2019-2-20.
 */

public class AudioChannelInitializer {
    public static void init() {
        Log.i("AudioChannelInitializer", "init");
        SystemOutputInstaller systemOutputInstaller = new SystemOutputInstaller();
        AudioOutputChannelManager.getInstance().setSystemAudioOutputInstaller(systemOutputInstaller)
            // TODO 请开发者将对应的AudioOutputInstaller注册进来
            //.registerThirdAudioOutputInstaller(bajinOutputInstaller)
            .checkInstallers();

        SystemReceiverInstaller systemReceiverInstaller = new SystemReceiverInstaller();
        AudioReceiverChannelManager.getInstance().setSystemAudioReceiverInstaller(systemReceiverInstaller)
            // TODO 请开发者将对应的AudioReceiverInstaller注册进来
            //.registerThirdAudioReceiverInstaller(bajinReceiverInstaller)
            .checkInstallers();
        Log.i("AudioChannelInitializer", "init  end");
    }
}
