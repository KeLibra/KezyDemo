package com.kezy.sdkdownloadlibs;

import android.content.Context;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public interface TaskImpl {

    // 下载器类型
    int getDownloadType(String url);

    // 下载的key
    String createDownloadKey(Context context, String downloadUrl);
}
