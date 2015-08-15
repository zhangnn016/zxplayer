package com.zx.player.zxplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.zx.player.utils.ZXToast;
import com.zx.player.zxplayer.R;
import com.zx.player.zxplayer.base.BaseActivity;
import com.zx.player.zxplayer.consts.ActionConsts;
import com.zx.player.zxplayer.fragments.SecretListFragment;
import com.zx.player.zxplayer.fragments.SettingsFragment;
import com.zx.player.zxplayer.fragments.VideoListFragment;

/**
 * 主页面
 * 不做任何数据相关的工作，只负责展示fragment
 */

public class HomeActivity extends BaseActivity {

    LocalBroadcastManager mBroadcastManager;

    private VideoListFragment mVideoListFragment;
    private SecretListFragment mSecretListFragment;
    private SettingsFragment mSettingsFragment;

    private View mBottomVideoList;
    private View mBottomSecretList;
    private View mBottomSettings;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActionConsts.ACTION_HOME_ACTIVITY.equals(intent.getAction())) {
                ZXToast.showToast("Got the broadcast");
            }
        }
    };

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }

    protected void initView() {
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActionConsts.ACTION_HOME_ACTIVITY);
        mBroadcastManager.registerReceiver(mReceiver, filter);

        mVideoListFragment = new VideoListFragment();
        mSecretListFragment = new SecretListFragment();
        mSettingsFragment = new SettingsFragment();

        setFragment(R.id.layout_container, mVideoListFragment);

        mBottomVideoList = findViewById(R.id.ll_video_list);
        mBottomSecretList = findViewById(R.id.ll_secret_list);
        mBottomSettings = findViewById(R.id.ll_settings);

        mBottomVideoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(R.id.layout_container, mVideoListFragment);
            }
        });

        mBottomSecretList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(R.id.layout_container, mSecretListFragment);
            }
        });

        mBottomSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(R.id.layout_container, mSettingsFragment);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastManager != null) {
            mBroadcastManager.unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
