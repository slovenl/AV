package com.ywl5320.wllivepusher.push;

public interface WlConnectListenr {

    void onConnecting();

    void onConnectSuccess();

    void onConnectFail(String msg);

}
