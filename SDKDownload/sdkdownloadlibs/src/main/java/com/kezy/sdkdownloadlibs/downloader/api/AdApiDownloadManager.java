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

import com.kezy.sdkdownloadlibs.task.DownloadTask;
import com.kezy.sdkdownloadlibs.task.TaskImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description
 */
public class AdApiDownloadManager implements TaskImpl<Long> {

    /**
     * 系统DownloadManager
     */
    private DownloadManager downloadManager;

    /**
     * 下载监听
     */
    private AdApiDownloadObserver downloadObserver;

    /**
     * @Fields mDownloadTaskList : 正在下载的任务
     */
    private List<DownloadTask> mDownloadTaskList = new ArrayList<>();

    @Override
    public int getDownloadType() {
        return DownloadType.TYPE_API;
    }

    @Override
    public Long createDownloadKey(Context context, String downloadUrl) {

        if (getTaskByUrl(downloadUrl) != null) {
            return getTaskByUrl(downloadUrl).taskId;
        }
       return -1L;
    }

    @Override
    public long getTaskId(String downloadUrl) {
        if (getTaskByUrl(downloadUrl) != null) {
            return getTaskByUrl(downloadUrl).taskId;
        }
        return -1L;
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
            if (getTaskByUrl(downloadUrl) != null) {
              downloadManager.remove(getTaskByUrl(downloadUrl).taskId);
                getTaskByUrl(downloadUrl).status = Status.DELETE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getStatus(Context context, String downloadUrl) {
        if (getTaskByUrl(downloadUrl) != null) {
            return getTaskByUrl(downloadUrl).status;
        }
        return Status.WAITING;

    }

    /**
     * 下载apky
     */
    @SuppressLint("MissingPermission")
    public void downLoadApk(Context context, String downloadUrl,String savePath, String appName) {

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
                    File file = new File(getDiskCachePath(context) + "/"+ savePath +"/", appName);
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
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
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
                DownloadTask task = createTask(downloadUrl, lastDownloadId, appName);
                if (task != null) {
                    mDownloadTaskList.add(task);
                }
                Log.e("------msg", " ---- url 111  map 222 = " + mDownloadTaskList.toString());
                // 如需要进度及下载状态，增加下载监听
                AdApiDownloadHandler downloadHandler = new AdApiDownloadHandler();
                downloadObserver = new AdApiDownloadObserver(downloadHandler, downloadManager, lastDownloadId);
                context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver);
                Toast.makeText(context,"apk 开始下载", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 防止有些厂商更改了系统的downloadManager
        }
    }

    private DownloadTask createTask(String downloadUrl, long lastDownloadId, String appName) {

        // 如果 已有task， 则更新taskID。 若无task， 则创建
        if (getTaskByUrl(downloadUrl) != null) {
            getTaskByUrl(downloadUrl).taskId = lastDownloadId;
        } else {
            DownloadTask task = new DownloadTask();
            task.taskId = lastDownloadId;
            task.url = downloadUrl;
            task.name = appName;
            return task;
        }
        return null;
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
            if (getTaskByUrl(url) != null) {
                downloadManager.remove(getTaskId(url));
            }
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
        long downloadId = getTaskId(downloadUrl);
        if (downloadId <= 0) {
            return "";
        }
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query.setFilterById(downloadId));
        if (cursor != null && cursor.moveToFirst()) {
            String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String apkPath = Uri.parse(fileUri).getPath();
            if (!TextUtils.isEmpty(apkPath)) {
                Log.e("-------msg", " apk path = " + apkPath);
                return new File(apkPath).getPath();
            }
            cursor.close();
        }
        return null;
    }

    @Nullable
    public String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if(context.getExternalCacheDir() == null) {
                if(context.getCacheDir() == null) {
                    return null;
                }
                return context.getCacheDir().getPath();
            }
            return context.getExternalCacheDir().getPath();
        } else {
            if(context.getCacheDir() != null) {
                return context.getCacheDir().getPath();
            }

            return null;
        }
    }





    private DownloadListener mDownloadListener;

    public void setDownloadListener(DownloadListener downloadListener) {
        this.mDownloadListener = downloadListener;
    }

    public interface DownloadListener{
        void downloadSuccess(String path);
        void downloadErr();
    }

    public class AdApiDownloadHandler extends Handler {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            DownloadTask task = (DownloadTask) msg.obj;

            Log.e("--------msg", " -------- 下载 状态 --- "  + task);
            if (task == null) {
                return;
            }
            if (getTaskById(task.taskId) != null) {
                getTaskById(task.taskId).status = task.status;
                getTaskById(task.taskId).isRunning = task.isRunning;
                getTaskById(task.taskId).progress = task.progress;
                getTaskById(task.taskId).totalSize = task.totalSize;
                getTaskById(task.taskId).tempSize = task.tempSize;
                if (getDownloadFile(null, task.url)!= null && TextUtils.isEmpty(task.path)) {
                    getTaskById(task.taskId).path = getDownloadFile(null, task.url);
                }
            } else {
                if (getDownloadFile(null, task.url)!= null && TextUtils.isEmpty(task.path)) {
                    task.path = getDownloadFile(null, task.url);
                }
                mDownloadTaskList.add(task);
            }
        }
    }


    public DownloadTask getTaskByUrl(String url) {
        if (mDownloadTaskList == null || TextUtils.isEmpty(url)) {
            return null;
        }
        for (int i = 0; i < mDownloadTaskList.size(); i++) {
            if (url.equals(mDownloadTaskList.get(i).url)) {
                return mDownloadTaskList.get(i);
            }
        }
        return null;
    }

    private DownloadTask getTaskById(long downloadId) {
        if (mDownloadTaskList == null || downloadId < 0) {
            return null;
        }

        for (DownloadTask task : mDownloadTaskList) {
            if (task != null && downloadId == task.taskId) {
                return task;
            }
        }
        return null;
    }
}
