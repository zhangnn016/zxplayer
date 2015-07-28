package com.zx.player.tools;

/**
 * 内存使用状态警告和bitmap状态通知
 * Created by niuniuzhang on 15/7/21.
 */
public interface ImageMemoryStateListener {

    public void onMemoryOverflow(long maxSize, long currentSize);

    public void onAllocateBitmap(String url, long size);

    public void onReleaseBitmap(String url, long size);

    public void onImageBytesFilled(String url, long size);

    public void onImageBytesReleased(String url, long size);
}
