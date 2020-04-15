package com.tencent.audiochanneldemo.player;

/**
 * Created by zoroweili on 2019-2-24.
 */

public class PlayerConfig {
    public String mediaPcmPath;
    public String saveMicPcmPath;
    public boolean useSystem;
    public boolean needSaveMicData;
    public boolean isSearch;
    public float mediaVol;
    public float micVol;

    public void copyFrom(PlayerConfig playerConfig) {
        if (playerConfig != null) {
            mediaPcmPath = playerConfig.mediaPcmPath;
            saveMicPcmPath = playerConfig.saveMicPcmPath;
            useSystem = playerConfig.useSystem;
            needSaveMicData = playerConfig.needSaveMicData;
            isSearch = playerConfig.isSearch;
            mediaVol = playerConfig.mediaVol;
            micVol = playerConfig.micVol;
        }
    }
}
