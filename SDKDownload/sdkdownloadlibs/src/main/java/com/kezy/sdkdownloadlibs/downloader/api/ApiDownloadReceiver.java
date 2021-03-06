package com.kezy.sdkdownloadlibs.downloader.api;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;

import java.util.Objects;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description DownloadManager下载广播接收器，需要在xml中注册,
 *  * 主要实现通知栏显示下载完成自动安装，或者通知栏点击跳转到系统的下载管理器界面
 */
public class ApiDownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (Objects.requireNonNull(intent.getAction()).equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                // 下载完成
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                // 自动安装app
                installApp(context, downloadId);
            } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                // 未下载完成，点击跳转系统的下载管理界面
//                Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
//                viewDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(viewDownloadIntent);
            }
        }
    }

    /**
     * 安装app
     *
     * @param context    上下文
     * @param downloadId 下载任务id
     */
    private void installApp(Context context, long downloadId) {
        try {
            if (downloadId == -1) {
                return;
            }
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            Cursor cursor = downloadManager.query(query.setFilterById(downloadId));
            if (cursor != null && cursor.moveToFirst()) {
                String fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                String path = Uri.parse(fileUri).getPath();
                cursor.close();
                DownloadUtils.installApk(context, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
