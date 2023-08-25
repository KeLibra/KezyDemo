package com.kezy.a23test;

import android.Manifest;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private AppNetworkUsageAdapter mAdapter;
    private Timer mTimer;
    private NetworkStatsManager mNetworkStatsManager;

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;

    private static final int REQUEST_PERMISSIONS = 123;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PACKAGE_USAGE_STATS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mListView = findViewById(R.id.list_view);
//        mAdapter = new AppNetworkUsageAdapter(this, R.layout.item_app_network_usage, new ArrayList<AppNetworkUsage>());
//        mListView.setAdapter(mAdapter);
//
//        // 检查是否已经授权
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            // 如果没有授权，则申请授权
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_READ_PHONE_STATE);
//        }


        findViewById(R.id.tv_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainTest2Activity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopAppNetworkUsageMonitoring();
    }

    private void startAppNetworkUsageMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNetworkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Log.e("-------msg", "  -------- --- --startAppNetworkUsageMonitoring --> run");
                    final List<AppNetworkUsage> appNetworkUsages = new ArrayList<>();
                    long startTime = System.currentTimeMillis() - 1000 * 10; // 过去一分钟
                    long endTime = System.currentTimeMillis();
                    try {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

                        // 获取当前活动网络
                        Network activeNetwork = connectivityManager.getActiveNetwork();
                        int networkType = getActiveNetworkType();

                        // 获取ConnectivityManager和NetworkStatsManager实例
                        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                        NetworkStats networkStats = mNetworkStatsManager.querySummary(networkType, "", startTime, endTime);
                        Log.e("-------msg", "  -------- --- --networkStats = " + networkStats + " ， networkStats.hasNextBucket() " + networkStats.hasNextBucket());
                        while (networkStats != null && networkStats.hasNextBucket()) {
                            networkStats.getNextBucket(bucket);
                            int uid = bucket.getUid();
                            String[] packageNames = getPackageManager().getPackagesForUid(uid);
                            long rxBytes = bucket.getRxBytes();
                            long txBytes = bucket.getTxBytes();
                            if (packageNames != null && packageNames.length > 0) {
                                for (String packageName : packageNames) {
                                    Log.e("-------msg", "  -------- --- --packageName = " + packageName + " ，rxBytes = " + rxBytes + " ，txBytes = " + txBytes);
                                    appNetworkUsages.add(new AppNetworkUsage(packageName, rxBytes, txBytes));
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();
                            mAdapter.addAll(appNetworkUsages);
                        }
                    });
                }
            }, 0, 10000); // 每隔一分钟执行一次
        }
    }

    private void stopAppNetworkUsageMonitoring() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }


    // 在Activity的onCreate()方法中调用此方法
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasPermissions = true;
            for (String permission : PERMISSIONS) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    hasPermissions = false;
                    break;
                }
            }

            if (!hasPermissions) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            }
        }
    }

    // 处理用户的权限请求响应
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "权限已经授予", Toast.LENGTH_SHORT).show();
                    // 权限已经授予，可以进行相应操作
                    startAppNetworkUsageMonitoring();
                } else {
                    Toast.makeText(this, "无法获取读取手机状态权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    private int getActiveNetworkType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return ConnectivityManager.TYPE_WIFI;
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return ConnectivityManager.TYPE_MOBILE;
                }
            }
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                int type = activeNetworkInfo.getType();

                if (type == ConnectivityManager.TYPE_WIFI) {
                    return ConnectivityManager.TYPE_WIFI;
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    return ConnectivityManager.TYPE_MOBILE;
                }
            }
        }

        return -1;
    }
}


