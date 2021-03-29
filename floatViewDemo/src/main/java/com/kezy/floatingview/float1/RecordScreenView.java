package com.kezy.floatingview.float1;

import android.content.Context;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.kezy.floatingview.R;

import java.lang.reflect.Field;

public class RecordScreenView extends LinearLayout implements View.OnClickListener{
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private long mLastDownTime;
    private float mLastDownX;
    private float mLastDownY;
    private boolean mIsLongTouch;
    private boolean mIsTouching;
    private float mTouchSlop;
    private final static long LONG_CLICK_LIMIT = 20;
    private final static int TIME_COUNT = 0;
    private int mStatusBarHeight;
    private int mCurrentMode,time=0;
    private final static int MODE_NONE = 0x000;
    private final static int MODE_MOVE = 0x001;
    private int mOffsetToParent;
    private int mOffsetToParentY;
    private Context mContext;
    public RecordScreenView(Context context) {
        super(context);
        this.mContext=context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        initView();
    }
    private void initView() {
        View view = inflate(getContext(), R.layout.floating_view, this);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mCurrentMode = MODE_NONE;
        mStatusBarHeight = getStatusBarHeight();
        mOffsetToParent = dip2px(25);
        mOffsetToParentY = mStatusBarHeight + mOffsetToParent;
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIsTouching = true;
                        mLastDownTime = System.currentTimeMillis();
                        mLastDownX = event.getX();
                        mLastDownY = event.getY();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isLongTouch()) {
                                    mIsLongTouch = true;
                                }
                            }
                        }, LONG_CLICK_LIMIT);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mIsLongTouch && isTouchSlop(event)) {
                            return true;
                        }
                        if (mIsLongTouch && (mCurrentMode == MODE_NONE || mCurrentMode == MODE_MOVE)) {
                            mLayoutParams.x = (int) (event.getRawX() - mOffsetToParent);
                            mLayoutParams.y = (int) (event.getRawY() - mOffsetToParentY);
                            mWindowManager.updateViewLayout(RecordScreenView.this, mLayoutParams);//不断刷新悬浮窗的位置
                            mCurrentMode = MODE_MOVE;
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mIsTouching = false;
                        if (mIsLongTouch) {
                            mIsLongTouch = false;
                        }
                        mCurrentMode = MODE_NONE;
                        break;
                }
                return true;
            }
        });
    }
    private boolean isLongTouch() {
        long time = System.currentTimeMillis();
        if (mIsTouching && mCurrentMode == MODE_NONE && (time - mLastDownTime  == LONG_CLICK_LIMIT)) {
            return true;
        }
        return false;
    }
    /**
     * 判断是否是轻微滑动
     *
     * @param event
     * @return
     */
    private boolean isTouchSlop(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (Math.abs(x - mLastDownX) < mTouchSlop && Math.abs(y - mLastDownY) < mTouchSlop) {
            return true;
        }
        return false;
    }
    public void setLayoutParams(WindowManager.LayoutParams params) {
        mLayoutParams = params;
    }
    /**
     * 获取通知栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
    public int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics()
        );
    }

    @Override
    public void onClick(View v) {

    }
}