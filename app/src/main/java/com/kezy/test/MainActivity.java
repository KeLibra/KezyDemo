package com.kezy.test;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private ImageView imageView;
    private LinearLayout ll_layout;

    private TextView tv_bottom;

    String path = "/storage/emulated/0/Android/data/com.ximalaya.ting.android/cache/download_apk/奇迹小说.apk";

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
//               startActivity(new Intent(MainActivity.this, MainActivity3.class));
//             boolean delete =  deleteApkFile(new File(path));
//               Log.v("-------msg", " ---delete : = " + delete);

               getSDCardAvailSize();
           }
       });

        List<Integer> blackCategoryIds = new ArrayList<>();
        blackCategoryIds.add(1);
        blackCategoryIds.add(2);
        blackCategoryIds.add(3);

//        PackageManager manager = getPackageManager();
//        manager.setInstallerPackageName("com.kezy.test", "cn.vastsky.onlineshop.installment");

    }


    private long getSDCardAvailSize() {
        String state = Environment.getExternalStorageState();
        long aaa=0;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();
            long totalSeize = blockSize * blockCount;
            aaa = availCount * blockSize;
            Log.e("-----msg -- size", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount);
            Log.e("-----msg -- size", "totalSeize:" + totalSeize );
            Log.e("-----msg -- size", "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize );

            queryStorage();
        }
        return aaa;
    }




    public void queryStorage(){
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());

        //存储块总数量
        long blockCount = statFs.getBlockCount();
        //块大小
        long blockSize = statFs.getBlockSize();
        //可用块数量
        long availableCount = statFs.getAvailableBlocks();
        //剩余块数量，注：这个包含保留块（including reserved blocks）即应用无法使用的空间
        long freeBlocks = statFs.getFreeBlocks();
        //这两个方法是直接输出总内存和可用空间，也有getFreeBytes
        //API level 18（JELLY_BEAN_MR2）引入
        long totalSize = statFs.getTotalBytes();
        long availableSize = statFs.getAvailableBytes();

        Log.d("statfs","total = " + getUnit(totalSize));
        Log.d("statfs","availableSize = " + getUnit(availableSize));

        //这里可以看出 available 是小于 free ,free 包括保留块。
        Log.d("statfs","total = " + getUnit(blockSize * blockCount));
        Log.d("statfs","available = " + getUnit(blockSize * availableCount));
        Log.d("statfs","free = " + getUnit(blockSize * freeBlocks));
    }

    private String[] units = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 单位转换
     */
    private String getUnit(float size) {
        int index = 0;
        while (size > 1024 && index < 4) {
            size = size / 1024;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s", size, units[index]);
    }
    /**
     * 下载前清空本地缓存的文件
     */
    private boolean deleteApkFile(File destFileDir) {
        try {
            if (!destFileDir.exists()) {
                return false;
            }
            if (destFileDir.isDirectory()) {
                File[] files = destFileDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        deleteApkFile(f);
                    }
                }
            }
            Log.v("-------msg", " return  ---delete : = ");
           return destFileDir.delete();
        }catch (Exception e) {
            e.printStackTrace();
            Log.v("-------msg", " exception   ---delete : = ");
        }
        Log.v("-------msg", " exception   ---delete : = ");
        return false;
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