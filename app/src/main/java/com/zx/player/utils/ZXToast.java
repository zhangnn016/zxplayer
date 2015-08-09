package com.zx.player.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.zx.player.zxplayer.application.ZXApplication;

/**
 * 打印toast
 * Created by leon on 15/8/9.
 */
public class ZXToast {

    private static Context mContext = ZXApplication.getApplication();

    public static void showToast(String content) {
        if (!TextUtils.isEmpty(content)) {
            Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(int contentID) {
        if (contentID > 0) {
            Toast.makeText(mContext, contentID, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showCenterToast(String content) {
        if (!TextUtils.isEmpty(content)) {
            Toast toShow = Toast.makeText(mContext, content, Toast.LENGTH_SHORT);
            toShow.setGravity(Gravity.CENTER, 0, 0);
            toShow.show();
        }
    }

    public static void showCenterToast(int contentID) {
        if (contentID > 0) {
            Toast toShow = Toast.makeText(mContext, contentID, Toast.LENGTH_SHORT);
            toShow.setGravity(Gravity.CENTER, 0, 0);
            toShow.show();
        }
    }
}
