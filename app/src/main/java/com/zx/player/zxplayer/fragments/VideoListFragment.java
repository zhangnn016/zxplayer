package com.zx.player.zxplayer.fragments;

import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.zx.player.utils.ZXLog;
import com.zx.player.utils.ZXToast;
import com.zx.player.zxplayer.R;
import com.zx.player.zxplayer.adapters.VideoListAdapter;
import com.zx.player.zxplayer.base.BaseFragment;
import com.zx.player.zxplayer.objects.ObjectFileInfo;
import com.zx.player.zxplayer.tools.filescan.IScanListener;
import com.zx.player.zxplayer.tools.filescan.ScanHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * 主页的视频列表fragment
 * Created by leon on 15/8/9.
 */
public class VideoListFragment extends BaseFragment {
    private static final String TAG = VideoListFragment.class.getSimpleName();

    private PtrClassicFrameLayout mRefreshBase;
    private ListView mListView;
    private VideoListAdapter mListAdapter;
    private List<ObjectFileInfo> mDataList;

    private ScanHelper mScanHelper;

    private static final String[] rootPaths = {"/Video", "/Movie", "/Movies", "/DCIM"};

    @Override
    protected int getResourceLayout() {
        return R.layout.fragment_home_videolist;
    }

    /**
     * 文件扫描监听
     */
    private IScanListener mScanFileListener = new IScanListener() {
        @Override
        public void onScanFinished(String filePath, ObjectFileInfo object) {
            if (!TextUtils.isEmpty(filePath) && object != null) {
                mListAdapter.addToBack(object);
            }
        }

        @Override
        public void onScanException(String filePath, int code, String reason) {
            ZXLog.d(TAG, "onScanException, " + filePath);
        }
    };

    @Override
    protected void initViews(View baseView) {
        mRefreshBase = (PtrClassicFrameLayout)baseView.findViewById(R.id.ptr_main);
        mListView = (ListView)baseView.findViewById(R.id.list_video);
        mDataList = new ArrayList<>();
        mListAdapter = new VideoListAdapter(getActivity(), mDataList);
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ZXToast.showCenterToast("onItemClick, " + mDataList.get(position).fileName);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ZXToast.showCenterToast("onItemLongClick, " + mDataList.get(position).fileName);
                return true;
            }
        });

        mRefreshBase.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                scanFiles();
                mRefreshBase.refreshComplete();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshBase.autoRefresh();
            }
        }, 100);
    }

    @Override
    protected void doInit() {
        mScanHelper = ScanHelper.instance(mScanFileListener);
    }

    private void scanFiles() {
        List<String> paths = new ArrayList<>();
        List<String> sdCards = getAbslutePath(rootPaths);
        if (sdCards != null && sdCards.size() > 0) {
            for (String sdcard : sdCards) {
                if (!TextUtils.isEmpty(sdcard)) {
                    for (String folder : rootPaths) {
                        paths.add(sdcard + folder);
                    }
                }
            }
        }
        mScanHelper.addRootFolders(paths);
    }

    private List<String> getAbslutePath(String[] rootPaths) {
        List<String> sdCards = new ArrayList<>();
        String innerSd = getInnerSDCardPath();
        if (!TextUtils.isEmpty(innerSd)) {
            sdCards.add(getInnerSDCardPath());
        }
        List<String> tmp = getExtSDCardPath();
        if (tmp != null && tmp.size() > 0) {
            sdCards.addAll(tmp);
        }
        return sdCards;
    }


    /**
     * 获取内置SD卡路径
     * @return
     */
    public String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取外置SD卡路径
     * @return	应该就一条记录或空
     */
    public List<String> getExtSDCardPath()
    {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard"))
                {
                    String [] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory())
                    {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return lResult;
    }
}
