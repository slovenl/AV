package com.sloven.player;

public class SPlayer implements IPlayer{
    private long  mNativeSPlayer;

    @Override
    public void setDataSource(SDataSource data) {
        _setDataSource(data);
    }

    @Override
    public void start() {
        _start();
    }

    @Override
    public void pause() {
        _pause();
    }

    @Override
    public void seek(long pos) {
        _seek(pos);
    }

    @Override
    public void stop() {
        _stop();
    }

    @Override
    public void release() {
        _release();
    }

    private native void _setDataSource(SDataSource data);
    private native void _start();
    private native void _pause();
    private native void _seek(long pos);
    private native void _stop();
    private native void _release();

    static{
        System.loadLibrary("player_jni");
    }
}
