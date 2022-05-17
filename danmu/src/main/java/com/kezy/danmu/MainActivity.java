package com.kezy.danmu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pools;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llContainer;
    LayoutTransition transition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llContainer = (LinearLayout) findViewById(R.id.ll_container);
        transition = new LayoutTransition();
        //添加动画
        ObjectAnimator valueAnimator = ObjectAnimator.ofFloat(null, "alpha", 0, 1);
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //当前展示超过四条，执行删除动画
                if (llContainer.getChildCount() == 4) {
                    handler.sendEmptyMessage(1);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (llContainer.getChildCount() == 5)
                    //动画执行完毕，删除view
                    handler.sendEmptyMessage(2);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        transition.setAnimator(LayoutTransition.APPEARING, valueAnimator);
        //删除动画
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0, 0);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(null, new PropertyValuesHolder[]{alpha}).setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));

        transition.setAnimator(LayoutTransition.DISAPPEARING, objectAnimator);
        llContainer.setLayoutTransition(transition);

    }

    private String[] texts = new String[]{
            "1. 火来我在灰烬中等你",
            "2. 我对这个世界没什么可说的。我对这个世界没什么可说的。我对这个世界没什么可说的。",
            "3. 侠之大者，为国为民。",
            "4. 为往圣而继绝学"};


    Pools.SimplePool<TextView> textViewPool = new Pools.SimplePool<>(texts.length);

    private TextView obtainTextView() {
        TextView textView = textViewPool.acquire();
        if (textView == null) {
            textView = new TextView(MainActivity.this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setPadding(dp2px(10), dp2px(5), dp2px(10), dp2px(5));
            textView.setTextColor(0xffffffff);
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(14);
            textView.setTextColor(Color.parseColor("#fd5353"));
            Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
            drawable.setBounds(0, 0, 80, 80);
            textView.setCompoundDrawablesRelative(drawable, null, null, null);
            textView.setCompoundDrawablePadding(10);
            switch (index) {
                case 0:
//                    textView.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_launcher_background));
                    break;
                case 1:
//                    textView.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_launcher_background));

                    break;
                case 2:
//                    textView.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_launcher_background));

                    break;
                case 3:
//                    textView.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_launcher_background));

                    break;
            }
        }
        textView.setText(texts[index]);
        return textView;
    }

    private int dp2px(float dp) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }


    int index = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    TextView textView = obtainTextView();
                    llContainer.addView(textView);
                    sendEmptyMessageDelayed(0, 800);
                    index++;
                    if (index == 4) {
                        index = 0;
                    }
                    break;
                case 1:
                    //给展示的第一个view增加渐变透明动画
                    llContainer.getChildAt(0).animate().alpha(0).setDuration(transition.getDuration(LayoutTransition.APPEARING)).start();
                    break;
                case 2:
                    //删除顶部view
                    llContainer.removeViewAt(0);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(0);
    }
}