package com.zx.player.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by niuniuzhang on 15/7/23.
 */
@SuppressLint("NewApi")
public class MagicianDrawable extends Drawable {
    //	private static final String TAG = "MagicianDrawable";
//	@Override
//	protected void finalize() throws Throwable {
//		// TODO Auto-generated method stub
//		flushImg2Cache();
//		super.finalize();
//	}
    private static MagicianDrawable sPool;
    private static int sPoolSize = 0;

    private static final int MAX_POOL_SIZE = 50;

    /*
     * drawable 2 draw
     */
    protected Drawable mRealDrawable;
    /*
     * 图片的链接
     */
    private String mUrl;

    private int mDisplayMode;
    //	private String mKey;
	/*
	 * 绑定的图片
	 */
    private View mBindedView;

    /*
     * 归属的ImageMagicianImpl
     */
    private PhotoShop mOwner;
    private Drawable mLoadingDrawable;
    private int mIntrinsicHeight;
    private int mIntrinsicWidth;

    protected MagicianDrawable mNext;

    /**
     * 获取一个MagicianDrawable实例，必须主线程调用
     * @param d			drawable对象，用于真正的绘制
     * @param url		drawable对应的url
     * @param owner		ImageMagicianImpl 实例
     * @return	MagicianDrawable实例
     */
    public static MagicianDrawable obtain(Drawable d,String url,PhotoShop owner,Drawable loadingDrawable,View view,int displayMode) {
        CommonUtils.throwExceptionNotMainThread();

        if (sPool != null) {
            MagicianDrawable p = sPool;
            sPool = p.mNext;
            p.mNext = null;
            p.mUrl = url;
            p.mOwner = owner;
            p.mRealDrawable = d;
            if (p.mRealDrawable != null) {
                p.addCallback();
                p.mIntrinsicHeight = p.mRealDrawable.getIntrinsicHeight();
                p.mIntrinsicWidth = p.mRealDrawable.getMinimumWidth();
            }
            p.mLoadingDrawable = loadingDrawable;
            p.mBindedView = view;
            p.mDisplayMode = displayMode;
//            p.mKey = key;
            sPoolSize--;
            if(d != null)
                p.setBounds(d.getBounds());
            return p;
        }
        return new MagicianDrawable(d,url,owner,loadingDrawable,view,displayMode);
    }

    /**
     * 回收一个MagicianDrawable 必须主线程调用
     * @param drawable	MagicianDrawable实例
     */
    private static void recycle(MagicianDrawable drawable){
        CommonUtils.throwExceptionNotMainThread();
        if (sPoolSize < MAX_POOL_SIZE) {

            drawable.mNext = sPool;
            drawable.mBindedView = null;
            drawable.mOwner = null;
            drawable.mRealDrawable = null;
            drawable.mUrl = null;
            drawable.mLoadingDrawable = null;
            drawable.mDisplayMode = 0;
            drawable.mIntrinsicHeight = -1;
            drawable.mIntrinsicWidth = -1;
            sPool = drawable;
            sPoolSize++;
        }
    }


    private MagicianDrawable(Drawable drawable,String url,PhotoShop owner,Drawable loadingDrawable,View view,int displayMode){
        mRealDrawable = drawable;
        if (mRealDrawable != null) {
            addCallback();
            mIntrinsicHeight = mRealDrawable.getIntrinsicHeight();
            mIntrinsicWidth = mRealDrawable.getMinimumWidth();
        }
        mUrl = url;
        mOwner = owner;
        mLoadingDrawable = loadingDrawable;
        mBindedView = view;
        mDisplayMode = displayMode;
        if(mRealDrawable != null) {
            super.setBounds(mRealDrawable.getBounds());
        }
    }

    public void invalidate(){
        if(mBindedView != null) {
//            Log.d("CallbackDispatcher", mUrl+mBindedView);
            mBindedView.invalidate();
        }
    }

    private void addCallback(){
        if(mRealDrawable != null && Build.VERSION.SDK_INT >= 11) {
            Callback callback = mRealDrawable.getCallback();
            if (callback == null || !(callback instanceof CallbackDispatcher)) {
                CallbackDispatcher dispatcher = new CallbackDispatcher();
                mRealDrawable.setCallback(dispatcher);
                dispatcher.addMagicianDrawable(this);
            } else {
                ((CallbackDispatcher) callback).addMagicianDrawable(this);
            }
        }
    }
    /*
     * 绘制
     */
    @SuppressWarnings("deprecation")
    @Override
    public void draw(Canvas canvas) {
//		Log.w("MagicianDrawable", "draw "+mUrl);
        if(null == mRealDrawable){
            //重新load图片通过draw方法按需load，节省内存
            mRealDrawable = mOwner.getDrawableInMem(mUrl,mDisplayMode);

            if(null == mRealDrawable){
                if(mBindedView != null){
                    Object parent = mBindedView.getTag(PhotoShop.TAG_PARENT);
                    if(mBindedView.getBackground() == this){
                        mBindedView.setTag(PhotoShop.TAG_BACKGROUND_KEY, null);
                        //设置loading背景
                        if(Build.VERSION.SDK_INT >= 16)
                            mBindedView.setBackground(mLoadingDrawable);
                        else
                            mBindedView.setBackgroundDrawable(mLoadingDrawable);

                        //重新绑定图片
                        if(parent instanceof Context){
                            mOwner.setImageBackground(mBindedView, mUrl,null);
                        }else
                            mOwner.setImageBackground(mBindedView, mUrl,(AbsListView) parent);
                    }else{
                        mBindedView.setTag(PhotoShop.TAG_DRAWABLE_KEY, null);
                        //设置loading背景
                        ((ImageView)mBindedView).setImageDrawable(mLoadingDrawable);
                        //重新绑定图片
                        if(parent instanceof Context)
                            mOwner.setImageDrawable((ImageView) mBindedView, mUrl,null);
                        else
                            mOwner.setImageDrawable((ImageView) mBindedView, mUrl,(AbsListView) parent);
                    }
                }
            }else{
                addCallback();
                mRealDrawable.setBounds(this.getBounds());
                //支持GIF
                //FIXME
                if(mRealDrawable instanceof Animatable) {
                    ((Animatable) mRealDrawable).start();
                }

                mBindedView.invalidate();
            }
        }

        if(mRealDrawable != null){
            mRealDrawable.setBounds(this.getBounds());
            mRealDrawable.draw(canvas);
        }else{
            if(mLoadingDrawable != null){
                mLoadingDrawable.draw(canvas);
            }
        }
    }

    protected long getByteCount(){
        if(mRealDrawable != null){
            Bitmap bitmap = ((BitmapDrawable)mRealDrawable).getBitmap();
            return bitmap.getRowBytes()*bitmap.getHeight();
        }else
            return 0;
    }

    /*
     * 将图片放入缓存，此时图片可以恢复
     */
    @SuppressLint("NewApi")
    protected void flushImg2Cache(){
        if(mRealDrawable != null){
            if(Build.VERSION.SDK_INT >= 11) {
                Callback callback = mRealDrawable.getCallback();
                if (callback instanceof CallbackDispatcher) {
                    ((CallbackDispatcher) callback).removeMagicianDrawable(this);
                }
            }
            mOwner.releaseDrawable(mUrl,mDisplayMode, (BitmapDrawable)mRealDrawable);
            mRealDrawable = null;
        }
    }

//	protected void resumeSelfFromCache(){
//
//		if(null == mRealDrawable){
//			mRealDrawable = mOwner.getDrawableInMem(mKey);
//
//			if(null == mRealDrawable){
//				if(mBindedView != null){
//					Object parent = mBindedView.getTag(ImageMagicianImpl.TAG_PARENT);
//					if(mBindedView.getBackground() == this){
//						mBindedView.setTag(ImageMagicianImpl.TAG_BACKGROUND_KEY, null);
//
//						if(parent instanceof Activity)
//							mOwner.setImageBackground(mBindedView, mUrl,null);
//						else
//							mOwner.setImageBackground(mBindedView, mUrl,(AbsListView) parent);
//					}else{
//						mBindedView.setTag(ImageMagicianImpl.TAG_DRAWABLE_KEY, null);
//						if(parent instanceof Activity)
//							mOwner.setImageDrawable((ImageView) mBindedView, mUrl,null);
//						else
//							mOwner.setImageDrawable((ImageView) mBindedView, mUrl,(AbsListView) parent);
//					}
//				}
//			}else{
//				mRealDrawable.setBounds(this.getBounds());
//				mBindedView.invalidate();
//			}
//		}
//	}

//	protected View getBindedView(){
//		return mBindedView;
//	}

    protected String getBindedUrl(){
        return mUrl;
    }

    /*
     * 回收图片
     * 彻底释放，图片将进入回收状态，无法恢复
     */
    @SuppressWarnings("deprecation")
    protected void recycle(){
        if(null != mBindedView){
            if(mBindedView.getBackground() == this){

                if(Build.VERSION.SDK_INT >= 16)
                    mBindedView.setBackground(null);
                else
                    mBindedView.setBackgroundDrawable(null);

            }else if(((ImageView)mBindedView).getDrawable() == this){
                ((ImageView)mBindedView).setImageDrawable(null);
            }
        }

        flushImg2Cache();
        setBounds(0, 0, 0, 0);
        MagicianDrawable.recycle(this);
    }

    protected boolean isRecycled(){
        return mRealDrawable == null;
    }
    /*
     * 设置绑定的view
     * 参数：
     * 		view  该图片绑定的view对象。
     */
//	protected void setBindedView(View view){
//		mBindedView = view;
//	}
    ////////////////Drawable方法重写//////////////////////////////////////////////////////////////
    @Override
    public void setFilterBitmap(boolean filter) {

        if(mRealDrawable != null)
            mRealDrawable.setFilterBitmap(filter);
    }

    @Override
    public void scheduleSelf(Runnable what, long when) {
        super.scheduleSelf(what, when);
    }

    @Override
    public void setDither(boolean dither) {

        if(mRealDrawable != null)
            mRealDrawable.setDither(dither);
    }



    @Override
    public int getChangingConfigurations() {

        if(mRealDrawable != null)
            return mRealDrawable.getChangingConfigurations();
        else
            return super.getChangingConfigurations();
    }

    @Override
    public void setAlpha(int alpha) {

        if(mRealDrawable != null)
            mRealDrawable.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter cf) {

        if(mRealDrawable != null)
            mRealDrawable.setColorFilter(cf);
    }
    //	@Override
//	public Drawable mutate() {
//
//		if(realDrawable != null)
//			return realDrawable.mutate();
//		else
//			return super.mutate();
//	}
    @Override
    public void inflate(Resources r, XmlPullParser parser,
                        AttributeSet attrs) throws XmlPullParserException, IOException {

        if(mRealDrawable != null)
            mRealDrawable.inflate(r, parser, attrs);
    }
    @Override
    public int getIntrinsicWidth() {

        if(mRealDrawable != null)
            return mRealDrawable.getIntrinsicWidth();
        else
            return mIntrinsicWidth;
    }
    @Override
    public int getIntrinsicHeight() {

        if(mRealDrawable != null)
            return mRealDrawable.getIntrinsicHeight();
        else
            return mIntrinsicHeight;
    }
    @Override
    public int getOpacity() {

        if(mRealDrawable != null)
            return mRealDrawable.getOpacity();
        else
            return 0;
    }
    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        //TaoLog.Logd("DrawableProxy", "setBounds");
        if(mRealDrawable != null)
            mRealDrawable.setBounds(left, top, right, bottom);
        if(mLoadingDrawable != null){
            mLoadingDrawable.setBounds(left, top, right, bottom);
        }
    }
    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);

        if(mRealDrawable != null)
            mRealDrawable.setBounds(bounds);
        if(mLoadingDrawable != null){
            mLoadingDrawable.setBounds(bounds);
        }

    }
    @Override
    public void setChangingConfigurations(int configs) {

        if(mRealDrawable != null)
            mRealDrawable.setChangingConfigurations(configs);
    }

    @Override
    public void setColorFilter(int color, PorterDuff.Mode mode) {

        if(mRealDrawable != null)
            mRealDrawable.setColorFilter(color, mode);
    }
    @Override
    public void clearColorFilter() {

        if(mRealDrawable != null)
            mRealDrawable.clearColorFilter();
    }
    @Override
    public boolean isStateful() {

        if(mRealDrawable != null)
            return mRealDrawable.isStateful();
        else
            return super.isStateful();
    }
    @Override
    public boolean setState(int[] stateSet) {

        if(mRealDrawable != null)
            return mRealDrawable.setState(stateSet);
        else
            return super.setState(stateSet);
    }
    @Override
    public int[] getState() {

        if(mRealDrawable != null)
            return mRealDrawable.getState();
        else
            return super.getState();
    }

    @Override
    public Drawable getCurrent() {
        if(mRealDrawable != null)
            return mRealDrawable;
        else
            return mLoadingDrawable;
    }

    //	@Override
//	public Drawable getCurrent() {
//
//		if(realDrawable != null)
//			return realDrawable.getCurrent();
//		else
//			return super.getCurrent();
//	}
    @Override
    public boolean setVisible(boolean visible, boolean restart) {

        if(mRealDrawable != null)
            return mRealDrawable.setVisible(visible, restart);
        else
            return super.setVisible(visible, restart);
    }
    @Override
    public Region getTransparentRegion() {

        if(mRealDrawable != null)
            return mRealDrawable.getTransparentRegion();
        else
            return super.getTransparentRegion();
    }


    @Override
    public int getMinimumWidth() {

        if(mRealDrawable != null)
            return mRealDrawable.getMinimumWidth();
        else
            return super.getMinimumWidth();
    }
    @Override
    public int getMinimumHeight() {

        if(mRealDrawable != null)
            return mRealDrawable.getMinimumHeight();
        else
            return super.getMinimumHeight();
    }
    @Override
    public boolean getPadding(Rect padding) {

        if(mRealDrawable != null)
            return mRealDrawable.getPadding(padding);
        else
            return super.getPadding(padding);
    }

//	public Drawable getRealDrawable(){
//		return mRealDrawable;
//	}
}
