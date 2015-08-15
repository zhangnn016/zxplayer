package com.zx.player.zxplayer.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.zx.player.zxplayer.objects.ObjectFileInfo;
import com.zx.player.zxplayer.viewholders.AbsViewHolder;
import com.zx.player.zxplayer.viewholders.FileInfoViewHolder;

import java.util.List;

/**
 * 主页面视频列表adapter
 * Created by leon on 15/8/9.
 */
public class VideoListAdapter extends AbsListAdapter<ObjectFileInfo> {

    public VideoListAdapter(Activity activity) {
        super(activity);
    }

    public VideoListAdapter(Activity activity, List<ObjectFileInfo> list) {
        super(activity, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ObjectFileInfo object = (ObjectFileInfo)getItem(position);
        AbsViewHolder holder;
        if (convertView == null) {
            holder = new FileInfoViewHolder(mContext);
            convertView = holder.createView(object, getListView());
            convertView.setTag(holder);
        } else {
            holder = (AbsViewHolder) convertView.getTag();
        }

        holder.refreshView(object);

        return convertView;
    }
}
