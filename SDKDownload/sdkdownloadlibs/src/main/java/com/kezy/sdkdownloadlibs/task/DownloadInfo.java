package com.kezy.sdkdownloadlibs.task;

import android.text.TextUtils;

import com.kezy.sdkdownloadlibs.manager.EngineImpl;

import java.io.File;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadInfo {

    public long timeId; // time戳
    public long taskId; // 任务id
    public long adId; // 对应广告ID
    public String url; // 下载url
    public String name; // apk name
    public String desc; // apk desc
    public String icon; // apk icon url

    public String path; // 下载path

    public int progress; // 进度

    public int status = EngineImpl.Status.WAITING;

    public double speed; // 速度

    public int retryCount = 0;

    public boolean isRunning = true;

    public long totalSize; // apk总大小
    public long tempSize; // apk 已下载大小

    public int downloadType; // 下载器类型

    private DownloadInfo() {
        this.timeId = System.currentTimeMillis();
    }

    public DownloadInfo(String url) {
        this();
        this.url = url;
    }

    public String getFilePath() {
        return new StringBuilder()
                /*
                 * .append(Environment.getExternalStorageDirectory())
                 * .append(File.separator)
                 */
                .append(path).append(File.separator).append(name).append(".apk").toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof DownloadInfo) {
            DownloadInfo other = (DownloadInfo) obj;
            // adid 相同 并且 下载url 相同，则认为是同一个task
                return TextUtils.equals(this.url, other.url) && this.adId == other.adId;
        }
        return false;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "taskId=" + taskId +
                ", adId=" + adId +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", icon='" + icon + '\'' +
                ", path='" + path + '\'' +
                ", progress=" + progress +
                ", status=" + status +
                ", isRunning=" + isRunning +
                ", totalSize=" + totalSize +
                ", tempSize=" + tempSize +
                ", downloadType=" + downloadType +
                '}';
    }
}
