package com.zx.player.tools;

/**
 * 磁盘缓存类，基于DiskLurCache
 * Created by niuniuzhang on 15/7/21.
 */
public class DiskCache implements IDiskCache {

    public static final String TAG = DiskCache.class.getSimpleName();

    private ImageMemoryCache mMemoryCache;

    public DiskCache(ImageMemoryCache memoryCache) {
        mMemoryCache = memoryCache;
    }

}
