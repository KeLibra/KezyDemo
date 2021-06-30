package com.kezy.sdkdownloadlibs.downloader.xima;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.listener.DownloadStatusChangeListener;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.manager.EngineImpl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.DOWNLOAD_APK_NAME;
import static com.kezy.sdkdownloadlibs.downloader.xima.DownloadService.DOWNLOAD_APK_URL;

/**
 */
public class DownloadServiceManage implements EngineImpl<String> {

    private boolean mConnected = false;

    private Context mContext;

    public DownloadServiceManage(Context context) {
        mContext = context.getApplicationContext();
        init(context);
    }

    @Nullable
    private DownloadService mDownloadService;

    private DownloadInfo mInfo;

    private DownloadStatusChangeListener mListener;


    @Override
    public void bindDownloadInfo(DownloadInfo info) {

        Log.e("----------", " -------- bindDownloadInfo " + mDownloadService);
        if (mDownloadService != null) {
            mDownloadService.setDownloadInfo(info);
        } else {
            mInfo = info;
        }
    }

    @Override
    public void bindStatusChangeListener(DownloadStatusChangeListener listener) {
        if (mDownloadService != null) {
            mDownloadService.addDownloadStatueListener(listener);
        } else {
            mListener = listener;
        }
    }

    @Override
    public DownloadInfo getInfo() {
        Log.i("-------msg", " v2 manager info " + mDownloadService.getDownloadInfoByUrl(mInfo.url));
        return mDownloadService.getDownloadInfoByUrl(mInfo.url);
    }

    @Override
    public long getTaskId() {
        return -1;
    }

    @Override
    public void startDownload(Context context) {
        downLoadAPK(mInfo.url);
    }

    @Override
    public void pauseDownload(Context context) {
        if (!checkConnectionStatus(context)) {
            return;
        }
        if (mDownloadService != null) {
            mDownloadService.pauseDownload(mInfo.url);
        }
    }

    @Override
    public void continueDownload(Context context) {

        if (!checkConnectionStatus(context)) {
            downLoadAPK(mInfo.url);
            return;
        }
        if (mDownloadService != null) {
            mDownloadService.startDownload(mInfo.url);
        }
    }

    @Override
    public void deleteDownload(Context context) {
        if (!checkConnectionStatus(context)) {
            return;
        }
        if (mDownloadService != null) {
            mDownloadService.removeDownload(mInfo.url);
        }
    }

    @Override
    public int getStatus(Context context) {
        return mDownloadService.getStatueByUrl(mInfo.url);
    }

    @Override
    public void installApk(Context context) {
        if (mDownloadService != null) {
            DownloadUtils.installApk(mContext, mDownloadService.getDownloadSavePath(mInfo.url));
        }
    }

    @Override
    public String getDownloadFile(Context context) {
        return mDownloadService.getDownloadSavePath(mInfo.url);
    }

    @Override
    public int getDownloaderType() {
        return DownloadType.TYPE_XIMA;
    }

    @Override
    public void destroy() {
        mDownloadService.removeAllListener();
        mDownloadService.unbindService(mConn);
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }
        Log.e("----------", " -------- init ");
        context.bindService(new Intent(context, DownloadService.class), mConn, Context.BIND_AUTO_CREATE);
    }


    public void unBindDownloadService(Context context) {
        try {
            if (mConnected) {
                context.unbindService(mConn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("----------", " -------- onServiceConnected ");
            if (service instanceof DownloadService.Binder) {
                mConnected = true;
                mDownloadService = ((DownloadService.Binder) service).getService();
                mDownloadService.setDownloadInfo(mInfo);
                mDownloadService.addDownloadStatueListener(mListener);
            }
        }
    };

    private boolean checkConnectionStatus(Context context) {
        if (!mConnected || mDownloadService == null) {
            init(context);
            return false;
        }
        return true;
    }

    public int getStatueByUrl(String url) {

        if (mDownloadService != null) {
            return mDownloadService.getStatueByUrl(url);
        }

        return DownloadInfo.Status.WAITING;
    }

    @Nullable
    public String getDownloadSavePath(String url) {

        if (mDownloadService != null) {
            return mDownloadService.getDownloadSavePath(url);
        }

        return null;
    }

    public boolean isDowning(String url) {

        if (mDownloadService != null) {
            return mDownloadService.isDowning(url);
        }
        return false;
    }



    public void downLoadAPK(final String downUrl, final String fileName) {
        if (TextUtils.isEmpty(downUrl)) {
            return;
        }

        try {
            goDownloadApk(URLDecoder.decode(downUrl, "utf-8"), fileName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void downLoadAPK(final String downUrl) {
        downLoadAPK(downUrl, getFileNameByDownLoadUrl(downUrl));
    }


    private String getFileNameByDownLoadUrl(String downloadUrl) {
        if (TextUtils.isEmpty(downloadUrl)) {
            return System.currentTimeMillis() + "";
        }

        URL url = null;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (url == null) {
            return System.currentTimeMillis() + "";
        }

        String fileName = new File(url.getPath()).getName();
        if (TextUtils.isEmpty(fileName)) {
            return System.currentTimeMillis() + "";
        }

        if (fileName.length() > 50) {
            fileName = fileName.substring(0, 50);
        }

        return fileName;
    }

    private void goDownloadApk(String downloadUrl, String fileName) {
        if (mContext == null) {
            return;
        }
        if (!mConnected) {
            init(mContext);
        }

        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DOWNLOAD_APK_URL, downloadUrl);
        if (!TextUtils.isEmpty(fileName)) {
            intent.putExtra(DOWNLOAD_APK_NAME, fileName);
        }
        mContext.startService(intent);
    }
}
