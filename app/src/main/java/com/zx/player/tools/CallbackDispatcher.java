package com.zx.player.tools;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * 回调分发
 * Created by niuniuzhang on 15/7/23.
 */
public class CallbackDispatcher implements Drawable.Callback{

    private List<MagicianDrawable> mBindedMagicianDrawables = new ArrayList<MagicianDrawable>();

    public void removeMagicianDrawable(MagicianDrawable drawable){
        mBindedMagicianDrawables.remove(drawable);
    }

    public void addMagicianDrawable(MagicianDrawable drawable){
        mBindedMagicianDrawables.add(drawable);
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        for(MagicianDrawable drawable:mBindedMagicianDrawables){
            drawable.invalidate();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
    }
}
