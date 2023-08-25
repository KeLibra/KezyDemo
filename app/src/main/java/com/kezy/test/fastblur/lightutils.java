package com.kezy.test.fastblur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * @Author Kezy
 * @Time 2022/5/30
 * @Description
 */
public class lightutils {

    public static Bitmap handleImageEffect(Bitmap bitmap, float lum) {
        //传进来的bitmap默认不能修改  所以再创建一个bm
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //画布
        Canvas canvas = new Canvas(bm);
        //抗锯齿
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //修改亮度
        ColorMatrix lumMatrix = new ColorMatrix();
        //r g b a    1 表示全不透明
        lumMatrix.setScale(lum, lum, lum, 1);
        //组合Matrix
        ColorMatrix imageMatrix = new ColorMatrix();
        imageMatrix.postConcat(lumMatrix);
        //为画笔设置颜色过滤器
        paint.setColorFilter(new ColorMatrixColorFilter(imageMatrix));
        //在canvas上照着bitmap画
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return bm;
    }
}
