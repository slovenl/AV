package com.sloven.player;

public class SPlayer implements IPlayer{
    private long  mNativeSPlayer;

    @Override
    public void setDataSource(SDataSource data) {
        native_setDataSource(data);
    }

    @Override
    public void start() {
        native_start();
    }

    @Override
    public void pause() {
        native_pause();
    }

    @Override
    public void seek(long pos) {
        native_seek(pos);
    }

    @Override
    public void stop() {
        native_stop();
    }

    @Override
    public void release() {
        native_release();
    }

    private native void native_setDataSource(SDataSource data);
    private native void native_start();
    private native void native_pause();
    private native void native_seek(long pos);
    private native void native_stop();
    private native void native_release();

    static{
        System.loadLibrary("player_jni");
    }
}
