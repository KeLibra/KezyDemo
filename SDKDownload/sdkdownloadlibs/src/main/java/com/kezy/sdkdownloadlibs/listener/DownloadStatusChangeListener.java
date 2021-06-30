package com.kezy.sdkdownloadlibs.listener;

/**
 * @Author Kezy
 * @Time 2021/6/29
 * @Description
 */
public interface DownloadStatusChangeListener {

    void onStart(String onlyKey, boolean isRestart);
    void onPause(String onlyKey);
    void onContinue(String onlyKey);
    void onRemove(String onlyKey);
    void onProgress(String onlyKey, int progress);
    void onError(String onlyKey);
    void onSuccess(String onlyKey);
}
