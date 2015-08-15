package com.zx.player.zxplayer.viewholders;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zx.player.tools.PhotoShop;
import com.zx.player.utils.TimeUtils;
import com.zx.player.zxplayer.R;
import com.zx.player.zxplayer.objects.ObjectFileInfo;

/**
 * 文件列表的viewHolder
 * Created by leon on 15/8/9.
 */
public class FileInfoViewHolder extends AbsViewHolder {

    private PhotoShop mPhotoshop;

    private ImageView mIvThumb;
    private TextView mTvTitle;
    private TextView mTvInfo;

    public FileInfoViewHolder(Activity activity) {
        super(activity);
        mPhotoshop = PhotoShop.getInstance(activity);
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_list_video_item;
    }

    @Override
    public void initView(View base, ListView lv) {
        mIvThumb = (ImageView)base.findViewById(R.id.iv_thumbnail);
        mTvTitle = (TextView)base.findViewById(R.id.tv_title);
        mTvInfo = (TextView)base.findViewById(R.id.tv_info);
    }

    @Override
    public void refreshView(ObjectFileInfo object) {
        if (object != null) {
            mTvTitle.setText(object.fileName);
            mTvInfo.setText(TimeUtils.formatDuration(object.duration));

            if (!TextUtils.isEmpty(object.thumbPath)) {
                mPhotoshop.setImageDrawable(mIvThumb, object.thumbPath, mListView);
            }
        }
    }
}
