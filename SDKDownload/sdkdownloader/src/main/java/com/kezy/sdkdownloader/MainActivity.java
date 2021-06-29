package com.kezy.sdkdownloader;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kezy.notifylib.NotificationChannels;
import com.kezy.sdkdownloadlibs.DownloadTask;
import com.kezy.sdkdownloadlibs.downloader.xima.DownloadServiceManage;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;

public class MainActivity extends AppCompatActivity {


    private Button btnApi, btnXima;

    private String url_113MB = "https://js.a.kspkg.com/bs2/fes/kwai-android-ANDROID_KS_LDM_SJYY_CPA_NJYSJLLQKJB-gifmakerrelease-9.1.11.18473_x32_a35aec.apk";
    private String url_35MB = "http://b.xzfile.com/apk3/xgmfxsv1.0.9.241_downcc.com.apk";


    NotificationManager mNotifyManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationChannels.createAllNotificationChannels(MainActivity.this);

//        DownloadTask task = new DownloadTask(new DownloadServiceManage(MainActivity.this), new DownloadInfo(url_35MB));
        DownloadTask task1 = new DownloadTask(new DownloadServiceManage(MainActivity.this), new DownloadInfo(url_113MB));

        btnApi = findViewById(R.id.btn_api);
        btnApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                task.start(MainActivity.this);
                task1.start(MainActivity.this);
            }
        });


        btnXima = findViewById(R.id.btn_xima);

        btnXima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("-----------msg", "  -==== 1111 " + task.getStatus(url_35MB));
                Log.e("-----------msg", "  -====  22222 " + task1.getStatus(url_113MB));


            }
        });
    }
}