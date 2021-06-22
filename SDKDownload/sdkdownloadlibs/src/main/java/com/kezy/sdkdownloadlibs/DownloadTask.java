package com.kezy.sdkdownloadlibs;

import android.content.Context;

import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.EngineImpl;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadTask<T extends EngineImpl> implements TaskImpl{
    public T taskManager;
    public DownloadInfo info;


   public DownloadTask(T taskManager, DownloadInfo info) {
       this.taskManager = taskManager;
       this.info = info;
       taskManager.bindDownloadInfo(info);
   }

    public void start(Context context) {
        taskManager.startDownload(context, info.url);
    }

    public int getStatus(String url) {
       return taskManager.getInfo(url).status;
    }

    @Override
    public int getDownloadType(String url) {
        return taskManager.getInfo(url).downloadType;
    }

    @Override
    public String createDownloadKey(Context context, String downloadUrl) {
        return taskManager.getInfo(downloadUrl).url + taskManager.getInfo(downloadUrl).adId;
    }

}
