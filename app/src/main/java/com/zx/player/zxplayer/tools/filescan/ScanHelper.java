package com.zx.player.zxplayer.tools.filescan;

import android.text.TextUtils;

import com.zx.player.utils.ZXLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件扫描类，负责文件扫描
 * Created by leon on 15/8/10.
 */
public class ScanHelper {
    public static final String TAG = ScanHelper.class.getSimpleName();

    /**
     * 并非单例模式，只是避免了外部随意new对象
     * @return
     */
    public static ScanHelper instance(IScanListener listener) {
        return new ScanHelper(listener);
    }

    private int mDirDepth; // 递归深度
    private static final int MAX_DIR_DEPTH = 5; // 单个目录的最大递归深度
    private List<File> mScanningFiles;

    private boolean mIsThreadRunning;
    private Thread mScanThread;
    private Object mSynLock;
    private IScanListener mListener;
    private FileScanTools mScanTools;
    private Pattern mSuffPattern;

    private static final String[] EXTENSIONS = {"mp4", "avi", "rmvb", "rm", "flv", "mkv", "wmv"};

    private ScanHelper(IScanListener listener) {
        mDirDepth = 0;
        mScanningFiles = new ArrayList<>();
        mIsThreadRunning = false;
        mSynLock = new Object();
        mScanThread = null;
        mScanTools = new FileScanTools();

        mSuffPattern = Pattern.compile(getPatternString(EXTENSIONS));
        mListener = listener;
        ZXLog.d(TAG, "Pattern is " + mSuffPattern.pattern());
    }

    /**
     * 添加一些扫描一级目录
     * @param fileList
     */
    public void addRootFiles(List<File> fileList) {
        if (fileList != null && fileList.size() > 0) {
            synchronized (mScanningFiles) {
                mScanningFiles.addAll(fileList);
            }
        }
        if (mScanningFiles.size() > 0 && mScanThread == null) {
            mScanThread = new Thread(new scanRunnable());
            mScanThread.start();
        }
        synchronized (mSynLock) {
            mSynLock.notify();
        }
    }

    /**
     * 添加一些扫描一级目录
     * @param pathList
     */
    public void addRootFolders(List<String> pathList) {
        if (pathList != null && pathList.size() > 0) {
            List<File> fileList = new ArrayList<>();
            for (String name : pathList) {
                if (!TextUtils.isEmpty(name)) {
                    File file = new File(name);
                    fileList.add(file);
                }
            }
            synchronized (mScanningFiles) {
                mScanningFiles.addAll(fileList);
            }
        }
        if (mScanningFiles.size() > 0 && mScanThread == null) {
            mScanThread = new Thread(new scanRunnable());
            mScanThread.start();
        }
        synchronized (mSynLock) {
            mSynLock.notify();
        }
    }

    private class scanRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                int curSize = 0;
                synchronized (mScanningFiles) {
                    curSize = mScanningFiles.size();
                }
                if (curSize == 0) {
                    synchronized (mSynLock) {
                        try {
                            mSynLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                File file = mScanningFiles.get(0);
                mScanningFiles.remove(0);
                mDirDepth = 0;
                if (file != null) {
                    scanFolder(file);
                }
            }
        }
    }

    private void scanFolder(File file) {
        if (mDirDepth >= MAX_DIR_DEPTH) {
            return;
        }
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File inner : files) {
                        scanFolder(inner);
                    }
                }
                mDirDepth++;
            } else {
                scanFile(file);
            }
        }
    }

    private void scanFile(File file) {
        if (file != null && file.exists() && !file.isDirectory()) {
            String fileName = file.getName();
            ZXLog.d(TAG, "scan File " + fileName);
            if (!TextUtils.isEmpty(fileName)) {
                int pos = fileName.lastIndexOf(".");
                if (pos > 0) {
                    fileName = fileName.substring(pos + 1, fileName.length()); // 取后缀名
                }
            }
            if (!TextUtils.isEmpty(fileName)) {
                fileName = fileName.toLowerCase();
                Matcher matcher = mSuffPattern.matcher(fileName);
                if (matcher.matches()) {
                    mScanTools.scanFile(file, mListener);
                }
            }
        }
    }

    private String getPatternString(String[] list) {
        StringBuilder builder = new StringBuilder();
        if (list != null && list.length > 0) {
            builder.append("//");
            for (String name : list) {
                if (!TextUtils.isEmpty(name)) {
                    builder.append(name);
                    builder.append("|");
                }
            }
        }
        return builder.toString();
    }
}
