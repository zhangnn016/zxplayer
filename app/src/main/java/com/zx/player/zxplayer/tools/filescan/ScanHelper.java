package com.zx.player.zxplayer.tools.filescan;

import java.io.File;

/**
 * 文件扫描类，负责文件扫描
 * Created by leon on 15/8/10.
 */
public class ScanHelper {

    public static final String TAG = ScanHelper.class.getSimpleName();

    public static ScanHelper instance() {
        return new ScanHelper();
    }

    private int mDirDepth; // 递归深度

    private ScanHelper() {
        mDirDepth = 0;
    }

    private void scanFolder(File file, String[] extensions) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {

            } else {
                scanFile(file, extensions);
            }
        }
    }

    private void scanFile(File file, String[] extensions) {
        if (file != null && file.exists() && !file.isDirectory()) {

        }
    }
}
