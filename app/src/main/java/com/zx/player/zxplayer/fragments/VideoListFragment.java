package com.zx.player.zxplayer.fragments;

import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import com.zx.player.zxplayer.R;
import com.zx.player.zxplayer.adapters.VideoListAdapter;
import com.zx.player.zxplayer.base.BaseFragment;
import com.zx.player.zxplayer.objects.FileInfoObject;

import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * 主页的视频列表fragment
 * Created by leon on 15/8/9.
 */
public class VideoListFragment extends BaseFragment {
    private static final String TAG = VideoListFragment.class.getSimpleName();

    private PtrClassicFrameLayout mRefreshBase;
    private ListView mListView;
    private VideoListAdapter mListAdapter;
    private List<FileInfoObject> mDataList;

    @Override
    protected int getResourceLayout() {
        return R.layout.fragment_home_videolist;
    }

    @Override
    protected void initViews(View baseView) {
        mRefreshBase = (PtrClassicFrameLayout)baseView.findViewById(R.id.ptr_main);
        mListView = (ListView)baseView.findViewById(R.id.list_video);

        mRefreshBase.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                mRefreshBase.refreshComplete();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshBase.autoRefresh();
            }
        }, 100);
    }
}
