package com.zx.player.zxplayer.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;

/**
 * Fragment的基类
 * Created by niuniuzhang on 15/7/28.
 */
public abstract class BaseFragment extends Fragment {

    protected View mView; // 根视图

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int resId = getResourceLayout();
        if (resId <= 0) {
            return null;
        }
        mView = inflater.inflate(resId, container, false);
        initViews(mView);
        return mView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected abstract int getResourceLayout();

    protected abstract void initViews(View baseView);
}
