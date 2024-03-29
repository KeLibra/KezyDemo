package com.kezy.danmuview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.util.Log;
import android.util.Printer;
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

    private View inView;
    private boolean isPlaying = false;
    private LayoutTransition transition;

    private final int DEFAULT_MAX_ITEM = 4;
    private final int DEFAULT_DELAY_TIME = 1 * 1000;
    //最大展示数量 默认四条
    private int maxItem = DEFAULT_MAX_ITEM;
    private int delayTime = DEFAULT_DELAY_TIME;

    private boolean mIsAutoPlay = true;

    private String[] texts = new String[]{
            "1. 火来我在灰烬中等你",
            "2. 我对这个世界没什么可说的。我对这个世界没什么可说的。我对这个世界没什么可说的。",
            "3. 侠之大者，为国为民。",
            "4. 为往圣而继绝学"};

    public DanMuView(Context context) {
        super(context);
        initView();
    }

    public DanMuView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyDanmuView);
        maxItem = typedArray.getInteger(R.styleable.MyDanmuView_maxItem, maxItem);
        typedArray.recycle();
        init();
        initView();
    }

    public DanMuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean ismIsAutoPlay() {
        return mIsAutoPlay;
    }

    public void setIsAutoPlay(boolean IsAutoPlay) {
        this.mIsAutoPlay = IsAutoPlay;
    }

    private void init() {
        setShowDividers(SHOW_DIVIDER_MIDDLE);
        setOrientation(VERTICAL);

        transition = new LayoutTransition();
        //添加动画
        ObjectAnimator addItemAnimator = ObjectAnimator.ofFloat(null, "alpha", 0, 1);
        addItemAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                //当前展示超过四条，执行删除动画
                if (getChildCount() >= maxItem) {
                    handler.sendEmptyMessage(1);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (getChildCount() > maxItem) {
                    //动画执行完毕，删除view
                    handler.sendEmptyMessage(2);
                }
            }
        });

//        addItemAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                //当前展示超过四条，执行删除动画
//                if (getChildCount() >= maxItem) {
//                    handler.sendEmptyMessage(1);
//                }
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (getChildCount() > maxItem) {
//                    //动画执行完毕，删除view
//                    handler.sendEmptyMessage(2);
//                }
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });


        transition.setAnimator(LayoutTransition.APPEARING, addItemAnimator);

        //删除动画
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0, 0);
        ObjectAnimator deleteItemAnimator = ObjectAnimator.ofPropertyValuesHolder(null, new PropertyValuesHolder[]{alpha})
                .setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
        transition.setAnimator(LayoutTransition.DISAPPEARING, deleteItemAnimator);

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

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    Pools.SimplePool<View> viewSimplePool = new Pools.SimplePool<>(texts.length);

    /**
     * 获取子控件布局
     *
     * @return View
     */
    private View getTextView() {

        inView = viewSimplePool.acquire();
        if (inView == null) {
            inView = LayoutInflater.from(getContext()).inflate(R.layout.item_live_danmu, null);
            TextView textView = inView.findViewById(R.id.causeTxt);
            textView.setText(texts[index]);
        }

        inView.setPivotX(30);
        inView.setPivotY(100);
//        invalidate();

        return inView;
    }

    /**
     * 回收
     */
    public void destory() {
        isPlaying = false;
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
                    Log.w("-------msg", " ------ 更新一个view " + index + ", length = " + texts.length);
                    if (index >= texts.length &&texts.length <= maxItem) {
                        stopPlay();
                        return;
                    }
                    if (index >= texts.length) {
                        index = 0;
                    }

                    View textView = getTextView();
                    addView(textView);
                    sendEmptyMessageDelayed(0, delayTime);
                    index++;
                    if (getChildCount() > maxItem + 1) {
                        removeViewAt(0);
                    }
                    break;
                case 1:
                    Log.d("-------msg", " ------给展示的第一个view增加渐变透明动画 ");
                    if (getChildAt(0) != null && getChildAt(0).animate() != null
                            && getChildAt(0).animate().alpha(0) != null) {
                        //给展示的第一个view增加渐变透明动画
                        getChildAt(0).animate().alpha(0).setDuration(transition.getDuration(LayoutTransition.APPEARING)).start();
                    }
                    break;
                case 2:
                    //删除顶部view
                    Log.v("-------msg", " ------删除顶部view ");
                    removeViewAt(0);
                    break;
            }
        }
    };


    public void startPlay() {
        Log.e("-------msg", " ------ startPlay -----   isPlaying = " + isPlaying);
        if (isPlaying) {
            return;
        }
        Log.e("-------msg", " ------ startPlay -----   mIsAutoPlay = " + mIsAutoPlay);
        if (!mIsAutoPlay) {
            return;
        }
        if (handler != null) {
            isPlaying = true;
            handler.sendEmptyMessage(0);
        }
    }

    public void stopPlay() {
        if (handler != null) {
            isPlaying = false;
            handler.removeMessages(0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.e("-------msg", " ------ onAttachedToWindow ----- ");
//        startPlay();

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        stopPlay();
        Log.e("-------msg", " ------ onDetachedFromWindow -----");
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            Log.d("-------msg", "可见 ---- mIsAutoPlay = " + mIsAutoPlay);
            //开始某些任务
//            startPlay();
        } else if (visibility == INVISIBLE || visibility == GONE) {
            Log.d("-------msg", "不可见");
            //停止某些任务
//            stopPlay();
        }
    }
}
