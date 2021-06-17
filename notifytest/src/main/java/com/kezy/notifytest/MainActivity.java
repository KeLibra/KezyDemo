package com.kezy.notifytest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class MainActivity extends AppCompatActivity {


    Bitmap LargeBitmap = null;
    private static final int NOTIFYID_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建大图标的Bitmap
        LargeBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mNManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        showNotify();

        findViewById(R.id.btn_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotify();
            }
        });

    }

    private NotificationManager mNManager;
    private Notification notify1;

    private void showNotify() {
        //定义一个PendingIntent点击Notification后启动一个Activity
        Intent it = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pit = PendingIntent.getActivity(MainActivity.this, 0, it, 0);

        NotificationManager manager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //需添加的代码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "default";
            String channelName = "默认通知";
            manager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH));
        }
        //
        Notification notification =new NotificationCompat.Builder(this,"default")
        .setContentTitle("叶良辰")                        //标题
                .setContentText("我有一百种方法让你呆不下去~")      //内容
                .setSubText("——记住我叫叶良辰")                    //内容下面的一小段文字
                .setTicker("收到叶良辰发送过来的信息~")             //收到信息后状态栏显示的文字信息
                .setWhen(System.currentTimeMillis())           //设置通知时间
                .setSmallIcon(R.mipmap.ic_launcher)            //设置小图标
                .setLargeIcon(LargeBitmap)                     //设置大图标
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)    //设置默认的三色灯与振动器
                .setAutoCancel(false)                           //设置点击后取消Notification
                .setContentIntent(pit)//设置PendingIntent
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        notification.defaults = Notification.DEFAULT_ALL;
        manager.notify(1,notification);
    }
}