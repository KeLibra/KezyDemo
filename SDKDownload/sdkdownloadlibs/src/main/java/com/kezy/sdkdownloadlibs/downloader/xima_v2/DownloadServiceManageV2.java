package com.kezy.sdkdownloadlibs.downloader.xima_v2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.EngineImpl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import static com.kezy.sdkdownloadlibs.downloader.xima_v2.DownloadServiceV2.DOWNLOAD_APK_NAME;
import static com.kezy.sdkdownloadlibs.downloader.xima_v2.DownloadServiceV2.DOWNLOAD_APK_URL;

/**
 */
public class DownloadServiceManageV2 implements EngineImpl<String> {

    private boolean mConnected = false;

    private Context mContext;
    public DownloadServiceManageV2(Context context) {
        mContext = context.getApplicationContext();
        init(context);
    }

    @Nullable
    private DownloadServiceV2 mDownloadService;

    private DownloadInfo mInfo;


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
    public DownloadInfo getInfo(String url) {
        Log.i("-------msg", " v2 manager info " + mDownloadService.getDownloadInfo(url));
        return mDownloadService.getDownloadInfo(url);
    }

    @Override
    public long getTaskId(String downloadUrl) {
        return -1;
    }

    @Override
    public void startDownload(Context context, String downloadUrl) {
        downLoadAPK(downloadUrl);
    }

    @Override
    public void pauseDownload(Context context, String downloadUrl) {
        if (!checkConnectionStatus(context)) {
            return;
        }
        if (mDownloadService != null) {
            mDownloadService.pauseDownload(downloadUrl);
        }
    }

    @Override
    public void continueDownload(Context context, String downloadUrl) {

        if (!checkConnectionStatus(context)) {
            downLoadAPK(downloadUrl);
            return;
        }
        if (mDownloadService != null) {
            mDownloadService.startDownload(downloadUrl);
        }
    }

    @Override
    public void deleteDownload(Context context, String downloadUrl) {
        if (!checkConnectionStatus(context)) {
            return;
        }
        if (mDownloadService != null) {
            mDownloadService.removeDownload(downloadUrl);
        }
    }

    @Override
    public int getStatus(Context context, String downloadUrl) {
        return mDownloadService.getStatueByUrl(downloadUrl);
    }

    @Override
    public String getDownloadFile(Context context, String downloadUrl) {
        return mDownloadService.getDownloadSavePath(downloadUrl);
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }
        Log.e("----------", " -------- init ");
        context.bindService(new Intent(context, DownloadServiceV2.class), mConn, Context.BIND_AUTO_CREATE);
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
            if (service instanceof DownloadServiceV2.Binder) {
                mConnected = true;
                mDownloadService = ((DownloadServiceV2.Binder) service).getService();
                mDownloadService.setDownloadInfo(mInfo);
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


    private void onServiceConnectedCallBack() {
    }
    public int getStatueByUrl(String url) {

        if (mDownloadService != null) {
            return mDownloadService.getStatueByUrl(url);
        }

        return Status.WAITING;
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

        Intent intent = new Intent(mContext, DownloadServiceV2.class);
        intent.putExtra(DOWNLOAD_APK_URL, downloadUrl);
        if (!TextUtils.isEmpty(fileName)) {
            intent.putExtra(DOWNLOAD_APK_NAME, fileName);
        }
        mContext.startService(intent);
    }


}
