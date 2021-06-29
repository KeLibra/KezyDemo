package com.kezy.sdkdownloadlibs.task;

import android.content.Context;

import com.kezy.sdkdownloadlibs.downloader.api.ApiDownloadManager;
import com.kezy.sdkdownloadlibs.manager.EngineImpl;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadTask<T extends EngineImpl> implements TaskImpl {
    public T mTaskManager;
    public DownloadInfo mDownloadInfo;

   public DownloadTask(T taskManager, DownloadInfo info) {
       if (taskManager == null) {
           this.mTaskManager = (T) new ApiDownloadManager();
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
    public int getDownloadType() {
        if (mTaskManager == null || mTaskManager.getInfo() == null) {
            return -1;
        }
        return mTaskManager.getInfo().downloadType;
    }

    @Override
    public String createDownloadKey() {
        if (mTaskManager == null || mTaskManager.getInfo() == null) {
            return "null";
        }
        return mTaskManager.getInfo().url + mTaskManager.getInfo().adId;
    }

}
