package com.zx.player.tools;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 工具类，取得主线程的handler
 * Created by niuniuzhang on 15/7/28.
 */
public class LooperManager {
    public static Handler getMainHanlder() {
        return new Handler(Looper.getMainLooper(), null);
    }

    public static Handler getHandler(Context context, Handler.Callback callback) {
        return new Handler(context.getMainLooper(), callback);
    }
}
