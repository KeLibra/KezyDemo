package com.kezy.sdkdownloadlibs.task;

import android.content.Context;

/**
 * @Author Kezy
 * @Time 2021/6/21
 * @Description task 接口
 */
public interface EngineImpl<T> {

    class Status {
        public static int WAITING = 0;     //等待, 没有下载
        public static int STARTED = 1;     //开始
        public static int DOWNLOADING = 2; // 正在下载
        public static int FINISHED = 3;    //完成
        public static int STOPPED = 4;     //暂停
        public static int ERROR = 5;       //错误
        public static int DELETE = 6;      // 删除
    }

    class DownloadType {
        public static int TYPE_API = 100; // api下载
        public static int TYPE_XIMA = 101; // 喜马下载

    }

    void bindDownloadInfo(DownloadInfo info);

    DownloadInfo getInfo(String url);

    // 下载任务id
    long getTaskId(String downloadUrl);

    // 开始下载
    void startDownload(Context context, String downloadUrl);

    //暂停下载
    void pauseDownload(Context context, String downloadUrl);

    // 继续下载
    void continueDownload(Context context, String downloadUrl);

    // 删除下载
    void deleteDownload(Context context, String downloadUrl);

    // 获取下载状态
    int getStatus(Context context, String downloadUrl);

    // 获取下载文件路径
    String getDownloadFile(Context context, String downloadUrl);

    int getDownloaderType();
}
