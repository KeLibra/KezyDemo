package com.kezy.sdkdownloadlibs.downloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;


import java.io.File;

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

    /**
     * 安装apk
     *
     * @param context
     * @param path    下载apk路径
     */
    public static void installApk2(Context context, String path) {
        if (!TextUtils.isEmpty(path)) {
            File apkFile = new File(path);
            try {
                // 安装
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        boolean allowInstall = context.getPackageManager().canRequestPackageInstalls();
                        if (!allowInstall) {
                            //不允许安装未知来源应用，请求安装未知应用来源的权限
                            return;
                        }
                    }
                    //Android7.0之后获取uri要用contentProvider
                    Uri apkUri = FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileProvider", apkFile);
                    //Granting Temporary Permissions to a URI
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void installApk(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        String fileName = file.getName().toUpperCase();
        if (TextUtils.isEmpty(fileName) || !fileName.endsWith(".APK")) {
            return;
        }

        try {
            Uri fileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
            } else {
                fileUri = Uri.fromFile(file);
            }
            String exName = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(exName);

            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent2.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent2.setDataAndType(fileUri, mimeType);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent2.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent2);
            }
        } catch (Exception e) {
            Log.d("-------msgTAG", "The selected file can't be shared: " + fileName);
        }
    }
}
