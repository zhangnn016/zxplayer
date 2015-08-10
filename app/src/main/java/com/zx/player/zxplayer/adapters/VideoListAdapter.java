package com.zx.player.zxplayer.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zx.player.zxplayer.R;
import com.zx.player.zxplayer.objects.FileInfoObject;
import com.zx.player.zxplayer.viewholders.AbsViewHolder;
import com.zx.player.zxplayer.viewholders.FileInfoViewHolder;

import java.util.List;

/**
 * 主页面视频列表adapter
 * Created by leon on 15/8/9.
 */
public class VideoListAdapter extends AbsListAdapter<FileInfoObject> {

    public VideoListAdapter(Activity activity) {
        super(activity);
    }

    public VideoListAdapter(Activity activity, List<FileInfoObject> list) {
        super(activity, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileInfoObject object = (FileInfoObject)getItem(position);
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
