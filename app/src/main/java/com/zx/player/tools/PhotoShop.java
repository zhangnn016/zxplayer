package com.zx.player.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * PhotoShop实现类
 * Created by niuniuzhang on 15/7/21.
 */
public class PhotoShop extends ActivityLifecycleCallbacksCompat implements IPhotoShop, LifecycleMonitor.APPStateListener {

    public static final String TAG = PhotoShop.class.getSimpleName();

    protected static final int TAG_BACKGROUND_KEY = 0x20120718;
    protected static final int TAG_DRAWABLE_KEY = 0x19561111;
    private static final int TAG_BACKGROUND = 0x19861103;
    private static final int TAG_DRAWABLE = 0X19820625;
    protected static final int TAG_PARENT = 0x19561212;
    private static final int TAG_BACKGROUND_PRIMITIVE = 0x20150304;
    private static final int TAG_BACKGROUND_CACHED = 0x20100315;
    private static final int TAG_DRAWABLE_PRIMITIVE = 0x20150308;
    private static final int TAG_DRAWABLE_CACHED = 0x20140617;
    private static final int TAG_DISPLAY_MODE_BKG = 0x20150306;
    private static final int TAG_DISPLAY_MODE_DWB = 0x20150307;

    // 缓存，key是activity的hashcode
    // 存储的是该activity内部的listView的hashcode列表
    private Map<String, List<String>> mListPool = new HashMap<String, List<String>>();

    // 缓存，key是activity的hashcode
    // 存储的是activity内部的view
    private Map<String, List<View>> mViewPool = new HashMap<String, List<View>>();

    private ImageMemoryCache mMemoryCache;
    private DiskCache mDiskCache;
    private LinkedList<ImageEventListener> mEventListeners = new LinkedList<ImageEventListener>();

    private Activity mForegroundActivity;
    private Handler mMainHandler;
    private Resources mRes;

    private final int MaxBitmapSize = 480;

    private Runnable mEnterBackgourndTask = new Runnable(){
        @Override
        public void run() {
            mMemoryCache.forceGC(0);
        }
    };

    private static PhotoShop instance;
    public static PhotoShop getInstance(Context context) {
        if (instance == null) {
            instance = new PhotoShop(context);
        }
        return instance;
    }

    protected PhotoShop(Context context) {
        mMemoryCache = new ImageMemoryCache(context);
        mMemoryCache.setStateListener(new MemoryStateReporter());
        mMainHandler = new Handler(Looper.getMainLooper(), null);

        mDiskCache = new DiskCache(mMemoryCache);
        mRes = context.getResources();

        LifecycleMonitor lifecycleMonitor = new LifecycleMonitorImpl(context);
        lifecycleMonitor.registerActivityLifecycleCallbacks(this);
        lifecycleMonitor.registerAppStateListener(this);
    }

    @Override
    public void setImageDrawable(ImageView imageView, String url, AbsListView parent) {
        setImage(imageView, url, false, parent, 0, false);
    }

    @Override
    public void setImageBackground(View view, String url, AbsListView parent) {
        setImage(view, url, true, parent, 0, false);
    }

    @Override
    public void add2Cache(Bitmap bitmap, byte[] image, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        CommonUtils.throwExceptionNotMainThread();
        int piexls = 0;
        if(bitmap != null)
            piexls = bitmap.getWidth()*bitmap.getHeight();
        //填充1级缓存
        if(bitmap != null && !mMemoryCache.hasBitmap(url,0)){
            mMemoryCache.remandBitmap(url,0,
                    new BitmapDrawable(mRes, bitmap.copy(Bitmap.Config.RGB_565,false)));
        }

        //填充2级缓存
        if (image != null && image.length > 0) {
            mMemoryCache.fillBytes2Cache(url, image,piexls);

            //刷入cache
            if(image.length <102400){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: 填充磁盘缓存，待实现
                        PSLog.d(TAG, "fill the DiskCache now");
                    }
                }).run();
            }
        }
    }

    @Override
    public void removeFromCache(String url) {
        if (TextUtils.isEmpty(url)) {
            PSLog.d(TAG, "removeImageDiskCache: url should not be null !");
            return;
        }

        CommonUtils.throwExceptionIfInMainThread();

        //TODO: 待实现，从缓存中移除
        mMemoryCache.removeFromCache(url, 0);
    }

    @Override
    public void clearMemoryCache(long maxToKeep) {
        CommonUtils.throwExceptionIfInMainThread();
        mMemoryCache.forceGC(maxToKeep);
    }

    @Override
    public long getMemorySize() {
        return mMemoryCache.getMemorySize();
    }

    @Override
    public void registerEventListener(ImageEventListener listener) {
        CommonUtils.throwExceptionNotMainThread();
        mEventListeners.add(listener);
    }

    @Override
    public void unregisterEventListener(ImageEventListener listener) {
        CommonUtils.throwExceptionNotMainThread();
        mEventListeners.remove(listener);
    }

    protected void reportStates() {
        PSLog.d(TAG, "report States, onMemoryOverflow");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        mForegroundActivity = activity;
        Log.d(TAG, "onResumed, invalidate all views");

        String activityString = activity2String(activity);

        List<View> views = mViewPool.get(activityString);
        if(views != null){
            for(View view:views){
                if(Build.VERSION.SDK_INT >= 11 && view.isHardwareAccelerated()){
                    view.invalidate();
                }
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onStopped, release all cache");
        String activityString = activity2String(activity);
        //释放所有图片的引用
        List<View> views = mViewPool.get(activityString);
        if(views != null){
            for(View view:views){
                Drawable drawable = view.getBackground();
                if(drawable != null && drawable instanceof MagicianDrawable){
                    ((MagicianDrawable)drawable).flushImg2Cache();
                }

                if(view instanceof ImageView){
                    drawable = ((ImageView)view).getDrawable();
                    if(drawable != null && drawable instanceof MagicianDrawable){
                        ((MagicianDrawable)drawable).flushImg2Cache();
                    }
                }
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "destroyed, cancel all bind");
        String activityString = activity2String(activity);
        //清理图片
        List<View> views = mViewPool.get(activityString);
        if(views != null){
            for(View view:views){
                //解绑图片
                Object binded = view.getTag(TAG_BACKGROUND);

                this.cancelBinded(binded, view);
                if(Build.VERSION.SDK_INT >= 16)
                    view.setBackground(null);
                else
                    view.setBackgroundDrawable(null);

                if(view instanceof ImageView){
                    binded = view.getTag(TAG_DRAWABLE);
                    this.cancelBinded(binded, view);
                    ((ImageView)view).setImageDrawable(null);
                }


                view.setTag(TAG_BACKGROUND, null);
                view.setTag(TAG_DRAWABLE, null);
                view.setTag(TAG_DRAWABLE_KEY,null);
                view.setTag(TAG_BACKGROUND_KEY,null);
                view.setTag(TAG_DRAWABLE_PRIMITIVE,null);
                view.setTag(TAG_BACKGROUND_PRIMITIVE,null);
                view.setTag(TAG_DISPLAY_MODE_DWB,null);
                view.setTag(TAG_DISPLAY_MODE_BKG,null);
                view.setTag(TAG_BACKGROUND_CACHED,null);
                view.setTag(TAG_DRAWABLE_CACHED,null);
                view.setTag(TAG_PARENT,null);
            }
        }
    }

    @Override
    public void onEnterBackground() {
        Log.d(TAG, "onEnterBackground, release all");
        mMainHandler.postDelayed(mEnterBackgourndTask,10000);
    }

    @Override
    public void onEnterForeground() {
        Log.d(TAG, "onEnterForeground, remove release runnable");
        mMainHandler.removeCallbacks(mEnterBackgourndTask);
    }

    protected void releaseDrawable(String key,int displayMode, BitmapDrawable drawable){
        mMemoryCache.remandBitmap(key, displayMode, drawable);
    }

    private void setImage(final View view, final String url,final boolean isBackground,AbsListView lvParent, final int displayMode,boolean isClearDrawable) {
        if(view == null) {
            Log.i(TAG, "setImageDrawable has null param: " + url);
            return;
        }

        CommonUtils.throwExceptionNotMainThread();

        //保存view原始形态
        if(isBackground) {
            if (view.getTag(TAG_BACKGROUND_CACHED) == null) {
                view.setTag(TAG_BACKGROUND_CACHED,Boolean.TRUE);
                view.setTag(TAG_BACKGROUND_PRIMITIVE,view.getBackground());
            }
        } else {
            if(view.getTag(TAG_DRAWABLE_CACHED) == null) {
                view.setTag(TAG_DRAWABLE_CACHED, Boolean.TRUE);
                view.setTag(TAG_DRAWABLE_PRIMITIVE, ((ImageView) view).getDrawable());
            }
        }

        // 目标url为null，则是解绑图片
        if(url == null) {
            Log.d(TAG, "url is null ,cancel bind it");
            Object binded;
            if(isBackground) {
                binded = view.getTag(TAG_BACKGROUND);
                view.setTag(TAG_BACKGROUND_KEY,null);
                view.setTag(TAG_BACKGROUND, null);
                view.setTag(TAG_DISPLAY_MODE_BKG,null);
                if(Build.VERSION.SDK_INT >= 16)
                    view.setBackground((Drawable) view.getTag(TAG_BACKGROUND_PRIMITIVE));
                else
                    view.setBackgroundDrawable((Drawable) view.getTag(TAG_BACKGROUND_PRIMITIVE));
            } else {
                binded = view.getTag(TAG_DRAWABLE);
                view.setTag(TAG_DRAWABLE, null);
                view.setTag(TAG_DRAWABLE_KEY,null);
                view.setTag(TAG_DISPLAY_MODE_DWB,null);
                if(view instanceof ImageView) {
                    ((ImageView)view).setImageDrawable((Drawable) view.getTag(TAG_DRAWABLE_PRIMITIVE));
                }
            }
            cancelBinded(binded, view);
            return;
        }

        //绑定相同的图片则返回
        String bindedUrl;
        Object existingBinded;
        int bindedDisplayMode;
        Integer displayModeTag;
        if(isBackground) {
            existingBinded = view.getTag(TAG_BACKGROUND);
            bindedUrl = (String) view.getTag(TAG_BACKGROUND_KEY);
            displayModeTag = (Integer) view.getTag(TAG_DISPLAY_MODE_BKG);
            bindedDisplayMode = displayModeTag != null?displayModeTag:0;
        } else {
            bindedUrl = (String) view.getTag(TAG_DRAWABLE_KEY);
            existingBinded = view.getTag(TAG_DRAWABLE);
            displayModeTag = (Integer) view.getTag(TAG_DISPLAY_MODE_DWB);
            bindedDisplayMode = displayModeTag != null?displayModeTag:0;
        }

        if(url.equals(bindedUrl) && displayMode == bindedDisplayMode) {
            Log.d(TAG, "is binding the same url, just set it");
            // if it is MagicianDrawable
            if(existingBinded instanceof MagicianDrawable) {
                //APP change the drawable but view has bined the MagicianDrawable already。
                //so just bind the MagicianDrawable to background or drawable again。
                if(existingBinded instanceof MagicianDrawable) {

                    Drawable newDrawable;
                    if(isBackground) {
                        newDrawable = view.getBackground();
                    } else {
                        newDrawable = ((ImageView)view).getDrawable();
                    }

                    if(existingBinded != newDrawable) {
                        if(isBackground) {
                            if(Build.VERSION.SDK_INT >= 16)
                                view.setBackground((MagicianDrawable)existingBinded);
                            else
                                view.setBackgroundDrawable((MagicianDrawable)existingBinded);
                        } else {
                            ((ImageView)view).setImageDrawable((MagicianDrawable)existingBinded);
                        }
                        Drawable tmp = ((Drawable) existingBinded).getCurrent();
                        //支持GIF
                        if(tmp instanceof Animatable) {
                            ((Animatable)tmp).start();
                        }
                    }
                }

                return;
            }
        }


        //设置view的listview及activity状态监听
        setParentStateListener(view, lvParent);

        Drawable drawable = mMemoryCache.hireBitmap(url, displayMode);
        if (drawable == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable drawable2 = decodeDrawableFromUrl(url, 0); // 读文件是耗时操作，必须放到线程里处理
                    if (drawable2 != null) {
                        mMainHandler.post(new Runnable() { // 缓存及UI更新需要在主线程处理
                            @Override
                            public void run() {
                                mMemoryCache.remandBitmap(url, 0, (BitmapDrawable)drawable2);
                                if(isBackground) {
                                    bindBackground(drawable2, view, url, displayMode);
                                } else {
                                    bindDrawable(drawable2, view, url, displayMode);
                                }
                            }
                        });
                    }
                }
            }).run();
        } else {
            if(isBackground) {
                bindBackground(drawable, view, url, displayMode);
            } else {
                bindDrawable(drawable, view, url, displayMode);
            }
        }
    }


    public BitmapDrawable decodeDrawableFromUrl(String url, int displayMode) {
        Bitmap ret = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = true;
            ret = BitmapFactory.decodeFile(url, options);

            int width = options.outWidth;
            int height = options.outHeight;

            int targetSize = Math.min(Math.min(width, height), MaxBitmapSize);

            options.inJustDecodeBounds = false;
            options.inSampleSize = computeSampleSize(options, targetSize, -1);

            ret = BitmapFactory.decodeFile(url, options);

        } catch(Exception e) {
            e.printStackTrace();
        }
        return new BitmapDrawable(mRes, ret);
    }

    public int computeSampleSize(BitmapFactory.Options options,
                                        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    //绑定drawable
    private void bindDrawable(Drawable realDrawable,View view,String url,int displayMode) {
        //获取已绑定的drawable
        Drawable imageDrawable = ((ImageView)view).getDrawable();
        imageDrawable = imageDrawable instanceof MagicianDrawable ? null:imageDrawable;

        //支持GIF
        if(realDrawable != null && realDrawable instanceof Animatable) {
            ((Animatable) realDrawable).start();
        }

        Drawable drawable = MagicianDrawable.obtain(realDrawable, url, PhotoShop.this,
                imageDrawable, view, displayMode);
        Object binded = view.getTag(TAG_DRAWABLE);
        cancelBinded(binded, view);
        //设置成可见，开始动画
        drawable.setVisible(true, false);
        view.setTag(TAG_DRAWABLE_KEY,url);
        view.setTag(TAG_DRAWABLE, drawable);
        view.setTag(TAG_DISPLAY_MODE_DWB,displayMode);
        ((ImageView)view).setImageDrawable(drawable);
    }

    @SuppressWarnings("deprecation")
    private void bindBackground(Drawable realDrawable,View view,String url,int displayMode) {
        //获取已绑定的background
        Drawable background = view.getBackground();
        background = background instanceof MagicianDrawable ? null:background;

        //支持GIF
        if(realDrawable != null && realDrawable instanceof Animatable) {
            ((Animatable) realDrawable).start();
        }

        Drawable drawable = MagicianDrawable.obtain(realDrawable, url, PhotoShop.this,
                background, view, displayMode);
        //绑定background
        Object binded = view.getTag(TAG_BACKGROUND);
        cancelBinded(binded, view);
        //设置成可见，开始动画
        drawable.setVisible(true, false);
        if(Build.VERSION.SDK_INT >= 16)
            view.setBackground(drawable);
        else
            view.setBackgroundDrawable(drawable);

        view.setTag(TAG_BACKGROUND_KEY,url);
        view.setTag(TAG_BACKGROUND, drawable);
        view.setTag(TAG_DISPLAY_MODE_BKG, displayMode);
    }

    //解除绑定，回收资源
    private void cancelBinded(Object binded,View view){
        //由于app可能通过setImageDrawable修改绑定的图片，故通过tag来标记imageview使用的图片，用来计算引用计数
        if(binded != null){
            if(binded instanceof MagicianDrawable){
                //释放上次绑定的图片
                ((MagicianDrawable)binded).recycle();

            }
        }
    }
    /**
     * 设置view的Activity生命周期监听及listview滚动监听。
     */
    private void setParentStateListener(View view, AbsListView lvParent){

        Context context = view.getContext();
        Object parent = view.getTag(TAG_PARENT);
        //parent为null，则已加入Activity生命周期监听
        if(parent == null){
            //未设置parent，则设置parent。
            parent = lvParent;
            if(parent == null)//parent为Activity
                parent = context;
            view.setTag(TAG_PARENT,parent);

            //view放入Activity池，用于状态切换时操作
            String activityString = activity2String(context);
            List<View> views = mViewPool.get(activityString);
            if(null == views){
                views = new ArrayList<View>();
                mViewPool.put(activityString, views);
            }
            views.add(view);
        }

        if(parent instanceof AbsListView && !ScrollListenerHooker.examHookedbyClass((AbsListView)parent,ListStateListener.class)){
            //设置监听
            ScrollListenerHooker.hookScrollListener((AbsListView)parent,new ListStateListener());
            //添加Activity list索引
            String contextString = activity2String(context);
            List<String> listViews = mListPool.get(contextString);
            if(listViews == null){
                listViews = new ArrayList<String>();
                mListPool.put(contextString, listViews);
            }
            String listString = listView2String((AbsListView) parent);
            if(!listViews.contains(listString))
                listViews.add(listString);
        }
    }

    private void notifyError(int errCode,String errDes,String url,List<View> views){
        if (views != null) {
            for(View view:views){
                for(ImageEventListener listener : mEventListeners) {
                    if (listener != null) {
                        listener.onError(errCode, errDes, url, view);
                    }
                }
            }
        }
    }

    private String activity2String(Context context){
        return context.getClass().getName()+context.hashCode();
    }

    private String listView2String(AbsListView view){
        return String.valueOf(view.hashCode());
    }

    protected BitmapDrawable getDrawableInMem(String key,int displayMode){
        return mMemoryCache.hireBitmap(key,displayMode);
    }

    //////////////ListView state listener/////////////////////////
    class ListStateListener extends ScrollListenerHooker.OnScrollHookListener {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            super.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            super.onScrollStateChanged(view, scrollState);
        }
    }

    class MemoryStateReporter implements ImageMemoryStateListener {

        @Override
        public void onMemoryOverflow(long maxSize, long currentSize) {
            Log.d(TAG, "onMemoryOverflow, " + maxSize + "/" + currentSize);
            reportStates();
        }

        @Override
        public void onAllocateBitmap(String url, long size) {
            Log.d(TAG, "onAllocateBitmap, " + url);
            for(ImageEventListener listener:mEventListeners) {
                if(listener != null)
                    listener.onImageProcessListener(ImageEventListener.EVENT_DECODE_BITMAP_INTO_MEM,null,url,size);
            }
        }

        @Override
        public void onReleaseBitmap(String url, long size) {
            Log.d(TAG, "onReleaseBitmap, " + url);
            for(ImageEventListener listener:mEventListeners) {
                if(listener != null)
                    listener.onImageProcessListener(ImageEventListener.EVENT_BITMAP_RELEASED, null, url, size);
            }
        }

        @Override
        public void onImageBytesFilled(String url, long size) {
            Log.d(TAG, "onImageBytesFilled, " + url);
            for(ImageEventListener listener:mEventListeners) {
                if(listener != null)
                    listener.onImageProcessListener(ImageEventListener.EVENT_FILL_IMAGEBYTES_INTO_MEM,null,url,size);
            }
        }

        @Override
        public void onImageBytesReleased(String url, long size) {
            Log.d(TAG, "onImageBytesReleased, " + url);
            for(ImageEventListener listener:mEventListeners) {
                if(listener != null)
                    listener.onImageProcessListener(ImageEventListener.EVENT_IMAGEBYTES_RELEASED,null,url,size);
            }
        }

    }
}
