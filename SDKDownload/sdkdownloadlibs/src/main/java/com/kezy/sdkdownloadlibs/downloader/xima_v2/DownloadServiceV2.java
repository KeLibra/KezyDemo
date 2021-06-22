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

import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.EngineImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadServiceV2 extends Service {

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

    public DownloadInfo getDownloadInfo(String url) {

       return getInfoByUrl(url);
    }

    public void setDownloadInfo(DownloadInfo info) {
       if (mDownloadTaskList.contains(info)) {
           mDownloadTaskList.add(info);
       }
    }

    private DownloadInfo getInfoByUrl(String url) {
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
        public DownloadServiceV2 getService() {
            return DownloadServiceV2.this;
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




        DownloadInfo info =  initDownloadTask(intent);
        if (info != null) {
            for (DownloadInfo dt : mDownloadTaskList) {
                if (info.equals(dt)) {
                    if (isDowning(dt.url)) {
                        // 如果是重新下载，触发一下 下载开始回调
                        handleStart(dt.url, true);
                    }
                    if (dt.status == EngineImpl.Status.ERROR) {
                        DownloadThreadV2 thread = new DownloadThreadV2(getApplicationContext(), dt, mHandler);
                        dt.retryCount = 0;
                        thread.start();
                    }
                    if (dt.status == EngineImpl.Status.STOPPED) {
                        startDownload(dt.url);
                    }
                    return super.onStartCommand(intent, flags, startId);
                }
            }
            if (!mDownloadTaskList.contains(info)) {
                mDownloadTaskList.add(info);
            }
            DownloadThreadV2 thread = new DownloadThreadV2(getApplicationContext(), info, mHandler);
            info.retryCount = 0;
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public void startDownload(String url) {
        if (getInfoByUrl(url) == null) {
            return;
        }
        getInfoByUrl(url).isRunning = true;
        Log.e("-------msg", "startDownload  --- task.isRunning = " + getInfoByUrl(url).isRunning);
        if (getInfoByUrl(url).status != EngineImpl.Status.DOWNLOADING) {
            getInfoByUrl(url).status = EngineImpl.Status.DOWNLOADING;
            DownloadThreadV2 thread = new DownloadThreadV2(getApplicationContext(), getInfoByUrl(url), mHandler);
            getInfoByUrl(url).retryCount = 0;
            getInfoByUrl(url).isRunning = true;
            thread.start();
        }
    }

    public void pauseDownload(String url) {
        if (getInfoByUrl(url) != null && getInfoByUrl(url).isRunning) {
            getInfoByUrl(url).isRunning = false;
            getInfoByUrl(url).status = EngineImpl.Status.STOPPED;
        }
    }

    public void removeDownload(String url) {
        if (getInfoByUrl(url) != null) {
            getInfoByUrl(url).status = EngineImpl.Status.DELETE;
            getInfoByUrl(url).isRunning = false;
            String filePath = getInfoByUrl(url).getFilePath();
            if (filePath != null && new File(filePath).exists()) {
                new File(filePath).delete();
            }
            File tempDownloadPath = getTempDownloadPath(getInfoByUrl(url));
            if (tempDownloadPath != null && tempDownloadPath.exists()) {
                tempDownloadPath.delete();
            }
        }
    }

    private DownloadInfo initDownloadTask(Intent intent) {


        if (null != intent) {
            String mFileName = intent.getStringExtra(DOWNLOAD_APK_NAME);
            String mDownloadUrl = intent.getStringExtra(DOWNLOAD_APK_URL);
            long spfileSize = 0;
            long spTemp = 0;
            if (!TextUtils.isEmpty(mDownloadUrl)) {
                DownloadInfo info = new DownloadInfo(mDownloadUrl);
                info.timeId = System.currentTimeMillis();
                info.url = mDownloadUrl;
                info.name = mFileName;
                if (spfileSize > 0 && spTemp > 0) {
                    info.totalSize = spfileSize;
                    info.tempSize = spTemp;
                    info.progress = (int) ((spTemp * 100) / spfileSize);
                }
                return info;
            }
        }
        return null;
    }

    public boolean isDowning(String url) {
        if (getInfoByUrl(url) != null) {
            if (getInfoByUrl(url).status == EngineImpl.Status.DOWNLOADING) {
                return true;
            }
        }

        return false;
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
        if (getInfoByUrl(url) != null) {
            return getInfoByUrl(url).status;
        }
        return EngineImpl.Status.WAITING;
    }

    @Nullable
    public String getDownloadSavePath(String url) {
        if (getInfoByUrl(url) != null) {
            return getInfoByUrl(url).getFilePath() + ".apk";
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

            DownloadInfo task = (DownloadInfo) msg.obj;
            if (task == null) {
                return;
            }
            switch (msg.what) {
                case DOWN_OK:
                    Log.i("-------------msg", " ------- 2222 下载完成 task URL : " + task.url);
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
//                    Log.e("----------msg", " ------- ing ----   " + task.progress);
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
                    break;
            }
        }
    }
}
