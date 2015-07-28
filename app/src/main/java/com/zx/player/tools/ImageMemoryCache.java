package com.zx.player.tools;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Bitmap内存缓存
 * 为了减少锁，所有方法必须在主线程调用，不提供线程安全
 * 内存缓存不支持分辨率模糊匹配
 *
 * Created by niuniuzhang on 15/7/21.
 */
public class ImageMemoryCache {
    private static final String TAG = ImageMemoryCache.class.getSimpleName();

    //内存缓存池
    private Map<String,SparseArray<BitmapReference>> mBitmapPool = new HashMap<String,SparseArray<BitmapReference>>();
    private List<BitmapReference> mBitmapArray = new LinkedList<BitmapReference>();

    private long mMemeoryOccupy = 0;
    public static long mMaxMemory = 10*1024*1024;

    private BitmapReference sPool = null;
    private int sPoolSize = 0;
    private static final int MAX_POOL_SIZE = 50;

    private ImageMemoryStateListener mStateListener;

    // 二级缓存用到的成员变量
    private long mMemeoryOccupy2 = 0;
    public static long mMaxMemory2 = 5*1024*1024;

    //设置缓存图片分辨率阀值
    private long mPiexlThreshold = 800*800;

    //CPU 频率阀值
    private static final long CPU_HZ_THRESHOLD = 1500000;


    private Map<String, ImageBytes> mBitmapBytesPool = new HashMap<String, ImageBytes>();
    private List<ImageBytes> mImageBytesArray = new LinkedList<ImageBytes>();


    public ImageMemoryCache(Context context){
        if(Build.VERSION.SDK_INT >= 14)
            context.getApplicationContext().registerComponentCallbacks(new LowMemoryListener());
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if(am != null){
            int memClass = am.getMemoryClass();
            if(memClass != 0) {
                mMaxMemory = memClass * 1024 * 256;
                mMaxMemory2 = memClass * 1024 * 128;
            }

            mMaxMemory = mMaxMemory > 20*1024*1024 ? 20*1024*1024: mMaxMemory;
            mMaxMemory = mMaxMemory < 4*1024*1024 ? 4*1024*1024: mMaxMemory;

            mMaxMemory2 = mMaxMemory2 > 10*1024*1024 ? 10*1024*1024: mMaxMemory2;
            mMaxMemory2 = mMaxMemory2 < 2*1024*1024 ? 2*1024*1024: mMaxMemory2;

//            mMinMemory2 = mMaxMemory2/2;

            long cpuHZ = 0;
            String hzString = getMaxCpuFreq();
            if(!TextUtils.isEmpty(hzString)) {
                cpuHZ = Long.parseLong(hzString);
            }
            PSLog.d(TAG, "cpu HZ =>>>>>"+cpuHZ);
            if (cpuHZ <= CPU_HZ_THRESHOLD) {
                mPiexlThreshold = 300*300;
            }

            PSLog.d(TAG, "using memory mMaxMemory = "+mMaxMemory/(1024*1024)+"M");
            PSLog.d(TAG, "using memory mMaxMemory2 = "+mMaxMemory2/(1024*1024)+"M");
        }
    }

    class LowMemoryListener implements ComponentCallbacks {

        @Override
        public void onConfigurationChanged(Configuration newConfig) {

        }

        @Override
        public void onLowMemory() {
            gc(0);
            gc2(0);
        }

    }
    /**
     * 租用Bitmap
     * @param key	bitmap对应的key
     * @return key对应的bitmap
     */
    public BitmapDrawable hireBitmap(String key,int displayMode){
        //通过key获取bitmap
        SparseArray<BitmapReference> bitmapReferences = mBitmapPool.get(key);
        if(bitmapReferences == null)
            return null;
        BitmapReference reference = bitmapReferences.get(displayMode);
        if(reference != null && !reference.mIsRecycled){
            //未被回收，增加引用计数
            reference.mCount++;
            //把最新使用的放链表尾部，使其最后回收
            if(mBitmapArray.remove(reference)) {
                mBitmapArray.add(reference);
            }
            return reference.mBitmapDrawable;
        }else if(reference != null){
            //移除一个被recycle的bitmap
            mBitmapPool.remove(key);
            mBitmapArray.remove(reference);
        }
        return null;
    }

    public void removeFromCache(String key, int displayMode) {
        SparseArray<BitmapReference> bitmapReferences = mBitmapPool.get(key);
        if (bitmapReferences != null) {
            BitmapReference reference = bitmapReferences.get(displayMode);
            if (reference != null && !reference.mIsRecycled) {
                reference.mCount--;
                if (reference.mCount == 0) {
                    if(reference.mBitmapDrawable instanceof Animatable)
                        ((Animatable) reference.mBitmapDrawable).stop();
                }
            }
        }
    }

    /**
     * clone一份图片进入新url
     */
    public void cloneBitmap(String newUrl, String oldUrl){
        if(TextUtils.isEmpty(newUrl) || TextUtils.isEmpty(oldUrl) || mBitmapPool.containsKey(newUrl))
            return;

        SparseArray<BitmapReference> bitmapDrawables = mBitmapPool.get(oldUrl);
        if(bitmapDrawables == null)
            return;

        int size = bitmapDrawables.size();
        for(int i = 0;i<size;i++){
            BitmapReference reference = bitmapDrawables.valueAt(i);
            Bitmap oldBitmap = reference.mBitmapDrawable.getBitmap();
            Bitmap newBitmap = oldBitmap.copy(Bitmap.Config.RGB_565,false);
            remandBitmap(newUrl,reference.mDisplayMode,new BitmapDrawable(newBitmap));
        }
    }


    public ImageBytes getBitmapBytes(String key) {
        return mBitmapBytesPool.get(key);
    }

    /**
     * 归还/寄存Bitmap。当放入一个新Bitmap时，默认未被引用
     */
    @SuppressWarnings("ConstantConditions")
    public void remandBitmap(String key,int displayMode,BitmapDrawable bitmapDrawable){
        //通过key获取bitmap
        SparseArray<BitmapReference> bitmapReferences = mBitmapPool.get(key);
        if(bitmapReferences == null) {
            bitmapReferences = new SparseArray<BitmapReference>();
            mBitmapPool.put(key,bitmapReferences);
        }
        BitmapReference reference = bitmapReferences.get(displayMode);
        if(reference != null){
            if(!reference.mIsRecycled && reference.mCount > 0){
                //未被回收，减少引用计数
                reference.mCount--;
                if (reference.mCount == 0) {
                    if(reference.mBitmapDrawable instanceof Animatable)
                        ((Animatable) reference.mBitmapDrawable).stop();
                }
            }else if(reference.mIsRecycled){
                //移除一个被recycle的bitmap
                mBitmapPool.remove(key);
                mBitmapArray.remove(reference);
            }
        }else{
            if(bitmapDrawable.getBitmap().isRecycled()){
                PSLog.d("ImageMagician", "add a recycled bitmap"+key);
                return;
            }
            BitmapReference bitmapReference = obtainBitmapRef(key,bitmapDrawable,displayMode);
            mMemeoryOccupy += bitmapReference.mMemorySize;
            if(mMemeoryOccupy > mMaxMemory){
                gc(mMaxMemory);
                //新分配图片事件
            }
            mStateListener.onAllocateBitmap(key+" "+displayMode, bitmapReference.mMemorySize);
            bitmapReferences.put(displayMode, bitmapReference);
            mBitmapArray.add(bitmapReference);
        }
    }

    public void fillBytes2Cache(String url, byte[] imageBytes,long piexlsSize) {
        if (TextUtils.isEmpty(url) || imageBytes == null) {
            return;
        }

        if(mBitmapBytesPool.containsKey(url))
            return;

        ImageBytes bitmapBytes = new ImageBytes();
        bitmapBytes.mUrl = url;
        bitmapBytes.mBytes = imageBytes;
        bitmapBytes.mPiexls = piexlsSize;

        if (bitmapBytes.mPiexls <mPiexlThreshold) {
            mBitmapBytesPool.put(url, bitmapBytes);
            mImageBytesArray.add(bitmapBytes);
            mMemeoryOccupy2+=bitmapBytes.mBytes.length;
            if (mMemeoryOccupy2 > mMaxMemory2)  gc2(mMaxMemory2);

            mStateListener.onImageBytesFilled(url,imageBytes.length);
        }
    }


    public boolean hasBitmap(String key,int displayMode){
        SparseArray<BitmapReference> bitmapReferences = mBitmapPool.get(key);
        if(bitmapReferences == null)
            return false;
        BitmapReference reference = bitmapReferences.get(displayMode);
        if(reference != null && !reference.mIsRecycled){
            return true;
        }else if(reference != null){
            mBitmapPool.remove(key);
            mBitmapArray.remove(reference);
        }
        return false;
    }

    /**
     * 查看key对应的二级缓存是否存在
     */
    public boolean hasBitmapBytes(String key){
        ImageBytes tmpImageBytes = mBitmapBytesPool.get(key);
        return tmpImageBytes != null;
    }

    //强制GC，将可以释放的缓存全部释放
    public void forceGC(long maxToKeep) {
        if(maxToKeep<0)
            maxToKeep = mMemeoryOccupy2 + mMaxMemory;
        long toRelease = mMemeoryOccupy+mMemeoryOccupy2-maxToKeep;
        if(toRelease < 0)
            return;
        gc(mMemeoryOccupy-toRelease > 0 ? mMemeoryOccupy-toRelease:0);
        toRelease = mMemeoryOccupy+mMemeoryOccupy2-maxToKeep;
        if(toRelease < 0)
            return;
        gc2(mMemeoryOccupy2-toRelease>0 ? mMemeoryOccupy2-toRelease:0);
    }

    public ImageMemoryStatus dumpMemoryInfo(){
        ImageMemoryStatus imageMemoryStatus = new ImageMemoryStatus();
        imageMemoryStatus.mBitmapStatus = new ArrayList<ImageMemoryStatus.BitmapStatus>();
        for(BitmapReference br:mBitmapArray){
            ImageMemoryStatus.BitmapStatus bs = new ImageMemoryStatus.BitmapStatus();
            bs.mUrl = br.mKey;
            bs.mDisplayMode = br.mDisplayMode;
            //bs.mSize = br.mBitmapDrawable.getBitmap().getRowBytes();
            bs.mSize = br.mMemorySize;
            imageMemoryStatus.mBitmapsSize += bs.mSize;
            imageMemoryStatus.mBitmapStatus.add(bs);
        }

        imageMemoryStatus.mImageBytesStatus = new ArrayList<ImageMemoryStatus.ImageBytesStatus>();
        for(ImageBytes ib:mImageBytesArray){
            ImageMemoryStatus.ImageBytesStatus imageBytesStatus = new ImageMemoryStatus.ImageBytesStatus();
            imageBytesStatus.mSize = ib.mBytes.length;
            imageBytesStatus.mUrl = ib.mUrl;
            imageMemoryStatus.mImageBytesSize += imageBytesStatus.mSize;
            imageMemoryStatus.mImageBytesStatus.add(imageBytesStatus);
        }
        return imageMemoryStatus;
    }


    public long getMemorySize(){
        return mMemeoryOccupy+mMemeoryOccupy2;
    }

    public long getMaxMemorySize(){
        return mMaxMemory+mMaxMemory2;
    }

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private void gc(long maxToKeep){
        if(maxToKeep < 0)
            maxToKeep = 0;

        if(maxToKeep > mMaxMemory)
            maxToKeep = mMaxMemory;

        Iterator<BitmapReference> it = mBitmapArray.iterator();
        while(it.hasNext()){
            BitmapReference bitmapRef = it.next();
            //内存在上限以内
            if(mMemeoryOccupy < maxToKeep)
                break;

            if(bitmapRef.mCount == 0){
                mMemeoryOccupy -= bitmapRef.mMemorySize;
                it.remove();
                //mBitmapArray.remove(mBitmapRef);
                SparseArray<BitmapReference> bitmapReferences = mBitmapPool.get(bitmapRef.mKey);
                if(bitmapReferences != null){
                    bitmapReferences.remove(bitmapRef.mDisplayMode);
                    if(bitmapReferences.size() == 0)
                        mBitmapPool.remove(bitmapRef.mKey);
                }
                mStateListener.onReleaseBitmap(bitmapRef.mKey+" "+bitmapRef.mDisplayMode, bitmapRef.mMemorySize);
                bitmapRef.mIsRecycled = true;
                bitmapRef.mBitmapDrawable.getBitmap().recycle();
                bitmapRef.mBitmapDrawable = null;
                bitmapRef.mKey = null;
                bitmapRef.mMemorySize = 0;
                bitmapRef.mNext = null;
                recycleBitmapRef(bitmapRef);
            }
        }
        if(mStateListener != null && mMemeoryOccupy > mMaxMemory){
            mStateListener.onMemoryOverflow(mMaxMemory, mMemeoryOccupy);
        }
    }

    //二级缓存gc操作
    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private void gc2(long maxToKeep) {
        if(maxToKeep < 0)
            maxToKeep = 0;

        if(maxToKeep > mMaxMemory2)
            maxToKeep = mMaxMemory2;

        Iterator<ImageBytes> it = mImageBytesArray.iterator();
        while(it.hasNext()){

            ImageBytes imageBytes = it.next();
            //内存在上限以内
            if(mMemeoryOccupy2 < maxToKeep )
                break;

            mMemeoryOccupy2 -= imageBytes.mBytes.length;
            it.remove();
            mBitmapBytesPool.remove(imageBytes.mUrl);
            mStateListener.onImageBytesReleased(imageBytes.mUrl, imageBytes.mBytes.length);
            imageBytes.mPiexls = 0;
            imageBytes.mBytes = null;
            imageBytes.mUrl = null;
        }

    }

    public void setStateListener(ImageMemoryStateListener listener){
        mStateListener = listener;
    }

    private BitmapReference obtainBitmapRef(String key,BitmapDrawable bitmapDrawable,int displayMode){
        if (sPool != null) {
            BitmapReference p = sPool;
            sPool = p.mNext;
            p.init(key, bitmapDrawable,displayMode);
            sPoolSize--;
            return p;
        }
        BitmapReference br = new BitmapReference();
        return br.init(key, bitmapDrawable,displayMode);
    }

    private void recycleBitmapRef(BitmapReference br){
        if (sPoolSize < MAX_POOL_SIZE) {
            br.mNext = sPool;
            sPool = br;
            sPoolSize++;
        }
    }
    // 获取手机CPU频率
    private String getMaxCpuFreq() {
        StringBuilder stringBuilder = new StringBuilder();
        String resultStr = "";
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                stringBuilder.append(new String(re));
            }
            in.close();

            resultStr = stringBuilder.toString();
            int index = resultStr.indexOf('\n');
            if (index == -1) {
                index = resultStr.length();
            }
            resultStr = resultStr.substring(0, index);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return resultStr;
    }

    @SuppressLint("NewApi")
    class BitmapReference{
        public int mCount = 0;
        public BitmapDrawable mBitmapDrawable;
        public long mMemorySize;
        public boolean mIsRecycled = false;
        public String mKey;
        public int mDisplayMode = 0;
        BitmapReference mNext;

        public BitmapReference init(String key,BitmapDrawable bitmapDrawable,int displayMode){
            mNext = null;
            mCount = 0;
            mDisplayMode = displayMode;
            mBitmapDrawable = bitmapDrawable;
            if (Build.VERSION.SDK_INT >= 12)
                mMemorySize = bitmapDrawable.getBitmap().getByteCount();
            else
                mMemorySize = bitmapDrawable.getBitmap().getRowBytes()*bitmapDrawable.getBitmap().getHeight();
            mIsRecycled = false;
            mKey = key;
            return this;
        }
    }

    public static class ImageBytes {
        public String mUrl;
        public byte[] mBytes;
        //图片分辨率
        public long mPiexls;
    }
}

