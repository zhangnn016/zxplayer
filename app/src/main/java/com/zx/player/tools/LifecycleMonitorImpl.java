package com.zx.player.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by niuniuzhang on 15/7/21.
 */
public class LifecycleMonitorImpl implements LifecycleMonitor {
    private static final String TAG = "LifecycleMonitorImpl";
    private static final int ON_STOP_CHECK_DELAY = 200;

    private Context mContext;
    private final ArrayList<ActivityLifecycleCallbacksCompat>
            mActivityLifecycleCallbacksCompat = new ArrayList<ActivityLifecycleCallbacksCompat>();
    private Map<ActivityLifecycleCallbacksCompat,ActivityLifecycleCallbacksWrapper> mCallbacksMap;

    private final ArrayList<APPStateListener> mAPPStateListener = new ArrayList<APPStateListener>();

    /**
     * 记录调用了onResume的activity,onStop 200ms之后移除
     * 只会在主线程中被调用，故不会出现互斥问题
     */
    private Set<String> mResumeActivitys = new HashSet<String>();

    private Handler mHandler;

    protected LifecycleMonitorImpl(Context context) {
        mContext = context.getApplicationContext();
        if(Build.VERSION.SDK_INT < 14) {
            hookInstrumentation();
        }else{
            mCallbacksMap = new HashMap<ActivityLifecycleCallbacksCompat, ActivityLifecycleCallbacksWrapper>();
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksCompat() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                //第一个Activity onResumeed的时候,表明APP进入前台
                if (mResumeActivitys.size() == 0) {
                    synchronized (mAPPStateListener) {
                        for (APPStateListener listener : mAPPStateListener) {
                            listener.onEnterForeground();
                        }
                    }
                    PSLog.d(TAG, "=====> enter foreground");
                }

                String activityString = activity.getClass().getName()+activity.hashCode();
                mResumeActivitys.add(activityString);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                final String activityString = activity.getClass().getName()+activity.hashCode();
                /**
                 * 这里延迟是防止Activity切换时，mResumeActivitys size为0导致误报进入后台
                 */
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mResumeActivitys.remove(activityString);
                        if (mResumeActivitys.size() == 0) {
                            synchronized (mAPPStateListener) {
                                for (APPStateListener listener : mAPPStateListener) {
                                    listener.onEnterBackground();
                                }
                            }

                            PSLog.d(TAG, "=====> enter background");
                        }
                    }
                }, ON_STOP_CHECK_DELAY);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 注册Activity 生命周期监听，兼容 ICE_CREAM_SANDWICH（14） 以前版本
     * @param callback	回调
     */
    @SuppressLint("NewApi")
    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback){
        if(callback == null)
            return;
        if(Build.VERSION.SDK_INT < 14){
            synchronized (mActivityLifecycleCallbacksCompat) {
                mActivityLifecycleCallbacksCompat.add(callback);
            }
        }else{
            if(!mCallbacksMap.containsKey(callback)) {
                ActivityLifecycleCallbacksWrapper realCallback = new ActivityLifecycleCallbacksWrapper(callback);
                mCallbacksMap.put(callback,realCallback);
                ((Application)mContext).registerActivityLifecycleCallbacks(realCallback);
            }
        }
    }

    /**
     * 注销Activity 生命周期监听，兼容 ICE_CREAM_SANDWICH（14） 以前版本
     * @param callback	回调
     */
    @SuppressLint("NewApi")
    @Override
    public synchronized void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacksCompat callback) {
        if(callback == null)
            return;
        if(Build.VERSION.SDK_INT < 14){
            synchronized (mActivityLifecycleCallbacksCompat) {
                mActivityLifecycleCallbacksCompat.remove(callback);
            }
        }else{
            ActivityLifecycleCallbacksWrapper realCallback = mCallbacksMap.get(callback);
            if(realCallback != null) {
                mCallbacksMap.remove(callback);
                ((Application) mContext).unregisterActivityLifecycleCallbacks(realCallback);
            }
        }
    }

    @Override
    public void registerAppStateListener(APPStateListener listener) {
        synchronized (mAPPStateListener) {
            mAPPStateListener.add(listener);
        }
    }

    @Override
    public void unregisterAppStateListener(APPStateListener listener) {
        synchronized (mAPPStateListener) {
            mAPPStateListener.remove(listener);
        }
    }

    private void hookInstrumentation(){
        try {
            Class<?> activityThreadC = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadM = activityThreadC.getDeclaredMethod("currentActivityThread");
            Field instrumentationF = activityThreadC.getDeclaredField("mInstrumentation");
            instrumentationF.setAccessible(true);
            Object at = currentActivityThreadM.invoke(null);
            instrumentationF.set(at,new InstrumentationHook((Instrumentation)instrumentationF.get(at)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    protected final void dispatchActivityCreatedCompat(Activity activity, Bundle savedInstanceState){
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivityCreated(activity, savedInstanceState);
        }
    }

    protected final void dispatchActivityStartedCompat(Activity activity) {
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivityStarted(activity);
        }
    }

    protected final void dispatchActivityResumedCompat(Activity activity) {
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivityResumed(activity);
        }
    }

    protected final void dispatchActivityPausedCompat(Activity activity) {
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivityPaused(activity);
        }
    }

    protected final void dispatchActivityStoppedCompat(Activity activity) {
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivityStopped(activity);
        }
    }

    protected final void dispatchActivitySaveInstanceStateCompat(Activity activity, Bundle outState) {
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivitySaveInstanceState(activity, outState);
        }
    }

    protected final void dispatchActivityDestroyedCompat(Activity activity) {
        Object[] callbacks;
        synchronized (mActivityLifecycleCallbacksCompat) {
            callbacks = mActivityLifecycleCallbacksCompat.toArray();
        }
        if(callbacks.length > 0){
            for(Object callback:callbacks)
                ((ActivityLifecycleCallbacksCompat)callback).onActivityDestroyed(activity);
        }
    }

}
