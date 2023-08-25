package com.kezy.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class AnimActivity extends AppCompatActivity {

    TextView tv_btn, tv_bg;
    Button bbbbb, aaaaa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim);

        tv_btn = findViewById(R.id.tv_btn);
        tv_btn.setVisibility(View.GONE);
        tv_bg = findViewById(R.id.tv_bg);
        tv_bg.setVisibility(View.GONE);
        bbbbb = findViewById(R.id.bbbbb);
        aaaaa = findViewById(R.id.aaaaa);


        // step 1
        // x1= translationY 向上移则为-x，向下为x ，x2 = 原本位置
        ObjectAnimator btnTransY = getStep1TranslationY(tv_btn, 200);
        ObjectAnimator btnAlpha = getStep1Alpha(tv_btn, 0, 1);
        ObjectAnimator bgTransY = getStep1TranslationY(tv_bg, 200);
        ObjectAnimator bgAlpha = getStep1Alpha(tv_bg, 0, 1);


        // step 2
        ObjectAnimator bgAlpha2 = getStep1Alpha(tv_bg,1, 0);


        AnimatorSet animatorSet = new AnimatorSet();  //组合动画
        animatorSet.playTogether(btnTransY, btnAlpha, bgTransY, bgAlpha); //设置动画
        animatorSet.play(bgAlpha2).after(3000);
        animatorSet.setStartDelay(1000);
        animatorSet.start(); //启动

        btnTransY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                tv_btn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                tv_btn.setBackgroundColor(Color.BLUE);
                tv_btn.setTextColor(Color.WHITE);
            }
        });
        btnAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                tv_bg.setVisibility(View.VISIBLE);
            }
        });

        bbbbb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatorSet.cancel();
            }
        });

        aaaaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatorSet.start();
            }
        });
    }

    private ObjectAnimator getStep1TranslationY(Object object, float fromTrans) {
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator translationY = new ObjectAnimator().ofFloat(object, "translationY", fromTrans, 0);
        translationY.setDuration(1000);
        return translationY;
    }

    private ObjectAnimator getStep1Alpha(Object ob, float fromAlpha, float toAlpha) {
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator alphaTotal = new ObjectAnimator().ofFloat(ob, "alpha", fromAlpha, toAlpha);
        alphaTotal.setDuration(1000);
        return alphaTotal;
    }

    private ObjectAnimator getStep2Alpha(Object ob) {
        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator alphaTotal = new ObjectAnimator().ofFloat(ob, "alpha", 1, 0);
        alphaTotal.setDuration(500);
        return alphaTotal;
    }
}