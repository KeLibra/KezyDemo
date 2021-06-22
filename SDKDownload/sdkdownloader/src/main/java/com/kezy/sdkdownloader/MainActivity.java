package com.kezy.sdkdownloader;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kezy.sdkdownloadlibs.downloader.api.AdApiDownloadManager;
import com.kezy.sdkdownloadlibs.task.DownloadTask;

public class MainActivity extends AppCompatActivity {


    private Button btnApi, btnXima;

    private String url_113MB = "https://js.a.kspkg.com/bs2/fes/kwai-android-ANDROID_KS_LDM_SJYY_CPA_NJYSJLLQKJB-gifmakerrelease-9.1.11.18473_x32_a35aec.apk";
    private String url_35MB = "http://b.xzfile.com/apk3/xgmfxsv1.0.9.241_downcc.com.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TaskImpl manager = new AdApiDownloadManager();
//
//        DownloadServiceManage v2 = new DownloadServiceManage(MainActivity.this);


        DownloadTask info = new DownloadTask();
        info.url = url_35MB;
        info.taskManager = new AdApiDownloadManager();

        btnApi = findViewById(R.id.btn_api);
        btnApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                manager.startDownload(MainActivity.this, url_35MB);
//                v2.downLoadAPK(url_35MB);

                info.taskManager.startDownload(MainActivity.this, info.url);
            }
        });


        btnXima = findViewById(R.id.btn_xima);

        btnXima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.v("--------msg", " ------- status 111 = " + manager.getStatus(MainActivity.this, url_35MB));

//                v2.pauseDownload(MainActivity.this, url_35MB);
                Log.v("--------msg", " ------- status 333 = " + info.taskManager.getStatus(MainActivity.this, url_35MB));
            }
        });
    }
}