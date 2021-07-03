package com.kezy.downloadaa.apidownload;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description
 */
public class AdApiDownloadManager {

    /**
     * 系统DownloadManager
     */
    private DownloadManager downloadManager;
    /**
     * 上次下载的id
     */
    private long lastDownloadId = -1;

    /**
     * 下载监听
     */
    private AdApiDownloadObserver downloadObserver;

    private Map<String, String> urlIdMap = new HashMap<>();
    private Map<String, DownloadManager.Request> IdRequestMap = new HashMap<>();

    private AdApiDownloadManager() {
    }

    public static AdApiDownloadManager getInstance() {
        return HolderClass.instance;
    }

    private static class HolderClass {
        private static AdApiDownloadManager instance = new AdApiDownloadManager();
    }

    public void downLoadApk(Context context, String downloadUrl) {
        downLoadApkWithName(context, downloadUrl, "");
    }
    public void downLoadApkWithName(Context context, String downloadUrl, String apkName) {
        downLoadApk(context, downloadUrl, "", apkName);
    }

    public void  downLoadApkWithPath(Context context, String downloadUrl, String savePath) {
        downLoadApk(context, downloadUrl, savePath, "");
    }
    /**
     * 下载apky
     */
    @SuppressLint("MissingPermission")
    public void downLoadApk(Context context, String downloadUrl,String savePath, String appName) {

        if (urlIdMap.get(downloadUrl) != null) {
            Toast.makeText(context, "应用正在下载,ID = " + urlIdMap.get(downloadUrl), Toast.LENGTH_LONG).show();
            DownloadManager.Request request = IdRequestMap.get(urlIdMap.get(downloadUrl));
            if (downloadManager != null) {
                downloadManager.enqueue(request);
            }
            return;
        }

        try {
            if (context != null) {
                if (!downLoadMangerIsEnable(context)) {
                    downFromBrowser(context, downloadUrl);
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
                    appName = "apk应用";
                }

                if (TextUtils.isEmpty(savePath)) {
                    //使用系统默认的下载路径 此处为应用内 /android/data/packages ,所以兼容7.0
//                    request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, context.getPackageName() + ".apk");
//                    deleteApkFile(Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.getPackageName() + ".apk")));
                    Log.v("--------msg", "下载路径 ----- " + (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + File.separator + context.getPackageName() + ".apk")));
                    //设置下载的路径
                    File file = new File(getDiskCachePath(context) + "/download_apk/", appName+".apk");
                    request.setDestinationUri(Uri.fromFile(file));
                    deleteApkFile(file);
                } else {
                    // 自定义的下载目录,注意这是涉及到android Q的存储权限，建议不要用getExternalStorageDirectory（）
//                    request.setDestinationInExternalFilesDir(context, savePath, context.getPackageName() + ".apk");
//                    request.setDestinationInExternalPublicDir("/download_apk", appName+".apk");
//                    request.setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().getAbsolutePath() , appName+".apk");

                    //设置下载的路径
                    File file = new File(getDiskCachePath(context) + "/"+ savePath +"/", appName+".apk");
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
                lastDownloadId = downloadManager.enqueue(request);
                urlIdMap.put(downloadUrl, lastDownloadId+"");
                IdRequestMap.put(lastDownloadId+"", request);
                Log.e("------msg", " ---- url 111  map = " + urlIdMap.toString());
                saveLocalData(context);
                // 如需要进度及下载状态，增加下载监听
                AdApiDownloadHandler downloadHandler = new AdApiDownloadHandler(context, this);
                downloadObserver = new AdApiDownloadObserver(downloadHandler, downloadManager, lastDownloadId);
                context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver);
                Toast.makeText(context,"apk 开始下载", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 防止有些厂商更改了系统的downloadManager
        }
    }

    private void saveLocalData(Context context) {
        SharedPreferences sharedPreferences= context.getSharedPreferences("data",Context.MODE_PRIVATE);
        //步骤2： 实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //步骤3：将获取过来的值放入文件
        editor.putString("urlIdMap", urlIdMap.toString());
        Log.e("------msg", " ---- url map = " + urlIdMap.toString());
        editor.putString("IdRequestMap", IdRequestMap.toString());
        Log.e("------msg", " ---- id  map = " + IdRequestMap.toString());
        //步骤4：提交
        editor.commit();

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
            if (lastDownloadId != -1) {
                downloadManager.remove(lastDownloadId);
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

    public void downFromBrowser(Context context, String downloadUrl) {
        // 从浏览器下载
        try {
            Intent intent = new Intent();
            Uri uri = Uri.parse(downloadUrl);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("", "无法通过浏览器下载！");
        }
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
    public File getDownloadFile() {
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor cursor = downloadManager.query(query.setFilterById(lastDownloadId));
        if (cursor != null && cursor.moveToFirst()) {
            String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            String apkPath = Uri.parse(fileUri).getPath();
            if (!TextUtils.isEmpty(apkPath)) {
                Log.e("-------msg", " apk path = " + apkPath);
                return new File(apkPath);
            }
            cursor.close();
        }
        return null;
    }

    @Nullable
    public static String getDiskCachePath(Context context) {
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

    /**
     * 设置下载的进度
     *
     * @param progress 进度
     */
    public void setProgress(int progress) {

    }
    /**
     * 显示下载失败
     */
    public void showFail() {
        if (mDownloadListener != null) {
            mDownloadListener.downloadErr();
        }
    }


    public void startDownload() {

    }

    public void pauseDownload(){

    }

    public void successDownload() {
        if (mDownloadListener != null) {
            mDownloadListener.downloadSuccess(getDownloadFile().toString());
        }
    }

    private DownloadListener mDownloadListener;

    public void setmDownloadListener(DownloadListener mDownloadListener) {
        this.mDownloadListener = mDownloadListener;
    }

    public interface DownloadListener{
        void downloadSuccess(String path);
        void downloadErr();
    }
}
