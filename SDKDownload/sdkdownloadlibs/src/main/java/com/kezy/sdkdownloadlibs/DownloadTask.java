package com.kezy.sdkdownloadlibs;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.kezy.sdkdownloadlibs.downloader.api.AdApiDownloadManager;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.EngineImpl;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadTask<T extends EngineImpl> implements TaskImpl{
    public T mTaskManager;
    public DownloadInfo mDownloadInfo;

   public DownloadTask(T taskManager, DownloadInfo info) {
       if (taskManager == null) {
           this.mTaskManager = (T) new AdApiDownloadManager();
       } else {
           this.mTaskManager = taskManager;
       }
       this.mDownloadInfo = info;
       mTaskManager.bindDownloadInfo(info);
   }

    public void start(Context context) {
        mTaskManager.startDownload(context);
    }


    public int getStatus() {
       if (mTaskManager == null) {
           return 0;
       }
       return mTaskManager.getInfo().status;
    }

    @Override
    public int getDownloadType(String url) {
        if (mTaskManager == null || mTaskManager.getInfo() == null) {
            return -1;
        }
        return mTaskManager.getInfo().downloadType;
    }

    @Override
    public String createDownloadKey(Context context, String downloadUrl) {
        if (mTaskManager == null || mTaskManager.getInfo() == null) {
            return "null";
        }
        return mTaskManager.getInfo().url + mTaskManager.getInfo().adId;
    }

}
