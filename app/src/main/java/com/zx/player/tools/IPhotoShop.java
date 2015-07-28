package com.zx.player.tools;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

/**
 * 图片统一管理接口。
 * 客户端不再自己管理图片，只需要将图片的url交给PhotoShop即可，PhtotoShop自己管理图片的加载、缓存、释放等。
 * PhotoShop会实时监测内存使用情况，出现OOM风险时，会主动将不再使用的图片回收到更低层次的缓存当中。
 * PhotoShop负责将图片显示在View上。Photoshop异步完成图片的加载和绑定操作
 * 处理图片的加载、绑定、缓存等等策略
 *
 * 2015.07.20
 *
 * Created by niuniuzhang on 15/7/21.
 */
public interface IPhotoShop {

    /**
     * 设置imageView的drawable为url所对应的图片
     * @param imageView 目标控件
     * @param url drawable对应的图片url，当url传入为null时，将解除上次绑定。
     * @param parent view从属的AbsListView，传入Adapter getView方法的ViewGroup参数即可。如不属于AbsListView则传入null
     */
    public void setImageDrawable(ImageView imageView, String url, AbsListView parent);

    /**
     * 设置一个view的背景图片为url所对应的图片
     * @param view
     * @param url
     * @param parent
     */
    public void setImageBackground(View view, String url, AbsListView parent);

    public void add2Cache(Bitmap bitmap, byte[] image, String url);

    public void removeFromCache(String url);

    /**
     * 清除图片的memory缓存。
     * @param maxToKeep 最大保留的内存值，单位字节，小于等于0时将清除所有内存缓存
     */
    public void clearMemoryCache(long maxToKeep);

    /**
     * 获取当前图片内存的占用情况，大小为字节
     * @return
     */
    public long getMemorySize();

    /**
     * 注册图像处理状态监听器
     * @param listener
     */
    public void registerEventListener(ImageEventListener listener);

    /**
     * 注销监听
     * @param listener
     */
    public void unregisterEventListener(ImageEventListener listener);
}
