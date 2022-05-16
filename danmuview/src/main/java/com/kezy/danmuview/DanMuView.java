package com.kezy.danmuview;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pools;

/**
 * @Author Kezy
 * @Time 2022/5/16
 * @Description
 */
public class DanMuView extends LinearLayout {

    private LayoutTransition transition;

    //最大展示数量 默认四条
    private int maxItem = 4;

    private String[] texts = new String[]{
            "火来我在灰烬中等你",
            "我对这个世界没什么可说的。我对这个世界没什么可说的。我对这个世界没什么可说的。",
            "侠之大者，为国为民。",
            "为往圣而继绝学"};

    public DanMuView(Context context) {
        super(context);
    }

    public DanMuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyDanmuView);
        maxItem = typedArray.getInteger(R.styleable.MyDanmuView_maxItem, maxItem);
        typedArray.recycle();
        init();
    }

    public DanMuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        setShowDividers(SHOW_DIVIDER_MIDDLE);
        transition = new LayoutTransition();
        //添加动画
        ObjectAnimator valueAnimator = ObjectAnimator.ofFloat(null, "alpha", 0, 1);
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //当前展示超过四条，执行删除动画
                if (getChildCount() == 4) {
                    handler.sendEmptyMessage(1);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (getChildCount() == 5)
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
        valueAnimator.setDuration(300);
        //删除动画
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0, 0);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(null, new PropertyValuesHolder[]{alpha}).setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));

        transition.setAnimator(LayoutTransition.DISAPPEARING, objectAnimator);
        setLayoutTransition(transition);
    }

    /**
     * 设置最大展示数量
     *
     * @param maxItem
     */
    public void setMaxItem(int maxItem) {
        this.maxItem = maxItem;
    }

    public void setData(String name, String cause) {
        View view = getTextView();
        //获取学生姓名
        TextView nameTxt = view.findViewById(R.id.studentNameTxt);
        //扣分原因
        TextView causeTxt = view.findViewById(R.id.causeTxt);

        //设置学生姓名
        nameTxt.setText(TextUtils.isEmpty(name) ? "佚名" : name);
        //扣分原因
        causeTxt.setText(TextUtils.isEmpty(cause) ? "无" : cause);
        setOrientation(VERTICAL);
        //添加子布局
        addView(view);
    }

    /**
     * 获取子控件布局
     *
     * @return View
     */
    private View getTextView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.item_live_danmu, null);
    }

    /**
     * 回收
     */
    public void destory() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (transition != null) {
            if (!transition.isRunning()) {
                transition = null;
            }
        }
    }

    int index = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    TextView textView = obtainTextView();
                    addView(textView);
                    sendEmptyMessageDelayed(0, 800);
                    setOrientation(VERTICAL);
                    //添加子布局

                    index++;
                    if (index == 4) {
                        index = 0;
                    }
                    break;
                case 1:
                    //给展示的第一个view增加渐变透明动画
                    getChildAt(0).animate().alpha(0).setDuration(transition.getDuration(LayoutTransition.APPEARING)).start();
                    break;
                case 2:
                    //删除顶部view
                    removeViewAt(0);
                    break;
            }
        }
    };

    Pools.SimplePool<TextView> textViewPool = new Pools.SimplePool<>(texts.length);

    private TextView obtainTextView() {
        TextView textView = textViewPool.acquire();
        if (textView == null) {
            textView = new TextView(getContext());
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

    private int dp2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    public void startPlay() {
        handler.sendEmptyMessage(0);
    }

    public void stopPlay () {
        handler.removeMessages(0);
    }


}
