package com.kezy.test;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kezy.test.broadcast.BootReceiver;
import com.kezy.test.fastblur.Blur;
import com.kezy.test.weight.AdBottomBannerView;
import com.kezy.test.weight.AutoVagueBgImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ImageView imageView;
    private LinearLayout ll_layout;

    private TextView tv_bottom;

    private AutoVagueBgImageView bgImageView;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case 0:
                    bgImageView.setImageResource(R.mipmap.image_2);
                    break;
                case 1:
                    bgImageView.setImageResource(R.mipmap.image_1);
                    break;
                default:
                    break;
            }
            handler.sendEmptyMessageDelayed(((what++)%2),100);
            super.handleMessage(msg);
        }

    };

    int what = 0;
    Thread thread = new Thread(new Runnable(){

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true){

                try{
                    Thread.sleep(100);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.iv_test);


        bgImageView = findViewById(R.id.iv_iamge);
//        bgImageView.setImageResource(R.drawable.header1);
//        bgImageView.initVagueBg();

        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.mipmap.image);
        Bitmap bitmap = Blur.fastBlur(MainActivity.this, bitmap1, 30, 15);

        imageView.setImageResource(R.mipmap.image);
        imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));

        handler.sendEmptyMessageDelayed(((what++)%2),100);
//        thread.start();
//


//        String str = "看#30s#视频，免#1小时#声音广告";
//        Log.d("-------a", "str = " + str);
//
//        CharSequence sequence = createHightText(str);
//        Log.e("-------a", "seq = " + sequence);


//        mwParams = new WindowManager.LayoutParams();
//        mWindowManager = this.getWindowManager();
//        mwParams.type = WindowManager.LayoutParams.TYPE_TOAST;
//        mwParams.format = PixelFormat.RGBA_8888;
//        mwParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        mwParams.gravity = Gravity.LEFT | Gravity.TOP;
//        mwParams.x = 0;
//        mwParams.y = 100;
//        mwParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        mwParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        mwParams.width = 200;
//        mwParams.height = 200;

//        LayoutInflater inflater = this.getLayoutInflater();
//        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.layout_item, null);
////        mWindowManager.addView(mFloatLayout,mwParams);
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.gravity = Gravity.BOTTOM;
//        mFloatLayout.setLayoutParams(layoutParams);
//        ViewGroup mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
//
//        if (mFloatLayout.getParent() != null) {
//            ((ViewGroup) mFloatLayout.getParent()).removeView(mFloatLayout);
//        }
//        mRootView.addView(mFloatLayout);


        ViewGroup mRootView = getWindow().getDecorView().findViewById(android.R.id.content);

        AdBottomBannerView bottomBannerView = new AdBottomBannerView(this);

//        RelativeLayout.MarginLayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        ((RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        ((RelativeLayout.LayoutParams) params).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bottomBannerView.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }
        params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
        bottomBannerView.setLayoutParams(params);

        mRootView.addView(bottomBannerView);

        tv_bottom = findViewById(R.id.tv_bottom);


       tv_bottom.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(MainActivity.this, MainActivity3.class));
           }
       });

        List<Integer> blackCategoryIds = new ArrayList<>();
        blackCategoryIds.add(1);
        blackCategoryIds.add(2);
        blackCategoryIds.add(3);

//        PackageManager manager = getPackageManager();
//        manager.setInstallerPackageName("com.kezy.test", "cn.vastsky.onlineshop.installment");

    }


    WindowManager mWindowManager;
    WindowManager.LayoutParams mwParams;
    LinearLayout mFloatLayout;

    private String createHightText(String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }

        String[] split = content.split("#");
        Log.e("-------a", "split.length = " + (split.length/2));
        if (split.length < 4) {
            return content;
        }

        StringBuilder spannableStringBuilder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (i == 3) {
                Log.e("-------a1", "spl = " + split[i]);

            } else {
                Log.e("-------a2", "spl = " + split[i]);
                spannableStringBuilder.append(split[i]);
            }
        }

        return spannableStringBuilder.toString();
    }
    BootReceiver updateInstallReceiver;

    @Override
    protected void onStart() {
        super.onStart();

        updateInstallReceiver = new BootReceiver();
        IntentFilter intentFilter1 = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter1.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter1.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter1.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter1.addDataScheme("package");
        this.registerReceiver(updateInstallReceiver, intentFilter1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateInstallReceiver != null) {
            this.unregisterReceiver(updateInstallReceiver);
        }
    }
}