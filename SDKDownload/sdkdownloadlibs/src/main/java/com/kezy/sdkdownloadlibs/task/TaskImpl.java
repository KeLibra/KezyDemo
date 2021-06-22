package com.kezy.sdkdownloadlibs.task;

import android.content.Context;

/**
 * @Author Kezy
 * @Time 2021/6/21
 * @Description task 接口
 */
public interface TaskImpl<T> {

    class Status {
        public static int WAITING = 0;     //等待
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
    int getDownloadType();

    T createDownloadKey(Context context, String downloadUrl);

    long getTaskId(String downloadUrl);

    void startDownload(Context context, String downloadUrl);

    void pauseDownload(Context context, String downloadUrl);

    void continueDownload(Context context, String downloadUrl);

    void deleteDownload(Context context, String downloadUrl);

    int getStatus(Context context, String downloadUrl);

    String getDownloadFile(Context context, String downloadUrl);
}
