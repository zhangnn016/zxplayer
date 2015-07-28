package com.zx.player.tools;

import android.app.Activity;
import android.os.Bundle;

/**
 * 监听activity生命周期接口，兼容老版本安卓
 * Created by niuniuzhang on 15/7/21.
 */
public abstract class ActivityLifecycleCallbacksCompat {
    public abstract void onActivityCreated(Activity activity, Bundle savedInstanceState);
    public abstract void onActivityStarted(Activity activity);
    public abstract void onActivityResumed(Activity activity);
    public abstract void onActivityPaused(Activity activity);
    public abstract void onActivityStopped(Activity activity);
    public abstract void onActivitySaveInstanceState(Activity    activity, Bundle outState);
    public abstract void onActivityDestroyed(Activity activity);
}
