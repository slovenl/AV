package com.sloven.player;

public interface IPlayer {

    void setDataSource(SDataSource data);

    void prepare();

    void start();

    void pause();

    void seek(long pos);

    void stop();

    void release();
}
