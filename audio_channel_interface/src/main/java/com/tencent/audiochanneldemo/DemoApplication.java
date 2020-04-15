package com.tencent.audiochanneldemo;

import android.app.Application;

/**
 * Created by zoroweili on 2019-2-26.
 */

public class DemoApplication extends Application{
    private static DemoApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }

    public static final DemoApplication getApplication() {
        return sApplication;
    }
}
