package com.zx.player.zxplayer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 主页底部的bar
 * Created by leon on 15/8/9.
 */
public class HomeBottomBar extends LinearLayout {

    private int mCurrent;

    private int mNumber;

    public HomeBottomBar(Context context) {
        super(context);
        doInit();
    }

    public HomeBottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        doInit();
    }

    public HomeBottomBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        doInit();
    }

    private void doInit() {
        // do nothing
    }
}
