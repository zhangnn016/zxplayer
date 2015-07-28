package com.zx.player.tools;

import android.util.Log;
import android.widget.AbsListView;

import java.lang.reflect.Field;

/**
 * ListView onScroll监听钩子。<br>
 * App在一些场景下需要在多个地方同时监听一个Listview的OnScroll事件，而Listview只支持设置一个listener，
 * 多个设置将会覆盖成最后设置的Listener。通过{@link ScrollListenerHooker  ScrollListenerHooker}
 * 可以解决覆盖问题。ScrollListenerHooker通过代理的模式在原有listener上插入钩子拦截并传递onScroll状态，
 * 并在挂入钩子前检查原有钩子是否已在监听状态，以防止循环调用的堆栈溢出。<br>
 *     <br>
 * 注意：<br>
 * 1、当App调用原生{@link AbsListView#setOnScrollListener(AbsListView.OnScrollListener) setOnScrollListener}接口将覆盖
 * 原有的设置和挂入的钩子。建议当需要多处监听该事件时，都采用该
 * {@link ScrollListenerHooker#hookScrollListener(AbsListView, OnScrollHookListener)}  hookScrollListener}
 * 接口完成。<br>
 *
 * 2、APP在实现{@link OnScrollHookListener OnScrollHookListener}接口时，需要在onScroll和onScrollStateChanged
 * 方法时调用super.onScroll和super.onScrollStateChanged方法。
 *
 * Created by niuniuzhang on 15/7/21.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class ScrollListenerHooker{
    private static final String TAG = "ScrollListenerHooker";
    private static ThreadLocal<Boolean> sExamTag = new ThreadLocal<Boolean>();
    private static ThreadLocal<String> sExamClass = new ThreadLocal<String>();

    private static Field sOnScrollListenerF;
    static{
        try {
            //获取存储OnScrollListener的field字段，用于获取原有的listener获取
            sOnScrollListenerF = AbsListView.class.getDeclaredField("mOnScrollListener");
            if(sOnScrollListenerF == null){
                Field[] allField = AbsListView.class.getDeclaredFields();
                for(Field field:allField){
                    if(field.getType().isAssignableFrom(AbsListView.OnScrollListener.class)){
                        sOnScrollListenerF = field;
                    }
                }
            }
            if(sOnScrollListenerF == null)
                Log.w(TAG, "ScrollListenerHooker can not get listview's scroll listener");
            else
                sOnScrollListenerF.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检测是否已挂入某种类型的监听
     * @param listView listview实例
     * @param clz   监听的class类型
     * @return  true 已挂有clz指定的类型，false 未挂入
     */
    public static boolean examHookedbyClass(AbsListView listView,Class clz){

        if(clz == null)
            return false;

        if(sOnScrollListenerF == null) {
            Log.w(TAG, "ScrollListenerHooker can not get listview's scroll listener");
            return false;
        }
        try{
            sExamClass.set(clz.getName());
            AbsListView.OnScrollListener listener = (AbsListView.OnScrollListener) sOnScrollListenerF.get(listView);
            if(listener == null)
                return false;
            listener.onScroll(listView,listView.getFirstVisiblePosition(),
                    listView.getLastVisiblePosition()-listView.getFirstVisiblePosition(),listView.getCount());
            return sExamClass.get() == null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            sExamClass.remove();
        }
        return false;
    }

    /**
     * listview设置onScroll状态监听
     * @param listView      监听的listview实例
     * @param hookListener  滚动监听
     */
    public static void hookScrollListener(AbsListView listView,OnScrollHookListener hookListener){
        if(listView == null || hookListener == null)
            return;

        if(sOnScrollListenerF == null) {
            Log.w(TAG, "ScrollListenerHooker can not get listview's scroll listener");
            return;
        }
        try {
            //通过先获取listner对象，再检测的方式，可以屏蔽多线程同一个钩子挂入监听列表两次的bug。
            AbsListView.OnScrollListener listener = (AbsListView.OnScrollListener) sOnScrollListenerF.get(listView);

            if(listener == null) {
                //未设置listener，直接设置
                listView.setOnScrollListener(hookListener);
            }else {
                //为了防止多线程并发挂钩子，所以采用threadlocal做检测标记，而不采用成员变量。
                sExamTag.set(true);
                //检查是否对应的实例已在监听
                listener.onScroll(listView,listView.getFirstVisiblePosition(),
                        listView.getLastVisiblePosition()-listView.getFirstVisiblePosition(),listView.getCount());
                hookListener.startExam();
                if(sExamTag.get() != null){
                    //未监听，则挂入钩子
                    hookListener.setDelegated(listener);
                    listView.setOnScrollListener(hookListener);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }finally {
            sExamTag.remove();
        }
    }

    /**
     *
     */
    public static abstract class OnScrollHookListener implements AbsListView.OnScrollListener {
        private AbsListView.OnScrollListener mDelegated;
        private boolean mIsExam = false;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(mDelegated != null)
                mDelegated.onScrollStateChanged(view,scrollState);
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if(mIsExam) {
                Boolean isExam = sExamTag.get();
                if (isExam != null && isExam) {
                    sExamTag.remove();
                    return;
                }
                mIsExam = false;
            }

            String examClz = sExamClass.get();
            if (examClz != null && getClass().getName().equals(examClz)) {
                sExamClass.remove();
                return;
            }

            if(mDelegated != null)
                mDelegated.onScroll(view,firstVisibleItem,visibleItemCount,totalItemCount);
        }

        public void startExam(){
            mIsExam = true;
        }

        protected void setDelegated(AbsListView.OnScrollListener listener){
            mDelegated = listener;
        }

    }
}
