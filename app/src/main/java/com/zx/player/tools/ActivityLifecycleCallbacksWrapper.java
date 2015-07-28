package com.zx.player.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

/**
 * 包装类
 * Created by niuniuzhang on 15/7/21.
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ActivityLifecycleCallbacksWrapper  implements Application.ActivityLifecycleCallbacks {
    private ActivityLifecycleCallbacksCompat mAppCallbacke;

    public ActivityLifecycleCallbacksWrapper(ActivityLifecycleCallbacksCompat callable) {
        mAppCallbacke = callable;
    }

    @Override
    public void onActivityCreated(Activity paramActivity, Bundle paramBundle) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivityCreated(paramActivity, paramBundle);
    }

    @Override
    public void onActivityStarted(Activity paramActivity) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivityStarted(paramActivity);
    }

    @Override
    public void onActivityResumed(Activity paramActivity) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivityResumed(paramActivity);
    }

    @Override
    public void onActivityPaused(Activity paramActivity) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivityPaused(paramActivity);
    }

    @Override
    public void onActivityStopped(Activity paramActivity) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivityStopped(paramActivity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity paramActivity, Bundle paramBundle) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivitySaveInstanceState(paramActivity, paramBundle);
    }

    @Override
    public void onActivityDestroyed(Activity paramActivity) {
        if (mAppCallbacke != null)
            mAppCallbacke.onActivityDestroyed(paramActivity);
    }
}
