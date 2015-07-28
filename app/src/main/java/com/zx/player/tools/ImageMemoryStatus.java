package com.zx.player.tools;

import android.view.View;

import java.util.List;

/**
 * Created by niuniuzhang on 15/7/21.
 */
public class ImageMemoryStatus{
    public long mBitmapsSize;
    public List<BitmapStatus> mBitmapStatus;
    public long mImageBytesSize;
    public List<ImageBytesStatus> mImageBytesStatus;

    public static class BitmapStatus{
        public List<View> mViews;
        public List<String> mActivitys;
        public String mUrl;
        public int mDisplayMode;
        public long mSize;
    }

    public static class ImageBytesStatus{
        public String mUrl;
        public long mSize;
    }
}