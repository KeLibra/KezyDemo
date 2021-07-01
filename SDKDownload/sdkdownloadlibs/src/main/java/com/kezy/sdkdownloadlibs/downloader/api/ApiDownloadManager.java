package com.kezy.sdkdownloadlibs.downloader.api;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.impls.EngineImpl;
import com.kezy.sdkdownloadlibs.listener.IDownloadStatusListener;

import java.io.File;
import java.util.Objects;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description
 */
public class ApiDownloadManager implements EngineImpl {

    /**
     * 系统DownloadManager
     */
    private DownloadManager downloadManager;

    /**
     * 下载监听
     */
    private ApiDownloadObserver downloadObserver;

    private IDownloadStatusListener mListener;

    private long mDownloadId;

    private String mDownloadUrl;


    @Override
    public void bindStatusChangeListener(IDownloadStatusListener listener) {
        mListener = listener;
    }


    @Override
    public long getTaskId() {

        return mDownloadId;
    }

    @Override
    public void startDownload(Context context, String url) {
        downLoadApk(context, url, "", "");
    }

    @Override
    public void pauseDownload(Context context, String url) {
    }

    @Override
    public void continueDownload(Context context, String url) {
    }

    @Override
    public void deleteDownload(Context context, String url) {
        try {
            downloadManager.remove(mDownloadId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void installApk(Context context, String url) {
        DownloadUtils.installApk(context, getDownloadFile(context));
    }

    /**
     * 获取下载的文件
     *
     * @return file
     */
    @Override
    public String getDownloadFile(Context context) {
        Log.e("-------msg", " getDownloadFile = ");
        long downloadId = getTaskId();
        if (downloadId <= 0) {
            return "";
        }
        Log.e("-------msg", " getDownloadFile = downloadId ---- " + downloadId);
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query.setFilterById(downloadId));
        if (cursor != null && cursor.moveToFirst()) {
            String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            Log.e("-------msg", " getDownloadFile = fileUri =  " + fileUri);
            if (!TextUtils.isEmpty(fileUri)) {
                String apkPath = Uri.parse(fileUri).getPath();
                if (!TextUtils.isEmpty(apkPath)) {
                    Log.e("-------msg", " apk path = " + apkPath);
                    return new File(apkPath).getPath();
                }
            }
            cursor.close();
        }
        return null;
    }

    @Override
    public void destroy() {

    }

    /**
     * 下载apky
     */
    @SuppressLint("MissingPermission")
    public void downLoadApk(Context context, String downloadUrl, String savePath, String appName) {

        this.mDownloadUrl = downloadUrl;
        Log.e("-------msg", " ----- downloadUrl ！" + downloadUrl);
        try {
            if (context != null) {
                if (!downLoadMangerIsEnable(context)) {
                    Log.e("-------msg", " ----- 下载器不可用！");
                    Toast.makeText(context, "下载器不可用！", Toast.LENGTH_LONG).show();
                    return;
                }
                // 获取下载管理器
                downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                clearCurrentTask();
                // 下载地址如果为null,抛出异常
                Uri uri = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                // 下载中和下载完成显示通知栏
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                if (TextUtils.isEmpty(appName)) {
                    appName = "应用.apk";
                } else {
                    if (!appName.contains(".apk")) {
                        appName = appName + ".apk";
                    }
                }

                if (TextUtils.isEmpty(savePath)) {
                    //使用系统默认的下载路径 此处为应用内 /android/data/packages ,所以兼容7.0
                    Log.v("--------msg", "下载路径 ----- " + (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.getPackageName() + ".apk")));
                    //设置下载的路径
                    File file = new File(getDiskCachePath(context) + "/download_apk/", appName);
                    request.setDestinationUri(Uri.fromFile(file));
                    deleteApkFile(file);
                } else {
                    //设置下载的路径
                    File file = new File(getDiskCachePath(context) + "/" + savePath + "/", appName);
                    request.setDestinationUri(Uri.fromFile(file));

                    deleteApkFile(Objects.requireNonNull(file));
                    Log.v("--------msg", "下载路径 ----- " + (context.getExternalFilesDir(savePath + File.separator + context.getPackageName() + ".apk")));
                    Log.v("--------msg", "下载路径 ----- " + (context.getCacheDir().toString()));
                }
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                // 部分机型（暂时发现Nexus 6P）无法下载，猜测原因为默认下载通过计量网络连接造成的，通过动态判断一下
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    boolean activeNetworkMetered = connectivityManager.isActiveNetworkMetered();
                    request.setAllowedOverMetered(activeNetworkMetered);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    request.allowScanningByMediaScanner();
                }
                // 设置通知栏的标题
                request.setTitle(appName);
                // 设置通知栏的描述
                request.setDescription("正在下载中...");
                // 设置媒体类型为apk文件
                request.setMimeType("application/vnd.android.package-archive");
                // 开启下载，返回下载id
                mDownloadId = downloadManager.enqueue(request);
                // 如需要进度及下载状态，增加下载监听
                downloadObserver = new ApiDownloadObserver(context, mDownloadId);
                context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver);
                Toast.makeText(context, "apk 开始下载", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 防止有些厂商更改了系统的downloadManager
        }
    }

    /**
     * 下载前清空本地缓存的文件
     */
    private void deleteApkFile(File destFileDir) {
        if (!destFileDir.exists()) {
            return;
        }
        if (destFileDir.isDirectory()) {
            File[] files = destFileDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteApkFile(f);
                }
            }
        }
        destFileDir.delete();
    }

    /**
     * 清除上一个任务，防止apk重复下载
     */
    public void clearCurrentTask() {
        try {
            downloadManager.remove(getTaskId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * downloadManager 是否可用
     *
     * @param context 上下文
     * @return true 可用
     */
    private boolean downLoadMangerIsEnable(Context context) {
        int state = context.getApplicationContext().getPackageManager()
                .getApplicationEnabledSetting("com.android.providers.downloads");
        return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED);
    }


    /**
     * 取消下载的监听
     */
    public void unregisterContentObserver(Context context) {
        context.getContentResolver().unregisterContentObserver(downloadObserver);
    }




    @Nullable
    public String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() == null) {
                if (context.getCacheDir() == null) {
                    return null;
                }
                return context.getCacheDir().getPath();
            }
            return context.getExternalCacheDir().getPath();
        } else {
            if (context.getCacheDir() != null) {
                return context.getCacheDir().getPath();
            }

            return null;
        }
    }


    public class ApiDownloadObserver extends ContentObserver {

        private final String TAG = "-------msg" + getClass().getCanonicalName();
        /**
         * 记录成功或者失败的状态，主要用来只发送一次成功或者失败
         */
        private boolean isEnd = false;

        private final DownloadManager.Query query;

        private long mDownloadId;

        private Context mContext;

        /**
         * Creates a content observer.
         *
         */
        public ApiDownloadObserver(Context context, long downloadId) {
            super(new Handler());
            this.mDownloadId = downloadId;
            this.mContext = context;
            query = new DownloadManager.Query().setFilterById(downloadId);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            queryDownloadStatus();
        }

        private int lastProgress;
        private boolean isStarted = false;
        /**
         * 检查下载的状态
         */
        private void queryDownloadStatus() {
            // Java 7 新的 try-with-resources ，凡是实现了AutoCloseable接口的可自动close()，所以此处不需要手动cursor.close()
            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor != null && cursor.moveToNext()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    long totalSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    long currentSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    // 当前进度
                    int mProgress;
                    if (totalSize != 0) {
                        mProgress = (int) ((currentSize * 100) / totalSize);
                    } else {
                        mProgress = 0;
                    }
                    if (lastProgress == mProgress){
                        return;
                    }

                    Log.d(TAG, String.valueOf(mProgress));
                    switch (status) {
                        case DownloadManager.STATUS_PAUSED:
                            if (mListener != null) {
                                mListener.onPause("");
                            }
                            Log.d(TAG, "STATUS_PAUSED");
                            break;
                        case DownloadManager.STATUS_PENDING:
                            // 开始下载
                            if (mListener != null) {
                                mListener.onStart("", false, totalSize);
                                isStarted = true;
                            }
                            Log.d(TAG, "STATUS_PENDING");
                            break;
                        case DownloadManager.STATUS_RUNNING:
                            if (!isStarted && mProgress == 0) {
                                /*
                                 * fix 系统下载器偶尔获取不到开始下载通知， 所以当没有获取到时，开始下载progress=0时补报一次
                                 */
                                mListener.onStart("", false, totalSize);
                                isStarted = true;
                            } else {
                                if (mListener != null) {
                                    mListener.onProgress("", mProgress);
                                }
                            }
                            Log.d(TAG, "STATUS_RUNNING");
                            break;
                        case DownloadManager.STATUS_SUCCESSFUL:
                            if (!isEnd) {
                                // 完成
                                if (mListener != null) {
                                    mListener.onSuccess("", getDownloadFile(mContext));
                                }
                                Log.d(TAG, "STATUS_SUCCESSFUL");
                                installApk(mContext, getDownloadFile(mContext));
                            }
                            isEnd = true;
                            break;
                        case DownloadManager.STATUS_FAILED:
                            if (!isEnd) {
                                if (mListener != null) {
                                    mListener.onError("");
                                }
                                Log.d(TAG, "STATUS_FAILED");
                            }
                            isEnd = true;
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
