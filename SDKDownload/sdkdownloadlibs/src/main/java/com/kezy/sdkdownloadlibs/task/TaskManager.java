package com.kezy.sdkdownloadlibs.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.downloader.api.ApiDownloadManager;
import com.kezy.sdkdownloadlibs.downloader.xima.DownloadServiceManage;
import com.kezy.sdkdownloadlibs.impls.EngineImpl;
import com.kezy.sdkdownloadlibs.impls.TaskImpl;
import com.kezy.sdkdownloadlibs.listener.IDownloadStatusListener;
import com.kezy.sdkdownloadlibs.listener.IDownloadTaskListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Author Kezy
 * @Time 2021/6/30
 * @Description
 */
public class TaskManager {

    private Context mContext;
    private Map<String, DownloadTask> mKeyTaskMap = new HashMap<>();

    private IDownloadTaskListener mTaskListener;

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
        if (context == null || info == null) {
            return null;
        }
        this.mContext = context.getApplicationContext();
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
                ((DownloadServiceManage)mTaskManager).bindDownloadInfo(info);
            } else {
                mTaskManager = new ApiDownloadManager();
                info.downloadType = EngineImpl.DownloadType.TYPE_API;
            }
            this.mDownloadInfo = info;
        }

        @Override
        public void start(Context context) {
            if (mTaskManager!= null) {
                mTaskManager.startDownload(context, mDownloadInfo.url);
            }
        }

        @Override
        public void pause(Context context) {
            if (mTaskManager!= null) {
                mTaskManager.pauseDownload(context, mDownloadInfo.url);
            }
        }

        @Override
        public void reStart(Context context){
            if (mTaskManager!= null) {
                mTaskManager.continueDownload(context, mDownloadInfo.url);
            }
        }

        @Override
        public void remove(Context context) {
            if (mTaskManager!= null) {
                mTaskManager.deleteDownload(context, mDownloadInfo.url);
            }
        }

        @Override
        public void install(Context context) {
            if (mTaskManager != null) {
                mTaskManager.installApk(context, mDownloadInfo.url);
            }
        }

        @Override
        public int getStatus() {
            if (mTaskManager == null) {
                return 0;
            }
            return mDownloadInfo.status;
        }

        @Override
        public DownloadInfo getInfo() {
                return mDownloadInfo;
        }

        @Override
        public int getDownloadType() {
            return mDownloadInfo.downloadType;
        }

        @Override
        public String createDownloadKey() {
            return mDownloadInfo.onlyKey();
        }

        @Override
        public void addTaskListener(IDownloadTaskListener taskListener) {
            mTaskListener = taskListener;
            if (mTaskManager != null) {
                mTaskManager.bindStatusChangeListener(new IDownloadStatusListener() {
                    @Override
                    public void onStart(String onlyKey, boolean isRestart, long totalSize) {
                        mDownloadInfo.status = DownloadInfo.Status.STARTED;
                        mDownloadInfo.totalSize = totalSize;
                        if (taskListener != null) {
                            taskListener.onStart(mDownloadInfo.onlyKey(), isRestart);
                        }
                    }

                    @Override
                    public void onPause(String onlyKey) {
                        mDownloadInfo.status = DownloadInfo.Status.STOPPED;
                        if (taskListener != null) {
                            taskListener.onPause(mDownloadInfo.onlyKey());
                        }
                    }

                    @Override
                    public void onContinue(String onlyKey) {
                        mDownloadInfo.status = DownloadInfo.Status.DOWNLOADING;
                        if (taskListener != null) {
                            taskListener.onContinue(mDownloadInfo.onlyKey());
                        }
                    }

                    @Override
                    public void onRemove(String onlyKey) {
                        mDownloadInfo.status = DownloadInfo.Status.DELETE;
                        if (taskListener != null) {
                            taskListener.onRemove(mDownloadInfo.onlyKey());
                        }
                    }

                    @Override
                    public void onProgress(String onlyKey, int progress) {
                        mDownloadInfo.status = DownloadInfo.Status.DOWNLOADING;
                        mDownloadInfo.tempSize = (long) (mDownloadInfo.totalSize * progress / 100);
                        mDownloadInfo.progress = progress;
                        if (taskListener != null) {
                            taskListener.onProgress(mDownloadInfo.onlyKey());
                        }
                    }

                    @Override
                    public void onError(String onlyKey) {
                        mDownloadInfo.status = DownloadInfo.Status.ERROR;
                        if (taskListener != null) {
                            taskListener.onError(mDownloadInfo.onlyKey());
                        }
                    }

                    @Override
                    public void onSuccess(String onlyKey, String path) {
                        mDownloadInfo.status = DownloadInfo.Status.FINISHED;
                        mDownloadInfo.path = path;
                        mDownloadInfo.packageName = DownloadUtils.getPackageNameByFilepath(mContext, path);
                        if (taskListener != null) {
                            taskListener.onSuccess(mDownloadInfo.onlyKey());
                        }
                    }

                    @Override
                    public void onInstallBegin(String onlyKey) {
                        mDownloadInfo.status = DownloadInfo.Status.FINISHED;
                        if (taskListener != null) {
                            taskListener.onInstallBegin(mDownloadInfo.onlyKey());
                        }
                    }
                });
            }
        }

        @Override
        public void openApp(Context context) {
            DownloadUtils.startAppByPackageName(context, mDownloadInfo.packageName);
        }
    }

    public void destroy(){
    }
}
