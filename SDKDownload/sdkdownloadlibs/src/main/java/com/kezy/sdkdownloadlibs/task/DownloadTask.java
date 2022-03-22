package com.kezy.sdkdownloadlibs.task;

import android.content.Context;

import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.downloader.api.ApiDownloadManager;
import com.kezy.sdkdownloadlibs.downloader.xima.DownloadServiceManage;
import com.kezy.sdkdownloadlibs.impls.EngineImpl;
import com.kezy.sdkdownloadlibs.impls.TaskImpl;
import com.kezy.sdkdownloadlibs.listener.IDownloadStatusListener;
import com.kezy.sdkdownloadlibs.listener.IDownloadTaskListener;

import java.io.File;

/**
 * @Author Kezy
 * @Time 2021/7/5
 * @Description
 */
public class DownloadTask implements TaskImpl {
    public EngineImpl mTaskManager;
    public DownloadInfo mDownloadInfo;

    private IDownloadTaskListener mTaskListener;

    private Context mContext;

    public DownloadTask(Context context,DownloadInfo info) {
        if (info.downloadType == EngineImpl.DownloadType.TYPE_XIMA) {
            mTaskManager = new DownloadServiceManage(context);
            ((DownloadServiceManage)mTaskManager).bindDownloadInfo(info);
        } else {
            mTaskManager = new ApiDownloadManager();
            info.downloadType = EngineImpl.DownloadType.TYPE_API;
        }
        this.mContext = context;
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

    @Override
    public void deleteApk(Context context) {
        File file = new File(mDownloadInfo.path);
        if (!file.exists()) {
            return;
        }
        file.delete();
    }
}
