package com.kezy.sdkdownloadlibs.task;

import android.content.Context;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.downloader.api.ApiDownloadManager;
import com.kezy.sdkdownloadlibs.listener.DownloadStatusChangeListener;
import com.kezy.sdkdownloadlibs.manager.EngineImpl;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadTask<T extends EngineImpl> implements TaskImpl {
    public T mTaskManager;
    public DownloadInfo mDownloadInfo;

    public void setDownloadStatusListener(DownloadStatusChangeListener statusListener) {
        mTaskManager.bindStatusChangeListener(statusListener);
    }

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
        if (mTaskManager!= null) {
            mTaskManager.startDownload(context);
        }
    }

    public void pause(Context context) {
        if (mTaskManager!= null) {
            mTaskManager.pauseDownload(context);
        }
    }

    public void reStart(Context context){
        if (mTaskManager!= null) {
            mTaskManager.continueDownload(context);
        }
    }

    public void remove(Context context) {
        if (mTaskManager!= null) {
            mTaskManager.deleteDownload(context);
        }
    }

    public void install(Context context) {
        if (mTaskManager != null) {
            mTaskManager.installApk(context);
        }
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
