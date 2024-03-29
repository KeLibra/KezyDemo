package com.ycm.unknowapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Button btnInstall;
    private Button btnInstallXMLY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnInstall = findViewById(R.id.btn_install);
        btnInstallXMLY = findViewById(R.id.btn_install_2);

        readAssertResource(this, "apk.apk");
        btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installProcess();
            }
        });

        btnInstallXMLY.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                installApkFromXMLY();

            }
        });

    }

    private void installApkFromXMLY() {
        String apkPath = getExternalCacheDir().getAbsolutePath();
        File file = new File(apkPath, "apk.apk");
        if (!file.exists()) {
            return;
        }
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = getUriFromFile(getBaseContext(), file);
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
        if (intent2.resolveActivity(getPackageManager()) != null) {
            startActivity(intent2);
        }
    }

    //安装应用的流程
    private void installProcess() {
        String apkPath = getExternalCacheDir().getAbsolutePath();
        File apk = new File(apkPath, "apk.apk");
        if (!apk.exists()) {
            return;
        }
        boolean haveInstallPermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先获取是否有安装未知来源应用的权限
            haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {//没有权限
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("安装应用需要打开未知来源权限，请去设置中开启权限");
                builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startInstallPermissionSettingActivity();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
                return;
            }
        }
        //有权限，开始安装应用程序
        installApk(apk);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, 10086);
    }

    //安装应用
    private void installApk(File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        } else {//Android7.0之后获取uri要用contentProvider
            Uri uri = getUriFromFile(getBaseContext(), apk);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(intent);
    }

    public static Uri getUriFromFile(Context context, File file) {
        Uri imageUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageUri = FileProvider.getUriForFile(context,
                    "com.ycm.unknowapp.fileprovider", file);//通过FileProvider创建一个content类型的Uri
        } else {
            imageUri = Uri.fromFile(file);
        }
        return imageUri;
    }

    public void readAssertResource(Context context, String strAssertFileName) {
        String apkPath = getExternalCacheDir().getAbsolutePath();
        File apk = new File(apkPath, "apk.apk");
        if (apk.exists()) {
            return;
        }
        AssetManager assetManager = context.getAssets();
        try {
            InputStream ims = assetManager.open(strAssertFileName);
            String newPath = getExternalCacheDir().getAbsolutePath();
            FileOutputStream fos = new FileOutputStream(new File(newPath, "apk.apk"));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = ims.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            ims.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10086 && resultCode == RESULT_OK) {
            installProcess();
        }
    }
}
