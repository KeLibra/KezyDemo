package com.kezy.downloadaa;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kezy.downloadaa.apidownload.AdApiDownloadManager;

public class MainActivity extends AppCompatActivity {

    private Button btnDownload, btnCopy;
    private TextView tvAppName;
    private EditText etAppUrl;

    String apkUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDownload = findViewById(R.id.btn_download);
        btnCopy = findViewById(R.id.btn_copy);
        tvAppName = findViewById(R.id.tv_app_name);
        etAppUrl = findViewById(R.id.et_apk_url);


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apkUrl = etAppUrl.getText().toString().trim();
                if (TextUtils.isEmpty(apkUrl)) {
                    showErrorMsg("请输入apk下载链接");
                    return;
                }
                if (!isDownloadApkUrl(apkUrl)) {
                    showErrorMsg("apk下载链格式接不正确");
                    return;
                }

                AdApiDownloadManager.getInstance().downLoadApk(MainActivity.this,
                        apkUrl, "cache", "测试apk");

            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apkName = tvAppName.getText().toString().trim();
                if (TextUtils.isEmpty(apkName)) {
                    showErrorMsg("apk 包名解析异常");
                    return;
                }
                // 获取系统剪贴板
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, apkName);
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(MainActivity.this,"包名： " + apkName + " 已复制到剪切板", Toast.LENGTH_LONG).show();
            }
        });

        AdApiDownloadManager.getInstance().setmDownloadListener(new AdApiDownloadManager.DownloadListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void downloadSuccess(String path) {
                PackageInfo packageInfo = getPackageInfoFromApkPath(MainActivity.this, path);
                if (packageInfo != null && packageInfo.applicationInfo != null) {
                    tvAppName.setTextColor(R.color.black);
                    tvAppName.setText(packageInfo.applicationInfo.packageName);
                } else {
                    tvAppName.setTextColor(Color.parseColor("#fd5353"));
                    tvAppName.setText("包名解析异常");
                }
            }

            @Override
            public void downloadErr() {
                tvAppName.setTextColor(Color.parseColor("#fd5353"));
                tvAppName.setText("下载异常");
            }
        });
    }


    /**
     * 通过apk 文件路径 获取packageinfo
     */
    public PackageInfo getPackageInfoFromApkPath(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            return info;
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("--------msg", " ------- e : " + e.toString());
        }
        return null;
    }

    private boolean isDownloadApkUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path) && path.contains(".apk")) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void showErrorMsg(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage(msg)
                .create();
        dialog.show();
    }
}