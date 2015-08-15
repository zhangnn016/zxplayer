package com.zx.player.zxplayer.tools.filescan;

import com.zx.player.zxplayer.objects.ObjectFileInfo;

/**
 * 文件扫描的listener
 * Created by leon on 15/8/9.
 */
public interface IScanListener {

    public void onScanFinished(String filePath, ObjectFileInfo object);

    public void onScanException(String filePath, int code, String reason);
}
