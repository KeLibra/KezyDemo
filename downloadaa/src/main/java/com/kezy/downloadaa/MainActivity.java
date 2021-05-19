package com.kezy.downloadaa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kezy.downloadaa.apidownload.AdApiDownloadManager;

public class MainActivity extends AppCompatActivity {

    private Button btnDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDownload = findViewById(R.id.btn_download);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdApiDownloadManager.getInstance().downLoadApk(MainActivity.this,
                        "https://apk-1251952132.file.myqcloud.com/stable/xxl-xima6_qiji.apk", "cache", "测试apk");
            }
        });
    }
}