package com.kezy.sdkdownloadlibs.manager;

import android.content.Context;

import com.kezy.sdkdownloadlibs.listener.DownloadStatusChangeListener;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;

/**
 * @Author Kezy
 * @Time 2021/6/21
 * @Description task 接口
 */
public interface EngineImpl<T> {

    class DownloadType {
        public static int TYPE_API = 100; // api下载
        public static int TYPE_XIMA = 101; // 喜马下载

    }

    void bindDownloadInfo(DownloadInfo info);

    void bindStatusChangeListener(DownloadStatusChangeListener listener);

    DownloadInfo getInfo();

    // 下载任务id
    long getTaskId();

    // 开始下载
    void startDownload(Context context);

    //暂停下载
    void pauseDownload(Context context);

    // 继续下载
    void continueDownload(Context context);

    // 删除下载
    void deleteDownload(Context context);

    // 获取下载状态
    int getStatus(Context context);

    void installApk(Context context);

    // 获取下载文件路径
    String getDownloadFile(Context context);

    int getDownloaderType();

    void destroy();
}
