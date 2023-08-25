package com.kezy.a23test;

/**
 * @Author Kezy
 * @Time 2023/6/12
 * @Description
 */
public class AppNetworkUsage {
    private String mPackageName;
    private long mRxBytes;
    private long mTxBytes;

    public AppNetworkUsage(String packageName, long rxBytes, long txBytes) {
        mPackageName = packageName;
        mRxBytes = rxBytes;
        mTxBytes = txBytes;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public long getRxBytes() {
        return mRxBytes;
    }

    public long getTxBytes() {
        return mTxBytes;
    }
}

