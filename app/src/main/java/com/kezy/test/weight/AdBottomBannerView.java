package com.kezy.test.weight;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.kezy.test.R;


/**
 * @Author Kezy
 * @Time 11/26/20
 * @Description
 */
public class AdBottomBannerView extends FrameLayout {


    private LinearLayout mFloatLayout;

    public AdBottomBannerView(@NonNull Context context) {
        super(context);
        init(context);
    }


    public AdBottomBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdBottomBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AdBottomBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View inflate = inflater.inflate(R.layout.layout_item, (ViewGroup) getRootView());


    }
}
