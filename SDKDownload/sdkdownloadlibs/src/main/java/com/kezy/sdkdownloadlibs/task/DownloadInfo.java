package com.kezy.sdkdownloadlibs.task;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadInfo implements Parcelable {

    public interface Status {
        int WAITING = 0;     //等待, 没有下载
        int STARTED = 1;     //开始
        int DOWNLOADING = 2; // 正在下载
        int FINISHED = 3;    //完成
        int STOPPED = 4;     //暂停
        int ERROR = 5;       //错误
        int DELETE = 6;      // 删除
    }

    // 自动生成的信息
    public long timeId; // time戳
    public int status;

    // 创建info时需要传递的信息
    public long adId; // 对应广告ID
    public String url; // 下载url
    public String name; // apk name
    public String desc; // apk desc
    public String icon; // apk icon url
    public int downloadType; // 下载器类型


    // 下载过程中，自动生成的信息
    public String path; // 下载path
    public int progress; // 进度
    public long taskId; // 任务id
    public double speed; // 速度
    public int retryCount = 0;
    public boolean isRunning = true;
    public long totalSize; // apk总大小
    public long tempSize; // apk 已下载大小


    private DownloadInfo() {
        this.timeId = System.currentTimeMillis();
        this.status = Status.WAITING;
    }

    private DownloadInfo(Builder builder) {
        this();
        this.url = builder.url;
        this.adId = builder.adId;
        this.name = builder.name;
        this.desc = builder.desc;
        this.icon = builder.icon;
        this.downloadType = builder.downloadType;
    }

    // apk 下载的关键信息
    public static class Builder {
        public String url; // 下载url
        public long adId; // 对应广告ID
        public String name; // apk name
        public String desc; // apk desc
        public String icon; // apk icon url
        public int downloadType; // 下载器类型


        public Builder(String url) {
            this.url = url;
        }

        public Builder setAdId(long adId) {
            this.adId = adId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder setIcon(String icon) {
            this.icon = icon;
            return this;
        }
        public Builder setDownloaderType(int downloadType) {
            this.downloadType = downloadType;
            return this;
        }

        public DownloadInfo build(){
            return new DownloadInfo(this);
        }
    }



    public String getFilePath() {
        return new StringBuilder()
                /*
                 * .append(Environment.getExternalStorageDirectory())
                 * .append(File.separator)
                 */
                .append(path).append(File.separator).append(name).toString();
    }

    public String onlyKey() {
        Log.e("--------msg", " -----get only key =  " + url + "_" + adId + "_" + downloadType);
        return url + "_" + adId + "_" + downloadType;
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
            return TextUtils.equals(this.onlyKey(), other.onlyKey());
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timeId);
        dest.writeInt(this.status);
        dest.writeLong(this.adId);
        dest.writeString(this.url);
        dest.writeString(this.name);
        dest.writeString(this.desc);
        dest.writeString(this.icon);
        dest.writeInt(this.downloadType);
        dest.writeString(this.path);
        dest.writeInt(this.progress);
        dest.writeLong(this.taskId);
        dest.writeDouble(this.speed);
        dest.writeInt(this.retryCount);
        dest.writeByte(this.isRunning ? (byte) 1 : (byte) 0);
        dest.writeLong(this.totalSize);
        dest.writeLong(this.tempSize);
    }

    public void readFromParcel(Parcel source) {
        this.timeId = source.readLong();
        this.status = source.readInt();
        this.adId = source.readLong();
        this.url = source.readString();
        this.name = source.readString();
        this.desc = source.readString();
        this.icon = source.readString();
        this.downloadType = source.readInt();
        this.path = source.readString();
        this.progress = source.readInt();
        this.taskId = source.readLong();
        this.speed = source.readDouble();
        this.retryCount = source.readInt();
        this.isRunning = source.readByte() != 0;
        this.totalSize = source.readLong();
        this.tempSize = source.readLong();
    }

    protected DownloadInfo(Parcel in) {
        this.timeId = in.readLong();
        this.status = in.readInt();
        this.adId = in.readLong();
        this.url = in.readString();
        this.name = in.readString();
        this.desc = in.readString();
        this.icon = in.readString();
        this.downloadType = in.readInt();
        this.path = in.readString();
        this.progress = in.readInt();
        this.taskId = in.readLong();
        this.speed = in.readDouble();
        this.retryCount = in.readInt();
        this.isRunning = in.readByte() != 0;
        this.totalSize = in.readLong();
        this.tempSize = in.readLong();
    }

    public static final Parcelable.Creator<DownloadInfo> CREATOR = new Parcelable.Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel source) {
            return new DownloadInfo(source);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
}
