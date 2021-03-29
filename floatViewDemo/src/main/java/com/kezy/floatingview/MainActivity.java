package com.kezy.floatingview;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.kezy.floatingview.float1.FloatWindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.btn_jump1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity2.class));
            }
        });

////        if (Build.VERSION.SDK_INT >= 23) {
////            if (!Settings.canDrawOverlays(this)) {
////                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                startActivity(intent);
////            } else {
////                FloatWindowManager.addBallView(MainActivity.this);
////            }
////        }
//        FloatWindowManager.addBallView(MainActivity.this);

        initFloatView();
    }

    private void initFloatView() {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.BLACK);
        textView.setText("zhang phil @ csdn");
        textView.setTextSize(10);
        textView.setTextColor(Color.RED);

        //类型是TYPE_TOAST，像一个普通的Android Toast一样。这样就不需要申请悬浮窗权限了。
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

        //初始化后不首先获得窗口焦点。不妨碍设备上其他部件的点击、触摸事件。
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = 300;
        //params.gravity=Gravity.BOTTOM;

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "不需要权限的悬浮窗实现", Toast.LENGTH_LONG).show();
            }
        });

        WindowManager windowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        windowManager.addView(textView, params);
    }
}