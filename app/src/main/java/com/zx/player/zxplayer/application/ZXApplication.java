package com.zx.player.zxplayer.application;

import android.app.Application;

/**
 * Application类
 * Created by niuniuzhang on 15/7/28.
 */
public class ZXApplication extends Application {

    private static ZXApplication mApplication;

    // 提供Application实例
    public static ZXApplication getApplication() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        this.registerActivityLifecycleCallbacks(new ZXActivityLifecycleCallback());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
