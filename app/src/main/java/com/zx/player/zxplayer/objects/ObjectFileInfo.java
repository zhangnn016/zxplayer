package com.zx.player.zxplayer.objects;

import java.io.Serializable;

/**
 * 文件信息，包括视频宽高，类型等等
 * Created by leon on 15/8/9.
 */
public class ObjectFileInfo implements Serializable {

    public String absPath;

    public String fileName;

    public int duration;

    public boolean isSecret;

    public int width;

    public int height;

    public String thumbPath;

    public int thumbWidth;

    public int thumbHeight;
}
