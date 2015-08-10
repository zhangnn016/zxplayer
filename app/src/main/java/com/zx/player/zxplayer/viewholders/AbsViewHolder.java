package com.zx.player.zxplayer.viewholders;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import com.zx.player.zxplayer.objects.FileInfoObject;

/**
 * viewholder的基类
 * Created by leon on 15/8/9.
 */
public abstract class AbsViewHolder {
    protected Activity mActivity;
    protected ListView mListView;

    public AbsViewHolder(Activity activity) {
        mActivity = activity;
    }

    public final View createView(FileInfoObject obj, ListView lv) {
        mListView = lv;
        if (mActivity != null) {
            View view = mActivity.getLayoutInflater().inflate(getLayoutId(), null);
            initView(view, lv);
            refreshView(obj);
            return view;
        }
        return null;
    }

    public abstract int getLayoutId();

    public abstract void initView(View base, ListView lv);

    public abstract void refreshView(FileInfoObject object);
}
