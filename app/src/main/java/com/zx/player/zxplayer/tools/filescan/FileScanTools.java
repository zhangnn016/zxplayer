package com.zx.player.zxplayer.tools.filescan;

import android.os.Handler;

import com.zx.player.tools.LooperManager;
import com.zx.player.zxplayer.objects.ObjectFileInfo;

import java.io.File;

/**
 * 文件扫描工具类
 * Created by leon on 15/8/15.
 */
public class FileScanTools {
    private static final String TAG = FileScanTools.class.getSimpleName();

    private Handler mMainHandler = LooperManager.getMainHanlder();

    /**
     * 扫描一个文件的信息
     * @param file
     * @param listener
     */
    public void scanFile(final File file, final IScanListener listener) {
        if (file != null && listener != null) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        ObjectFileInfo info = new ObjectFileInfo();
                        info.fileName = file.getName();
                        info.absPath = file.getAbsolutePath();
                        info.duration = 100;
                        info.thumbPath = null;
                        info.isSecret = false;
                        listener.onScanFinished(info.absPath, info);
                    }
                }
            }, 300);
        }
    }
}
