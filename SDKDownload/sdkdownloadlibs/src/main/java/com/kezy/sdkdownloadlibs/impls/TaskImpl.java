package com.kezy.sdkdownloadlibs.impls;

import android.content.Context;

import com.kezy.sdkdownloadlibs.listener.IDownloadStatusListener;
import com.kezy.sdkdownloadlibs.listener.IDownloadTaskListener;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public interface TaskImpl {

    DownloadInfo getInfo();

    // 下载器类型
    int getDownloadType();

    // 下载的key
    String createDownloadKey();

    void start(Context context);

    void pause(Context context);

    void reStart(Context context);

    void remove(Context context);

    void install(Context context);

    int getStatus();

    void addTaskListener(IDownloadTaskListener listener);
}
