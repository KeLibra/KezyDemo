package com.kezy.test.weight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.kezy.test.fastblur.FastBlur;

/**
 * @Author Kezy
 * @Time 2020/10/28
 * @Description
 */
@SuppressLint("AppCompatCustomView")
public class AutoVagueBgImageView extends ImageView {
    public AutoVagueBgImageView(Context context) {
        super(context);
    }

    public AutoVagueBgImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoVagueBgImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoVagueBgImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void initVagueBg() {

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                buildDrawingCache();
                setVagueBg();
                return true;
            }
        });
    }

    private void setVagueBg() {
        setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
        setDrawingCacheEnabled(false);
        setBackground(null);
        setImageResource(-1);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.header1);

        Log.e("---------------msg", " bitmap == " + bitmap);
        if (bitmap != null) {
//            setImageBitmap(bitmap);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap vagueBitmap = FastBlur.doBlur(bitmap, 20, true);
            Log.e("---------------msg", " vagueBitmap == " + vagueBitmap.getWidth());
            setBackground(new BitmapDrawable(vagueBitmap));
        }
    }
}
