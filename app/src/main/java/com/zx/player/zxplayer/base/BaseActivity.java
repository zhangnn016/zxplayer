package com.zx.player.zxplayer.base;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * 基类activity
 * 提供一些基本功能，如页面中心转菊花等
 * Created by niuniuzhang on 15/7/28.
 */
public class BaseActivity extends Activity {

    protected ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mActionBar = getActionBar();
    }
}
