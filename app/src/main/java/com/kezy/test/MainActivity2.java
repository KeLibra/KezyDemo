package com.kezy.test;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class MainActivity2 extends AppCompatActivity {


    private ImageView iv_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        findViewById(R.id.tv_text1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/a.apk");
                Log.e("--------msg", " file = "  + file.exists() + " ,  name : " + file.toString());
              startInstall(MainActivity2.this,file);
            }
        });
        iv_image = findViewById(R.id.iv_image_www);


        ObjectAnimator translationX = new ObjectAnimator().ofFloat(iv_image,"translationX",-60 -25, 0);
        translationX.setDuration(300);
        translationX.setRepeatCount(2);
        translationX.setStartDelay(1000);
        translationX.setInterpolator(new AnticipateInterpolator());


        ObjectAnimator translationX2 = new ObjectAnimator().ofFloat(iv_image,"translationX",-60 -25, 0);
        translationX2.setDuration(300);
        translationX2.setRepeatCount(2);
        translationX2.setStartDelay(500);
        translationX2.setInterpolator(new AnticipateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.play(translationX).after(translationX2);
        set.start();
    }

    public static void startInstall(Context context, File file) {
        if (Build.VERSION.SDK_INT >= 29) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri apkUri = FileProvider.getUriForFile(context, "com.kezy.test.fileProvider", file);
            Log.d("-------msg", " 0000 -- = " + apkUri);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    Uri getUri() {
        Uri fileUri;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/a.apk");
        Log.e("--------msg", " file = "  + file.exists() + " ,  name : " + file.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(getBaseContext(), "com.kezy.test.fileProvider", file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    void installApk(final Uri uri) {
        if (uri == null) {
            Toast toast = Toast.makeText(getBaseContext(), "安装失败，请稍后重试", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        Log.e("-------msg", " -0-0------ = " + uri);
        Intent install = new Intent();
        install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.setAction(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(install);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}