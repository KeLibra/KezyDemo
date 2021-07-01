package com.kezy.sdkdownloadlibs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

/**
 * @Author Kezy
 * @Time 2021/7/1
 * @Description
 */
public class GetBroadcast extends BroadcastReceiver {
    private static GetBroadcast mReceiver = new GetBroadcast();
    private static IntentFilter mIntentFilter;
    public static void registerReceiver(Context context) {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addDataScheme("package");
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        context.registerReceiver(mReceiver, mIntentFilter);
    }
    public static void unregisterReceiver(Context context) {
        context.unregisterReceiver(mReceiver);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("------------msg", " --------  安装了------------ intent = " + intent.getAction());
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            Toast.makeText(context, "有应用被添加", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            Toast.makeText(context, "有应用被删除", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();
        }
    }
}
