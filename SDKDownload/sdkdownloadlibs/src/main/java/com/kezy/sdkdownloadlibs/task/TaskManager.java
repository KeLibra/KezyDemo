package com.kezy.sdkdownloadlibs.task;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.kezy.sdkdownloadlibs.downloader.api.ApiDownloadManager;
import com.kezy.sdkdownloadlibs.downloader.xima.DownloadServiceManage;
import com.kezy.sdkdownloadlibs.impls.EngineImpl;
import com.kezy.sdkdownloadlibs.impls.TaskImpl;
import com.kezy.sdkdownloadlibs.listener.DownloadStatusChangeListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Kezy
 * @Time 2021/6/30
 * @Description
 */
public class TaskManager {

    private Map<String, DownloadTask> mKeyTaskMap = new HashMap<>();

    private TaskManager() {
        loadLocalData();
    }

    private static class INSTANCE {
        private static TaskManager instance = new TaskManager();
    }

    public static TaskManager getInstance() {
        return INSTANCE.instance;
    }

    public TaskImpl createDownloadTask(Context context, DownloadInfo info) {
        DownloadTask task = getTaskByOnlyKey(info.onlyKey());
        if (task == null) {
            task = new DownloadTask(context, info);
            updateTask(task);
        }
        return task;
    }

    public void updateTask(DownloadTask task) {
        if (task == null || task.mDownloadInfo == null) {
            return;
        }
        mKeyTaskMap.put(task.createDownloadKey(), task);
        saveLocalData();
    }

    public void removeTask(DownloadTask task) {
        if (task == null || task.mDownloadInfo == null) {
            return;
        }
        mKeyTaskMap.remove(task.createDownloadKey());
    }

    public DownloadTask getTaskByOnlyKey(String onlyKey) {
        for (Map.Entry<String, DownloadTask> entry : mKeyTaskMap.entrySet()) {
            if (entry != null && entry.getKey().endsWith(onlyKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void loadLocalData() {
        Log.e("--------msg", "--------1 load local data ");
    }

    private void saveLocalData() {
        Log.e("--------msg", "--------2 save local data ");
    }

    private class DownloadTask implements TaskImpl {
        public EngineImpl mTaskManager;
        public DownloadInfo mDownloadInfo;

        public DownloadTask(Context context,DownloadInfo info) {
            if (info.downloadType == EngineImpl.DownloadType.TYPE_XIMA) {
                mTaskManager = new DownloadServiceManage(context);
            } else {
                mTaskManager = new ApiDownloadManager();
                info.downloadType = EngineImpl.DownloadType.TYPE_API;
            }
            this.mDownloadInfo = info;
            mTaskManager.bindDownloadInfo(info);
        }

        @Override
        public void start(Context context) {
            if (mTaskManager!= null) {
                mTaskManager.startDownload(context);
            }
        }

        @Override
        public void pause(Context context) {
            if (mTaskManager!= null) {
                mTaskManager.pauseDownload(context);
            }
        }

        @Override
        public void reStart(Context context){
            if (mTaskManager!= null) {
                mTaskManager.continueDownload(context);
            }
        }

        @Override
        public void remove(Context context) {
            if (mTaskManager!= null) {
                mTaskManager.deleteDownload(context);
            }
        }

        @Override
        public void install(Context context) {
            if (mTaskManager != null) {
                mTaskManager.installApk(context);
            }
        }

        @Override
        public int getStatus() {
            if (mTaskManager == null) {
                return 0;
            }
            return mTaskManager.getInfo().status;
        }

        @Override
        public DownloadInfo getInfo() {
            if (mTaskManager == null) {
                return mDownloadInfo;
            }
            return mTaskManager.getInfo();
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
                return mDownloadInfo.onlyKey();
            }
            return mTaskManager.getInfo().onlyKey();
        }

        @Override
        public void setDownloadStatusListener(DownloadStatusChangeListener statusListener) {
            if (mTaskManager != null) {
                mTaskManager.bindStatusChangeListener(statusListener);
            }
        }
    }
}
