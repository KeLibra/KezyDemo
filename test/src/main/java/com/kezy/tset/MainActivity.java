package com.kezy.tset;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    // dev 修改内容
    private static String TAG = "-------msg";
    UseTimeDataManager mUseTimeDataManager;

    private Button btn, btn2;
    private TextView txt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = getIntent();
        if (intent != null) {
            //获取整个uri的链接
            String dataString = intent.getDataString();
            //获取相应Uri中的一些内容！

            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                String authority = data.getAuthority();
                String host = data.getHost();
                String port = String.valueOf(data.getPort());
                String path = data.getPath();
                String query = data.getQuery();
            }
            Log.e("---------msg", " -------- dataString " + dataString);
        }

        btn = findViewById(R.id.btn_click);
        btn2 = findViewById(R.id.btn_click2);
        btn2.setVisibility(View.GONE);
        txt = findViewById(R.id.tv_msg);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(btn2, "rotation", 0, 5f, 0, -5f,5f,0, -5f, 0);
        objectAnimator.setDuration(500);
        objectAnimator.setStartDelay(200);

        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(btn2,"translationY",100f, 0);
        objectAnimator2.setDuration(400);

        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(btn2,"alpha",0.2f,1f);
        objectAnimator3.setDuration(500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator2).with(objectAnimator3).before(objectAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                btn2.setVisibility(View.VISIBLE);
            }
        });



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn2.setVisibility(View.GONE);
                animatorSet.start();


                if(!checkAppUsagePermission(MainActivity.this)) {
                    requestAppUsagePermission(MainActivity.this);
                } else {
                    mUseTimeDataManager = UseTimeDataManager.getInstance(MainActivity.this);
                    mUseTimeDataManager.refreshData(1);

                    txt.setText(getJsonObjectStr());
                    Log.v("-------msg", " ----- get json = " + getTopActivityPackageName(MainActivity.this));
                }
            }
        });
    }


    public static String getTopActivityPackageName(@NonNull Context context) {
        final UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        if(usageStatsManager == null) {
            return "null";
        }

        String topActivityPackageName = "null - no";
        long time = System.currentTimeMillis();
        // 查询最后十秒钟使用应用统计数据
        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*1000, time);
        // 以最后使用时间为标准进行排序
        if(usageStatsList != null) {
            SortedMap<Long,UsageStats> sortedMap = new TreeMap<Long,UsageStats>();
            for (UsageStats usageStats : usageStatsList) {
                sortedMap.put(usageStats.getLastTimeUsed(),usageStats);
            }
            if(sortedMap.size() != 0) {
                topActivityPackageName =  sortedMap.get(sortedMap.lastKey()).getPackageName();
                Log.d(TAG,"Top activity package name = " + topActivityPackageName);
            }
        }

        return topActivityPackageName;
    }


    public String getJsonObjectStr() {
        String jsonAppdeTails = "";
        try {
            List<PackageInfo> packageInfos = mUseTimeDataManager.getmPackageInfoListOrderByTime();
            JSONObject jsonObject2 = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < packageInfos.size(); i++) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonArray.put(i, jsonObject.accumulate("count", packageInfos.get(i).getmUsedCount()));
                    jsonArray.put(i, jsonObject.accumulate("name", packageInfos.get(i).getmPackageName()));
                    jsonArray.put(i, jsonObject.accumulate("time", packageInfos.get(i).getmUsedTime()));
                    jsonArray.put(i, jsonObject.accumulate("appname", packageInfos.get(i).getmAppName()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return "";
                }

            }
            jsonObject2.put("details", jsonArray);
            jsonAppdeTails = jsonObject2.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonAppdeTails;
    }



    public static void checkUsageStateAccessPermission(Context context) {
        if(!checkAppUsagePermission(context)) {
            requestAppUsagePermission(context);
        }
    }

    public static boolean checkAppUsagePermission(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if(usageStatsManager == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        // try to get app usage state in last 1 min
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 60 * 1000, currentTime);
        if (stats.size() == 0) {
            return false;
        }

        return true;
    }

    public static void requestAppUsagePermission(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.i(TAG,"Start usage access settings activity fail!");
        }
    }



    /**
     * Use reflect to get Package Usage Statistics data.<br>
     */
    public static void getPkgUsageStats() {
        Log.d(TAG, "[getPkgUsageStats]");
        try {
            Class<?> cServiceManager = Class
                    .forName("android.os.ServiceManager");
            Method mGetService = cServiceManager.getMethod("getService",
                    java.lang.String.class);
            Object oRemoteService = mGetService.invoke(null, "usagestats");

            Class<?> cStub = Class
                    .forName("com.android.internal.app.IUsageStats$Stub");
            Method mUsageStatsService = cStub.getMethod("asInterface",
                    android.os.IBinder.class);
            Object oIUsageStats = mUsageStatsService.invoke(null,
                    oRemoteService);

            Class<?> cIUsageStatus = Class
                    .forName("com.android.internal.app.IUsageStats");
            Method mGetAllPkgUsageStats = cIUsageStatus.getMethod(
                    "getAllPkgUsageStats", (Class[]) null);
            Object[] oPkgUsageStatsArray = (Object[]) mGetAllPkgUsageStats
                    .invoke(oIUsageStats, (Object[]) null);
            Log.d(TAG, "[getPkgUsageStats] oPkgUsageStatsArray = "+oPkgUsageStatsArray);

            Class<?> cPkgUsageStats = Class
                    .forName("com.android.internal.os.PkgUsageStats");

            StringBuffer sb = new StringBuffer();
            sb.append("nerver used : ");
            for (Object pkgUsageStats : oPkgUsageStatsArray) {
                // get pkgUsageStats.packageName, pkgUsageStats.launchCount,
                // pkgUsageStats.usageTime
                String packageName = (String) cPkgUsageStats.getDeclaredField(
                        "packageName").get(pkgUsageStats);
                int launchCount = cPkgUsageStats
                        .getDeclaredField("launchCount").getInt(pkgUsageStats);
                long usageTime = cPkgUsageStats.getDeclaredField("usageTime")
                        .getLong(pkgUsageStats);
                if (launchCount > 0)
                    Log.d(TAG, "[getPkgUsageStats] "+ packageName + "  count: "
                            + launchCount + "  time:  " + usageTime);
                else {
                    sb.append(packageName + "; ");
                }
            }
            Log.d(TAG, "[getPkgUsageStats] " + sb.toString());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(TAG, "IllegalArgumentException 11 --  " + e.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "IllegalAccessException 22 --  " + e.toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG, "InvocationTargetException 33 --  " + e.toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            Log.e(TAG, "NoSuchFieldException 44 --  " + e.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "ClassNotFoundException 55 --  " + e.toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(TAG, "NoSuchMethodException 66 --  " + e.toString());
        }
    }




}