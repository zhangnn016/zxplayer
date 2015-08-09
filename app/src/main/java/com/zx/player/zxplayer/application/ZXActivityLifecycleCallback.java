package com.zx.player.zxplayer.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.zx.player.utils.ZXLog;

/**
 * 处理activity生命周期监控
 * 可以看到当前打开了哪个Activity
 * Created by niuniuzhang on 15/7/28
 */
public class ZXActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = ZXActivityLifecycleCallback.class.getSimpleName();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ZXLog.d(TAG, "onActivityCreated, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ZXLog.d(TAG, "onActivityStarted, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ZXLog.d(TAG, "onActivityResumed, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ZXLog.d(TAG, "onActivityPaused, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ZXLog.d(TAG, "onActivityStopped, " + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        ZXLog.d(TAG, "onActivitySaveInstanceState, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ZXLog.d(TAG, "onActivityDestroyed, " + activity.getLocalClassName());
    }
}
