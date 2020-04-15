package com.tencent.audiochanneldemo.systemchannel;

import com.tencent.karaoketv.audiochannel.AudioOutput;
import com.tencent.karaoketv.audiochannel.AudioOutputInstaller;
import com.tencent.karaoketv.audiochannel.AudioParams;

/**
 * Created by zoroweili on 2019-2-14.
 */

public class SystemOutputInstaller extends AudioOutputInstaller {

    @Override
    protected boolean onCheckInstallerEnable() {
        return true;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    protected AudioOutput onCreateAudioOutput(AudioParams params) {
        SystemAudioOutput systemAudioOutput = new SystemAudioOutput();
        systemAudioOutput.init(params);
        return systemAudioOutput;
    }

    @Override
    protected boolean onInstall() {
        try {
            super.onInstall();
            notifyOnInstall(1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return true;
    }

    @Override
    protected boolean onUninstall() {
        try {
            super.onUninstall();
            notifyOnUnInstall(1);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return true;
    }
}
