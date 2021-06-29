package com.kezy.sdkdownloadlibs.task;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public interface TaskImpl {

    // 下载器类型
    int getDownloadType();

    // 下载的key
    String createDownloadKey();
}
