package com.kezy.sdkdownloadlibs.downloader;

/**
 * @Author Kezy
 * @Time 2021/6/22
 * @Description
 */
public class DownloadUtils {

    public static boolean checkSdcardMounted() {
        // 获取SdCard状态
        String status = android.os.Environment.getExternalStorageState();
        // 判断sdcard是否可用
        return android.os.Environment.MEDIA_MOUNTED.equals(status);
    }
}
