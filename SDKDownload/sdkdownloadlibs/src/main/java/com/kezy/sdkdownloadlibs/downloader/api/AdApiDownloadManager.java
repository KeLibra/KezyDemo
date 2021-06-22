package com.kezy.sdkdownloadlibs.downloader.api;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.EngineImpl;

import java.io.File;
import java.util.Objects;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description
 */
public class AdApiDownloadManager implements EngineImpl<Long> {

    /**
     * 系统DownloadManager
     */
    private DownloadManager downloadManager;

    /**
     * 下载监听
     */
    private AdApiDownloadObserver downloadObserver;

    private DownloadInfo mInfo;


    @Override
    public void bindDownloadInfo(DownloadInfo info) {
        mInfo = info;
    }

    @Override
    public DownloadInfo getInfo(String url) {
        return mInfo;
    }

    @Override
    public long getTaskId(String downloadUrl) {

        return mInfo.taskId;
    }

    @Override
    public void startDownload(Context context, String downloadUrl) {
        downLoadApk(context, downloadUrl, "", "");
    }

    @Override
    public void pauseDownload(Context context, String downloadUrl) {
    }

    @Override
    public void continueDownload(Context context, String downloadUrl) {
    }

    @Override
    public void deleteDownload(Context context, String downloadUrl) {
        try {
            downloadManager.remove(mInfo.taskId);
            mInfo.status = Status.DELETE;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getStatus(Context context, String downloadUrl) {
        if (mInfo != null) {
            return mInfo.status;
        }
        return Status.WAITING;

    }

    /**
     * 下载apky
     */
    @SuppressLint("MissingPermission")
    public void downLoadApk(Context context, String downloadUrl, String savePath, String appName) {

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
                clearCurrentTask(downloadUrl);
                // 下载地址如果为null,抛出异常
                Uri uri = Uri.parse(downloadUrl);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                // 下载中和下载完成显示通知栏
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                if (TextUtils.isEmpty(appName)) {
                    appName = "应用";
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
                long lastDownloadId = downloadManager.enqueue(request);
                createTask(lastDownloadId, appName);
                // 如需要进度及下载状态，增加下载监听
                AdApiDownloadHandler downloadHandler = new AdApiDownloadHandler();
                downloadObserver = new AdApiDownloadObserver(downloadHandler, downloadManager, lastDownloadId);
                context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver);
                Toast.makeText(context, "apk 开始下载", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 防止有些厂商更改了系统的downloadManager
        }
    }

    private void createTask(long lastDownloadId, String appName) {

        // 如果 已有task， 则更新taskID。 若无task， 则创建
        mInfo.taskId = lastDownloadId;
        mInfo.name = appName;
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
    public void clearCurrentTask(String url) {
        try {
            downloadManager.remove(getTaskId(url));
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

    /**
     * 安装app
     *
     * @param apkFile 下载的文件
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void installApp(Context context, File apkFile) {
        try {
            // 安装
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            } else {
                boolean allowInstall = context.getPackageManager().canRequestPackageInstalls();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!allowInstall) {
                        //不允许安装未知来源应用，请求安装未知应用来源的权限
                        return;
                    }
                }
                //Android7.0之后获取uri要用contentProvider
                Uri apkUri = FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileProvider", apkFile);
                //Granting Temporary Permissions to a URI
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取下载的文件
     *
     * @return file
     */
    @Override
    public String getDownloadFile(Context context, String downloadUrl) {
        Log.e("-------msg", " getDownloadFile = " + downloadUrl);
        long downloadId = getTaskId(downloadUrl);
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


    private DownloadListener mDownloadListener;

    public void setDownloadListener(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
    }

    public interface DownloadListener {
        void downloadSuccess(String path);

        void downloadErr();
    }

    public class AdApiDownloadHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            DownloadInfo task = (DownloadInfo) msg.obj;

            if (task == null) {
                return;
            }
            Log.e("--------msg", " -------- 下载 状态 --- " + task.url);

            if (mInfo != null) {
                mInfo.status = task.status;
                mInfo.isRunning = task.isRunning;
                mInfo.progress = task.progress;
                mInfo.totalSize = task.totalSize;
                mInfo.tempSize = task.tempSize;
                if (getDownloadFile(null, mInfo.url) != null && TextUtils.isEmpty(mInfo.path)) {
                    mInfo.path = getDownloadFile(null, mInfo.url);
                }
            }
        }
    }
}
