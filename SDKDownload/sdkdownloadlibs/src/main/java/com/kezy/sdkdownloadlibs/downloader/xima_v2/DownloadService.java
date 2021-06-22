package com.kezy.sdkdownloadlibs.downloader.xima_v2;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kezy.sdkdownloadlibs.task.DownloadTask;
import com.kezy.sdkdownloadlibs.task.TaskImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadService extends Service {

    private static final String TAG = "-----msg download v2";
    private Binder mBinder;
    private UpdateHandler mHandler;

    public static final String DOWNLOAD_APK_PATH = "download_apk";


    public static final String DOWNLOAD_APK_URL = "download_apk_url";
    public static final String DOWNLOAD_APK_NAME = "download_apk_name";


    // 通知栏点击行动
    private static final String ACTION = "actionName";
    private static final String DOWNLOADURL = "downloadUrl";
    private static final String PAUSE_ACTION = "pauseAction";
    private static final String CANCLE_ACTION = "cancleAction";
    private static final String RESUME_ACTION = "resumeAction";

    /**
     * @Fields mDownloadTaskList : 正在下载的任务
     */
    private List<DownloadTask> mDownloadTaskList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class Binder extends android.os.Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new Binder();
        mHandler = new UpdateHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(ACTION)) {
            String action = intent.getStringExtra(ACTION);
            String downloadUrl = intent.getStringExtra(DOWNLOADURL);

            Log.d(TAG, "onStartCommand " + action);

            if (PAUSE_ACTION.equals(action)) {
                pauseDownload(downloadUrl);
            } else if (CANCLE_ACTION.equals(action)) {

                removeDownload(downloadUrl);
            } else if (RESUME_ACTION.equals(action)) {
                startDownload(downloadUrl);
            }
            return super.onStartCommand(intent, flags, startId);
        }

        DownloadTask task = getDownloadTask(intent);
        if (task != null) {
            for (DownloadTask dt : mDownloadTaskList) {
                if (task.equals(dt)) {
                    if (isDowning(dt.url)) {
                        // 如果是重新下载，触发一下 下载开始回调
                        handleStart(dt.url, true);
                    }
                    if (dt.status == TaskImpl.Status.ERROR) {
                        DownloadThread thread = new DownloadThread(getApplicationContext(), dt, mHandler);
                        dt.retryCount = 0;
                        thread.start();
                    }
                    if (dt.status == TaskImpl.Status.STOPPED) {
                        startDownload(dt.url);
                    }
                    return super.onStartCommand(intent, flags, startId);
                }
            }
            mDownloadTaskList.add(task);
            DownloadThread thread = new DownloadThread(getApplicationContext(), task, mHandler);
            task.retryCount = 0;
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public void startDownload(String url) {
        DownloadTask task = getDownloadTrackByUrl(url);
        if (task == null) {
            return;
        }
        task.isRunning = true;
        Log.e("-------msg" , "startDownload  --- task.isRunning = " + task.isRunning);
        if (task.status != TaskImpl.Status.DOWNLOADING) {
            task.status = TaskImpl.Status.DOWNLOADING;
            DownloadThread thread = new DownloadThread(getApplicationContext(), task, mHandler);
            task.retryCount = 0;
            task.isRunning = true;
            thread.start();
        }
    }

    public void pauseDownload(String url) {
        DownloadTask task = getDownloadTrackByUrl(url);
        if (task != null && task.isRunning) {
            task.isRunning = false;
            task.status = TaskImpl.Status.STOPPED;
        }
    }

    public void removeDownload(String url) {
        DownloadTask task = getDownloadTrackByUrl(url);
        if (task != null) {
            task.status = TaskImpl.Status.DELETE;
            task.isRunning = false;
            String filePath = task.getFilePath();
            if (filePath != null && new File(filePath).exists()) {
                new File(filePath).delete();
            }
            File tempDownloadPath = getTempDownloadPath(task);
            if (tempDownloadPath != null && tempDownloadPath.exists()) {
                tempDownloadPath.delete();
            }
        }
    }

    private DownloadTask getDownloadTask(Intent intent) {
        DownloadTask task = null;
        if (null != intent) {
            String mFileName = intent.getStringExtra(DOWNLOAD_APK_NAME);
            String mDownloadUrl = intent.getStringExtra(DOWNLOAD_APK_URL);
            long spfileSize = 0;
            long spTemp = 0;
            if (!TextUtils.isEmpty(mDownloadUrl)) {
                task = new DownloadTask();
                task.timeId = System.currentTimeMillis();
                task.url = mDownloadUrl;
                task.name = mFileName;
                if (spfileSize > 0 && spTemp > 0) {
                    task.totalSize = spfileSize;
                    task.tempSize = spTemp;
                    task.progress = (int) ((spTemp * 100) / spfileSize);
                }
            }
        }
        return task;
    }

    public boolean isDowning(String url) {
        DownloadTask downloadTask = getDownloadTrackByUrl(url);
        if (downloadTask != null) {
            if (downloadTask.status == TaskImpl.Status.DOWNLOADING) {
                return true;
            }
        }

        return false;
    }

    public DownloadTask getDownloadTrackByUrl(String url) {
        if (mDownloadTaskList == null || TextUtils.isEmpty(url)) {
            return null;
        }
        for (int i = 0; i < mDownloadTaskList.size(); i++) {
            if (url.equals(mDownloadTaskList.get(i).url)) {
                return mDownloadTaskList.get(i);
            }
        }
        return null;
    }

    private void handleStart(String url, boolean isRestart) {
//        for (IDownloadServiceStatueListener l : mDownloadServiceStatueListeners) {
//            l.onStartCallBack(url, isRestart);
//        }

        Log.d(TAG, "handleStart   " + url);
    }

    private File getTempDownloadPath(DownloadTask task) {
        if (task != null) {
            return new File(task.path, task.name + ".temp");
        }
        return null;
    }

    public int getStatueByUrl(String url) {
       DownloadTask downloadTask = getDownloadTrackByUrl(url);
        if (downloadTask != null) {
            return downloadTask.status;
        }
        return TaskImpl.Status.WAITING;
    }

    @Nullable
    public String getDownloadSavePath(String url) {
        DownloadTask downloadTask = getDownloadTrackByUrl(url);
        if (downloadTask != null) {
            return downloadTask.getFilePath() + ".apk";
        }
        return null;
    }


    public static final int DOWN_OK = 1001;
    public static final int DOWN_ERROR = 1002;
    public static final int DOWNLOAD_ING = 1003;
    public static final int REQUEST_TIME_OUT = 1004;
    public static final int REQUEST_TIME_OUT_RETRY = 1005;
    public static final int HANDLER_PAUSE = 1006;
    public static final int HANDLER_REMOVE = 1007;
    public static final int HANDLER_SHOW_RETRY_NOTIF = 1008;

    public class UpdateHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.e("----------msg", " ------- 下载过程  msg msg msg ---- msg   " + msg);
            DownloadTask task = (DownloadTask) msg.obj;
            if (task == null) {
                return;
            }

            switch (msg.what) {
                case DOWN_OK:
                    // 下载完成，点击安装
                    File file = new File(task.getFilePath() + ".apk");
                    String fileName = file.getName().toUpperCase();
                    Log.e("----------msg", " ------- 下载完成 ----fileName   " + fileName);
                    if (TextUtils.isEmpty(fileName)
                            || !fileName.endsWith(".APK")) {
                        return;
                    }
                    break;

                case DOWN_ERROR:
                    Log.e("----------msg", " ------- err ----   ");
                    break;
                case DOWNLOAD_ING:
                    Log.e("----------msg", " ------- ing ----   " + task.progress);
                    break;
                case REQUEST_TIME_OUT:
                    Log.e("----------msg", " ------- REQUEST_TIME_OUT ----   ");
                    break;
                case REQUEST_TIME_OUT_RETRY:
                    Log.e("----------msg", " ------- REQUEST_TIME_OUT_RETRY ----   ");
                    break;
                case HANDLER_PAUSE:
                    Log.e("----------msg", " ------- HANDLER_PAUSE ----   ");
                    break;
                case HANDLER_REMOVE:
                    Log.e("----------msg", " ------- HANDLER_REMOVE ----   ");
                    break;
                case HANDLER_SHOW_RETRY_NOTIF:
                    Log.e("----------msg", " ------- HANDLER_SHOW_RETRY_NOTIF ----   ");
                    break;

                default:
                    // stopService(updateIntent);
                    break;
            }
        }
    }
}
