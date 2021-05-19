package com.kezy.downloadaa.apidownload;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;

/**
 * @Author Kezy
 * @Time 2021/5/18
 * @Description
 */
public class AdApiDownloadHandler extends Handler {

    private final WeakReference<AdApiDownloadManager> wrfUpdateManager;
    private Context context;

    public AdApiDownloadHandler(Context context, AdApiDownloadManager updateManager) {
        this.context = context;
        wrfUpdateManager = new WeakReference<>(updateManager);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case DownloadManager.STATUS_PAUSED:
                // 暂停
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().pauseDownload();
                }
                break;
            case DownloadManager.STATUS_PENDING:
                // 开始
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().startDownload();
                }
                break;
            case DownloadManager.STATUS_RUNNING:
                // 下载中
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().setProgress(msg.arg1);
                }
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().setProgress(100);
                    wrfUpdateManager.get().unregisterContentObserver(context);
                }
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().installApp(context, wrfUpdateManager.get().getDownloadFile());
                }
                break;
            case DownloadManager.STATUS_FAILED:
                // 下载失败，清除本次的下载任务
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().clearCurrentTask();
                    wrfUpdateManager.get().unregisterContentObserver(context);
                    wrfUpdateManager.get().showFail();
                }
                Log.e("------msg", " ------- 下载失败");
                break;
            default:
                break;
        }
    }
}
