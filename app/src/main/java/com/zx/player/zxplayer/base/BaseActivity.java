package com.zx.player.zxplayer.base;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * 基类activity
 * 提供一些基本功能，如页面中心转菊花等
 * Created by niuniuzhang on 15/7/28.
 */
public abstract class BaseActivity extends Activity {

    protected ActionBar mActionBar;
    protected FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        mActionBar = getActionBar();
        mFragmentManager = getFragmentManager();
        initView();
    }

    protected void setFragment(int layoutResId, Fragment fragment) {
        mFragmentManager.beginTransaction().replace(layoutResId, fragment).commit();
    }

    protected abstract void initView();

    protected abstract int getLayoutResourceId();
}
