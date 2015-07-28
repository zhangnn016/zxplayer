package com.zx.player.zxplayer.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.zx.player.zxplayer.base.LogUtils;

/**
 * 处理activity生命周期监控
 * 可以看到当前打开了哪个Activity
 * Created by niuniuzhang on 15/7/28
 */
public class ZXActivityLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = ZXActivityLifecycleCallback.class.getSimpleName();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LogUtils.d(TAG, "onActivityCreated, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LogUtils.d(TAG, "onActivityStarted, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        LogUtils.d(TAG, "onActivityResumed, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LogUtils.d(TAG, "onActivityPaused, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LogUtils.d(TAG, "onActivityStopped, " + activity.getLocalClassName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        LogUtils.d(TAG, "onActivitySaveInstanceState, " + activity.getLocalClassName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        LogUtils.d(TAG, "onActivityDestroyed, " + activity.getLocalClassName());
    }
}
