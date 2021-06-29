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
    private Context mContext;


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

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("---------msg", "------ binder ---- " + service);
        }
    };
    public int getStatus(String url) {
       if (mTaskManager == null || mTaskManager.getInfo(url) == null) {
           return 0;
       }
       return mTaskManager.getInfo(url).status;
    }

    @Override
    public int getDownloadType(String url) {
        if (mTaskManager == null || mTaskManager.getInfo(url) == null) {
            return -1;
        }
        return mTaskManager.getInfo(url).downloadType;
    }

    @Override
    public String createDownloadKey(Context context, String downloadUrl) {
        if (mTaskManager == null || mTaskManager.getInfo(downloadUrl) == null) {
            return "null";
        }
        return mTaskManager.getInfo(downloadUrl).url + mTaskManager.getInfo(downloadUrl).adId;
    }

}
