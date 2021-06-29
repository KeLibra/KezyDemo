package com.kezy.sdkdownloadlibs.downloader.xima;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kezy.notifylib.NotificationsManager;
import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.manager.EngineImpl;

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
    private List<DownloadInfo> mDownloadTaskList = new ArrayList<>();

    private NotificationManager mNotifyManager;


    public void setDownloadInfo(DownloadInfo info) {
        if (!mDownloadTaskList.contains(info)) {
            mDownloadTaskList.add(info);
        }
    }


    public DownloadInfo getDownloadInfoByUrl(String url) {
        for (DownloadInfo task : mDownloadTaskList) {
            if (TextUtils.equals(url, task.url)) {
                return task;
            }
        }
        return null;
    }

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
        Log.e("---------msg", " --------- onCreate");
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBinder = new Binder();
        mHandler = new UpdateHandler(this);
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

        DownloadInfo info =  initDownloadTask(intent);
        if (info != null) {
            for (DownloadInfo dt : mDownloadTaskList) {
                if (info.equals(dt)) {
                    if (dt.status == EngineImpl.Status.WAITING) {
                        startDownload(dt.url);
                    }
                    if (isDowning(dt.url)) {
                        // 如果是重新下载，触发一下 下载开始回调
                        handleStart(dt.url, true);
                    }
                    if (dt.status == EngineImpl.Status.ERROR) {
                        DownloadThread thread = new DownloadThread(getApplicationContext(), dt, mHandler);
                        dt.retryCount = 0;
                        thread.start();
                    }
                    if (dt.status == EngineImpl.Status.STOPPED) {
                        startDownload(dt.url);
                    }
                    if (dt.status == EngineImpl.Status.FINISHED) {
                        File file = new File(dt.path);
                        if (file == null || !file.exists()) {
                            Log.i("-------msg", "------- 已下载完成过了，但是apk被删除了，需要重新下载 ");
                            startDownload(dt.url);
                        }
                    }
                    return super.onStartCommand(intent, flags, startId);
                }
            }
            if (!mDownloadTaskList.contains(info)) {
                mDownloadTaskList.add(info);
            }
            DownloadThread thread = new DownloadThread(getApplicationContext(), info, mHandler);
            info.retryCount = 0;
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }



    private DownloadInfo initDownloadTask(Intent intent) {
        DownloadInfo info;
        if (null != intent) {

            String mDownloadUrl = intent.getStringExtra(DOWNLOAD_APK_URL);
            if (!TextUtils.isEmpty(mDownloadUrl)) {
                if (getDownloadInfoByUrl(mDownloadUrl) != null) {
                    info = getDownloadInfoByUrl(mDownloadUrl);
                } else {
                    String mFileName = intent.getStringExtra(DOWNLOAD_APK_NAME);
                    info = new DownloadInfo(mDownloadUrl);
                    info.name = mFileName;
                }
                return info;
            }
        }
        return null;
    }

    public boolean isDowning(String url) {
        if (getDownloadInfoByUrl(url) != null) {
            if (getDownloadInfoByUrl(url).status == EngineImpl.Status.DOWNLOADING) {
                return true;
            }
        }

        return false;
    }

    public void startDownload(String url) {
        if (getDownloadInfoByUrl(url) == null) {
            return;
        }
        getDownloadInfoByUrl(url).isRunning = true;
        Log.e("-------msg", "startDownload  --- task.isRunning = " + getDownloadInfoByUrl(url).isRunning);
        if (getDownloadInfoByUrl(url).status != EngineImpl.Status.DOWNLOADING) {
            getDownloadInfoByUrl(url).status = EngineImpl.Status.DOWNLOADING;
            DownloadThread thread = new DownloadThread(getApplicationContext(), getDownloadInfoByUrl(url), mHandler);
            getDownloadInfoByUrl(url).retryCount = 0;
            getDownloadInfoByUrl(url).isRunning = true;
            thread.start();
        }
    }

    public void pauseDownload(String url) {
        if (getDownloadInfoByUrl(url) != null && getDownloadInfoByUrl(url).isRunning) {
            getDownloadInfoByUrl(url).isRunning = false;
            getDownloadInfoByUrl(url).status = EngineImpl.Status.STOPPED;
        }
    }

    public void removeDownload(String url) {
        if (getDownloadInfoByUrl(url) != null) {
            getDownloadInfoByUrl(url).status = EngineImpl.Status.DELETE;
            getDownloadInfoByUrl(url).isRunning = false;
            String filePath = getDownloadInfoByUrl(url).getFilePath();
            if (filePath != null && new File(filePath).exists()) {
                new File(filePath).delete();
            }
            File tempDownloadPath = getTempDownloadPath(getDownloadInfoByUrl(url));
            if (tempDownloadPath != null && tempDownloadPath.exists()) {
                tempDownloadPath.delete();
            }
        }
    }



    private void handleStart(String url, boolean isRestart) {
//        for (IDownloadServiceStatueListener l : mDownloadServiceStatueListeners) {
//            l.onStartCallBack(url, isRestart);
//        }

        Log.d(TAG, "handleStart   " + url);
    }

    private File getTempDownloadPath(DownloadInfo task) {
        if (task != null) {
            return new File(task.path, task.name + ".temp");
        }
        return null;
    }

    public int getStatueByUrl(String url) {
        if (getDownloadInfoByUrl(url) != null) {
            return getDownloadInfoByUrl(url).status;
        }
        return EngineImpl.Status.WAITING;
    }

    @Nullable
    public String getDownloadSavePath(String url) {
        if (getDownloadInfoByUrl(url) != null) {
            return getDownloadInfoByUrl(url).getFilePath();
        }
        return null;
    }

    public static final int DOWN_OK = 1001;
    public static final int DOWN_ERROR = 1002;
    public static final int DOWNLOAD_ING = 1003;
    public static final int REQUEST_TIME_OUT = 1004;
    public static final int HANDLER_PAUSE = 1006;
    public static final int HANDLER_REMOVE = 1007;

    public class UpdateHandler extends Handler {

        private Context mContext;
        public UpdateHandler(Context context) {
            mContext = context;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.i("-------------msg", " ------- handleMessage : " + msg.toString());
            DownloadInfo task = (DownloadInfo) msg.obj;
            if (task == null) {
                return;
            }

            if (getDownloadInfoByUrl(task.url) != null) {
                getDownloadInfoByUrl(task.url).status = task.status;
                getDownloadInfoByUrl(task.url).isRunning = task.isRunning;
                getDownloadInfoByUrl(task.url).progress = task.progress;
                getDownloadInfoByUrl(task.url).totalSize = task.totalSize;
                getDownloadInfoByUrl(task.url).tempSize = task.tempSize;

            }
            switch (msg.what) {
                case DOWN_OK:
                    Log.i("-------------msg", " ------- 2222 下载完成 task URL : " + task.url);
                    // 下载完成，点击安装
                    Log.e("----------msg", " ------- 下载完成22 ----fileName   " + task.path);
                    NotificationsManager.getInstance().clearNotificationById(mNotifyManager, (int) task.timeId);
                    DownloadUtils.installApk(mContext, task.path);
                    break;

                case DOWN_ERROR:
                    Log.e("----------msg", " ------- err ----   ");
                    break;
                case DOWNLOAD_ING:
                    Log.e("----------msg", " ------- ing ----   " + task.progress);
                    NotificationsManager.getInstance().sendProgressViewNotification(mContext, mNotifyManager, task.progress, task.timeId);
                    break;
                case REQUEST_TIME_OUT:
                    Log.e("----------msg", " ------- REQUEST_TIME_OUT ----   ");
                    break;
                case HANDLER_PAUSE:
                    Log.e("----------msg", " ------- HANDLER_PAUSE ----   ");
                    break;
                case HANDLER_REMOVE:
                    Log.e("----------msg", " ------- HANDLER_REMOVE ----   ");
                    break;
                default:
                    break;
            }
        }
    }
}
