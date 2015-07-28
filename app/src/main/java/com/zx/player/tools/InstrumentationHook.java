package com.zx.player.tools;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by niuniuzhang on 15/7/21.
 */
public class InstrumentationHook extends Instrumentation {

    private Instrumentation mSystemInstrumentation;
    public InstrumentationHook(Instrumentation instrumentation){
        mSystemInstrumentation = instrumentation;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl) Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivityCreatedCompat(activity, icicle);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnCreate(activity, icicle);
        else
            super.callActivityOnCreate(activity, icicle);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnDestroy(Activity activity) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl)Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivityDestroyedCompat(activity);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnDestroy(activity);
        else
            super.callActivityOnDestroy(activity);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnPause(Activity activity) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl)Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivityPausedCompat(activity);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnPause(activity);
        else
            super.callActivityOnPause(activity);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnResume(Activity activity) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl)Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivityResumedCompat(activity);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnResume(activity);
        else
            super.callActivityOnResume(activity);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl)Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivitySaveInstanceStateCompat(activity, outState);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnSaveInstanceState(activity, outState);
        else
            super.callActivityOnSaveInstanceState(activity, outState);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnStart(Activity activity) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl)Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivityStartedCompat(activity);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnStart(activity);
        else
            super.callActivityOnStart(activity);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void callActivityOnStop(Activity activity) {
        if(Build.VERSION.SDK_INT < 14){
//            ((LifecycleMonitorImpl)Doraemon.getArtifact(LifecycleMonitor.LIFECYCLE_ARTIFACT)).dispatchActivityStoppedCompat(activity);
        }
        if(mSystemInstrumentation != null)
            mSystemInstrumentation.callActivityOnStop(activity);
        else
            super.callActivityOnStop(activity);
    }

}
