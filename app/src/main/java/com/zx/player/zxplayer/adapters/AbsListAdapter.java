package com.zx.player.zxplayer.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有adapter的基类
 * Created by leon on 15/8/9.
 */
public abstract class AbsListAdapter<T> extends BaseAdapter {
    protected List<T> mDataList;

    protected ListView mListView;

    protected Activity mContext;

    public AbsListAdapter(Activity activity) {
        mContext = activity;
        mDataList = new ArrayList<T>();
    }

    public AbsListAdapter(Activity activity, List<T> list) {
        mContext = activity;
        mDataList = list;
    }

    public void setList(List<T> list) {
        setCopyList(list);
    }

    public void setCopyList(List<T> list) {
        if (list != null && list.size() > 0) {
            if (mDataList == null) {
                mDataList = new ArrayList<T>();
            }
            mDataList.clear();
            mDataList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addToBack(List<T> list) {
        if (list != null && list.size() > 0) {
            if (mDataList == null) {
                mDataList = new ArrayList<T>();
            }
            mDataList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addToBack(T data) {
        if (data != null) {
            if (mDataList == null) {
                mDataList = new ArrayList<T>();
            }
            mDataList.add(data);
            notifyDataSetChanged();
        }
    }

    public List<T> getDataList() {
        return mDataList;
    }

    @Override
    public int getCount() {
        if (mDataList == null) {
            return 0;
        } else {
            return mDataList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mDataList == null ? null : mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    public Activity getActivity() {
        return mContext;
    }

    public void setListView(ListView lv) {
        mListView = lv;
    }

    public ListView getListView() {
        return mListView;
    }
}
