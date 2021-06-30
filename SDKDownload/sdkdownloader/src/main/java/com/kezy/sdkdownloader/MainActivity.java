package com.kezy.sdkdownloader;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.kezy.notifylib.NotificationChannels;
import com.kezy.sdkdownloadlibs.downloader.DownloadUtils;
import com.kezy.sdkdownloadlibs.downloader.xima.DownloadServiceManage;
import com.kezy.sdkdownloadlibs.impls.TaskImpl;
import com.kezy.sdkdownloadlibs.listener.DownloadStatusChangeListener;
import com.kezy.sdkdownloadlibs.task.DownloadInfo;
import com.kezy.sdkdownloadlibs.task.TaskManager;

public class MainActivity extends AppCompatActivity {


    private Button btnApi, btnXima, btnDownload;

    private String url_113MB = "https://js.a.kspkg.com/bs2/fes/kwai-android-ANDROID_KS_LDM_SJYY_CPA_NJYSJLLQKJB-gifmakerrelease-9.1.11.18473_x32_a35aec.apk";
    private String url_35MB = "http://b.xzfile.com/apk3/xgmfxsv1.0.9.241_downcc.com.apk";

    private ProgressBar pbBar;
    private TextView tvPb;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pbBar = findViewById(R.id.probar);
        btnDownload = findViewById(R.id.btn_download);
        tvPb = findViewById(R.id.tv_pb);

        NotificationChannels.createAllNotificationChannels(MainActivity.this);

//        DownloadTask task = new DownloadTask(new DownloadServiceManage(MainActivity.this), new DownloadInfo(url_35MB));
//        DownloadTask task1 = new DownloadTask(new DownloadServiceManage(MainActivity.this), new DownloadInfo(url_113MB));

//        DownloadTask task = new DownloadTask(new ApiDownloadManager(), new DownloadInfo(url_35MB));
//        DownloadTask task1 = new DownloadTask(new ApiDownloadManager(), new DownloadInfo(url_113MB));


        TaskImpl task = TaskManager.getInstance().createDownloadTask(new DownloadServiceManage(MainActivity.this),
                new DownloadInfo.Builder(url_35MB).build());

        btnApi = findViewById(R.id.btn_api);
        btnApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.start(MainActivity.this);
//                task1.start(MainActivity.this);
            }
        });


        btnXima = findViewById(R.id.btn_xima);

        btnXima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("-----------msg", "  -==== 1111 " + task.getStatus());
//                Log.e("-----------msg", "  -====  22222 " + task1.getStatus());

                DownloadUtils.installApk(MainActivity.this, task.getInfo().path);
            }
        });


        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = task.getStatus();
                switch (status) {
                    case DownloadInfo.Status.WAITING:
                        task.start(MainActivity.this);
                        break;
                    case DownloadInfo.Status.STARTED:
                    case DownloadInfo.Status.DOWNLOADING:
                        task.pause(MainActivity.this);
                        break;
                    case DownloadInfo.Status.FINISHED:
                        task.install(MainActivity.this);
                        break;
                    case DownloadInfo.Status.STOPPED:
                        task.reStart(MainActivity.this);
                        break;
                    case DownloadInfo.Status.DELETE:
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + status);
                }
            }
        });

        task.setDownloadStatusListener(new DownloadStatusChangeListener() {
            @Override
            public void onStart(String onlyKey, boolean isRestart) {
                Log.v("--------msg", " ---- " + onlyKey);
                btnDownload.setText("下载中...");
                pbBar.setProgress(0);
                tvPb.setText("0 %");
            }

            @Override
            public void onPause(String onlyKey) {
                btnDownload.setText("继续");
            }

            @Override
            public void onContinue(String onlyKey) {
                btnDownload.setText("下载中...");
            }

            @Override
            public void onRemove(String onlyKey) {

            }

            @Override
            public void onProgress(String onlyKey, int progress) {
                btnDownload.setText("下载中...");
                pbBar.setProgress(progress);
                tvPb.setText(progress + " %");
            }

            @Override
            public void onError(String onlyKey) {
                btnDownload.setText("失败");
            }

            @Override
            public void onSuccess(String onlyKey) {
                btnDownload.setText("安装");
                pbBar.setProgress(100);
                tvPb.setText("100 %");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}