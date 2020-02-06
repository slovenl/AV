package com.sloven.splayer;


import android.app.Activity;
import android.os.Bundle;

import com.sloven.player.SPlayer;

public class MainActivity extends Activity {
    private SPlayer mPlayer = new SPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayer.start();
    }
}
