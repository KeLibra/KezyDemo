package com.kezy.sdkdownloadlibs;

import android.content.Context;

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
        mTaskManager.startDownload(context, mDownloadInfo.url);
    }

    public int getStatus(String url) {
       return mTaskManager.getInfo(url).status;
    }

    @Override
    public int getDownloadType(String url) {
        return mTaskManager.getInfo(url).downloadType;
    }

    @Override
    public String createDownloadKey(Context context, String downloadUrl) {
        return mTaskManager.getInfo(downloadUrl).url + mTaskManager.getInfo(downloadUrl).adId;
    }

}
