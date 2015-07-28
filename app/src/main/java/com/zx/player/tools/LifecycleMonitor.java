package com.zx.player.tools;


/**
 * 生命周期监控器，用于监控Activity生命周期
 * android 4.0系统后Application提供了Activity生命周期监听功能。为了提供android 4.0前系统前功能，提供统一监听接口
 * Created by niuniuzhang on 15/7/21.
 */

public interface LifecycleMonitor {
    public static final String LIFECYCLE_ARTIFACT = "LIFECYCLE";
    /**
     * 注册Activity 生命周期监听，兼容 ICE_CREAM_SANDWICH（14） 以前版本
     * @param callback	生命周期回调
     */
    @SuppressWarnings("UnusedDeclaration")
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback);

    /**
     * 注销Activity 生命周期监听，兼容 ICE_CREAM_SANDWICH（14） 以前版本
     * @param callback	生命周期回调
     */
    @SuppressWarnings("UnusedDeclaration")
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback);

    /**
     * 注册APP状态监听
     * @param listener	APP状态回调
     */
    public void registerAppStateListener(APPStateListener listener);

    /**
     * 注销APP状态监听
     * @param listener	APP状态回调
     */
    public void unregisterAppStateListener(APPStateListener listener);

    // app当前状态
    public interface APPStateListener {
        //APP进入后台
        public void onEnterBackground();

        //APP进入前台
        public void onEnterForeground();
    }
}

