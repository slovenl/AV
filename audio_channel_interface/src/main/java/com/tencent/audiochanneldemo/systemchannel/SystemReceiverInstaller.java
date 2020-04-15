package com.tencent.audiochanneldemo.systemchannel;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.tencent.audiochanneldemo.DemoApplication;
import com.tencent.karaoketv.audiochannel.AudioParams;
import com.tencent.karaoketv.audiochannel.AudioReceiver;
import com.tencent.karaoketv.audiochannel.AudioReceiverInstaller;

/**
 * Created by zoroweili on 2019-2-26.
 */

public class SystemReceiverInstaller extends AudioReceiverInstaller{
    @Override
    protected AudioReceiver onCreateAudioReceiver(AudioParams audioParams) {
        SystemAudioReceiver systemAudioReceiver = new SystemAudioReceiver();
        systemAudioReceiver.init(audioParams);
        return systemAudioReceiver;
    }

    @Override
    protected boolean onCheckInstallerEnable() {
        return true;
    }

    @Override
    protected boolean onInstall() {
        try {
            super.onInstall();
            int permission = ActivityCompat.checkSelfPermission(DemoApplication.getApplication(),
                Manifest.permission.RECORD_AUDIO);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                notifyOnInstall(1002);
                return false;
            } else {
                notifyOnInstall(1001);
                return true;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        notifyOnInstall(1003);
        return false;
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
