package com.kezy.test.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * @Author Kezy
 * @Time 1/4/21
 * @Description
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("--------msg ---------------:");
        //接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            System.out.println("--------msg  安装了: --  " + packageName + "   -- 包名的程序");

            PackageManager pm = context.getPackageManager();
            pm.setInstallerPackageName(packageName.replace("package:", ""), "com.kezy.test");
            String installer;
//            try {
                installer = context.getPackageManager().getInstallerPackageName(packageName.replace("package:", ""));
                Log.d("-------msg", "" + installer);

//            } catch (Exception e) {
//                Log.e("-------msg", e.toString());
//            }
        }
        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageNames = intent.getDataString();
            System.out.println("--------msg 卸载了:" + packageNames + "包名的程序");

        }
    }
}
