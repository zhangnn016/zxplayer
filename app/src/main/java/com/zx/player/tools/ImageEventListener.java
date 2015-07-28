package com.zx.player.tools;

import android.view.View;

/**
 *  image状态监听器，监听ImageMagician内部状态变化
 *  提供以下状态通知
 *      1、图片解码分配内存。通知方法：{@link #onImageProcessListener(int, View, String, long)}
 *      2、图片释放回收内存。通知方法：{@link #onImageProcessListener(int, View, String, long)}
 *      3、内部错误。通知方法：{@link #onError(int, String, String, View)}
 *      4、图片下载请求通知。通知方法：{@link #onError(int, String, String, View)}
 *      5、图片下载进度通知。通知方法：{@link #onDownloadProgressListener(View, int)}
 *      6、图片
 *
 * Created by niuniuzhang on 15/7/21.
 */

public interface ImageEventListener {
    /**
     * 图片过大，无法解码错误
     */
    public static final int ERR_IMAGE_TOO_LARGE = -1;

    /**
     * 下载出错
     */
    public static final int ERR_DOWNLOAD_FAILED = -2;

    /**
     * 下载请求Waiting状态
     */
//    public static final int EVENT_WAITING = 0x01;

    /**
     * 下载请求Started状态
     */
    public static final int EVENT_DOWNLOAD_STARTED = 0x02;

    /**
     * 下载请求Completed状态
     */
    public static final int EVENT_DOWNLOAD_COMPLETED = 0x03;

    /**
     * 解码进入内存
     */
    public static final int EVENT_DECODE_BITMAP_INTO_MEM = 0x04;

    /**
     * 释放图片内存
     */
    public static final int EVENT_BITMAP_RELEASED = 0x05;

    /**
     * 图片数据存入内存
     */
    public static final int EVENT_FILL_IMAGEBYTES_INTO_MEM = 0x06;

    /**
     * 释放图片数据出内存
     */
    public static final int EVENT_IMAGEBYTES_RELEASED = 0x07;

    /**
     * 图片decode出现oom时的异常信息
     * @param errCode       错误码，{@link #ERR_IMAGE_TOO_LARGE ERR_IMAGE_TOO_LARGE}和{@link #ERR_DOWNLOAD_FAILED ERR_DOWNLOAD_FAILED}
     * @param errDes        错误描述
     */
    public void onError(int errCode, String errDes, String url, View view);

    /**
     * 图片请求状态监听
     * @param event     事件类型
     * @param view		图片对应的view，{@link #EVENT_DECODE_BITMAP_INTO_MEM EVENT_DECODE_BITMAP_INTO_MEM}、{@link #EVENT_BITMAP_RELEASED EVENT_BITMAP_RELEASED}、
     *                  {@link #EVENT_FILL_IMAGEBYTES_INTO_MEM EVENT_FILL_IMAGEBYTES_INTO_MEM}和{@link #EVENT_IMAGEBYTES_RELEASED EVENT_IMAGEBYTES_RELEASED}事件该字段为空
     * @param url		图片对应的url
     * @param size      图片对应的大小，{@link #EVENT_DECODE_BITMAP_INTO_MEM EVENT_DECODE_BITMAP_INTO_MEM}和{@link #EVENT_BITMAP_RELEASED EVENT_BITMAP_RELEASED}事件
     *                  对应Bitmap占用的内存大小，{@link #EVENT_DOWNLOAD_COMPLETED EVENT_COMPLETED}事件对应为下载大小，其他事件都为0
     */
    public void onImageProcessListener(int event, View view, String url, long size);

    /**
     * 下载进度监听
     * @param view      对应的view
     * @param progress	下载进度，下载进度从0-100。
     */
    public void onDownloadProgressListener(View view, int progress);

}
