package com.kezy.tset.view;

/**
 * @Author Kezy
 * @Time 2023/9/7
 * @Description
 */


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kezy.tset.R;


public class WaterWaveView extends View {
    private int widthSize;
    private int heightSize;
    private float[] mContentOneYs=null;
    private float[] mContentTwoYs=null;
    private float[] restoreOnes=null;
    private float[] restoreTwos=null;
    private float n=0.5f;
    private int SWINGONE;
    private int SWINGTWO;
    private int OFFSETONE=0;
    private int OFFSETTWO=0;
    private Paint mPaint1;
    private Paint mPaint2;
    private Paint circlePaint;
    private Canvas bitmapCanvas;
    private Bitmap bitmap;
    private float endValue;



    public WaterWaveView(Context context) {
        super(context);
        init();
    }

    public WaterWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaterWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WaterWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPaint1=new Paint();
        mPaint1.setColor(Color.parseColor("#AB9DCF"));
        mPaint1.setStrokeWidth(4);
        mPaint1.setAlpha(240);
        mPaint1.setStyle(Paint.Style.FILL);
        mPaint1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mPaint2=new Paint();
        mPaint2.setColor(Color.parseColor("#A2D1F3"));
        mPaint2.setStrokeWidth(4);
        mPaint2.setAlpha(240);
        mPaint2.setStyle(Paint.Style.FILL);
        mPaint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        circlePaint=new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GRAY);
        // circlePaint.setAlpha(56);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthSize=MeasureSpec.getSize(widthMeasureSpec);
        heightSize=widthSize;
        setMeasuredDimension(widthSize,heightSize);

        bitmap=Bitmap.createBitmap(widthSize,heightSize, Bitmap.Config.ARGB_8888);//实现圆球效果
        bitmapCanvas=new Canvas(bitmap);
        SWINGONE=widthSize/20;
        SWINGTWO=widthSize/10;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        getPoints();
    }
    private void getPoints(){
        mContentOneYs=new float[widthSize];
        mContentTwoYs=new float[widthSize];
        restoreOnes=new float[widthSize];
        restoreTwos=new float[widthSize];
        for (int i=0;i<widthSize;i++){
            mContentOneYs[i]=getposition1(i,SWINGONE,OFFSETONE,(int)(widthSize*n));
            mContentTwoYs[i]=getposition2(i,SWINGTWO,OFFSETTWO,(int)(widthSize*n));
        }
    }
    private float getposition1(int x,int swing,int offset,int baseHeight){
        float cycle=(float)(2*Math.PI)/widthSize;
        return (float)Math.sin(cycle*x+offset)*swing+baseHeight;
    }
    private float getposition2(int x,int swing,int offset,int baseHeight){
        float cycle=(float)(2*Math.PI)/widthSize;
        return (float)Math.cos(cycle*x+offset)*swing+baseHeight;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        bitmapCanvas.drawCircle(widthSize/2,heightSize/2,widthSize/2,circlePaint);//实现圆球效果

        canvas.save();
        getPoints();
        for (int i=0;i<widthSize;i++){
            final float x=i;
            final float y1=mContentOneYs[i];
            final float y2=mContentTwoYs[i];
            bitmapCanvas.drawLine(x,y1,x,heightSize,mPaint2);//实现了线的绘制最终生成了图，在画的球上画的线
            bitmapCanvas.drawLine(x,y2,x,heightSize,mPaint1);
        }
        canvas.drawBitmap(bitmap,0,0,null);//将球画在了主界面的View中。
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {//按下就会产生动画效果
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                return false;
            case MotionEvent.ACTION_UP:
                begainAnimation();
                invalidate();
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void begainAnimation(){//所有动画的实现都在这个函数里面
        final ValueAnimator valueAnimator=ValueAnimator.ofInt(0,20);
        final ValueAnimator valueAnimator1=ValueAnimator.ofInt(0,20);
        final ValueAnimator valueAnimator2=ValueAnimator.ofFloat(1,0.5f);
        valueAnimator2.setDuration(5000);
        valueAnimator.setDuration(5000);
        valueAnimator1.setDuration(5000);
        valueAnimator.setTarget(OFFSETTWO);
        valueAnimator1.setTarget(OFFSETONE);
        valueAnimator2.setTarget(n);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                OFFSETTWO=(int)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                OFFSETONE=(int)valueAnimator1.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                n=(float)valueAnimator2.getAnimatedValue();
            }
        });
        valueAnimator.start();
        valueAnimator1.start();
        valueAnimator2.start();
    }
    public void changgeState(float endValue){//给外部的一个接口
        this.endValue=1-endValue;
        begainAnimation();
    }

}