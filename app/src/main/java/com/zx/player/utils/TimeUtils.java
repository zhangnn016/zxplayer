package com.zx.player.utils;

/**
 * 时间处理工具类
 * Created by leon on 15/8/9.
 */
public class TimeUtils {

    public static String formatDuration(int duration) {
        if (duration <= 0) {
            return "未知时长";
        }

        if (duration < 60) {
            return String.format("00:00:%2d", duration);
        } else if (duration < 3600) {
            int minutes = duration / 60;
            int seconds = duration - minutes * 60;
            return String.format("00:%02d:%02d", minutes, seconds);
        } else {
            int hours = duration / 3600;
            duration = duration - hours * 3600;
            int minutes = duration / 60;
            int seconds = duration - minutes * 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
