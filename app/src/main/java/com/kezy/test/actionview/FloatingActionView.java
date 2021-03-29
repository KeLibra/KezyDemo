package com.kezy.test.actionview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kezy.test.R;


/**
 * @Author Kezy
 * @Time 2/3/21
 * @Description
 */
public class FloatingActionView extends FrameLayout {
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int POSITION_TOP_CENTER = 1;
    public static final int POSITION_TOP_RIGHT = 2;
    public static final int POSITION_RIGHT_CENTER = 3;
    public static final int POSITION_BOTTOM_RIGHT = 4;
    public static final int POSITION_BOTTOM_CENTER = 5;
    public static final int POSITION_BOTTOM_LEFT = 6;
    public static final int POSITION_LEFT_CENTER = 7;
    public static final int POSITION_TOP_LEFT = 8;
    private View contentView;

    @SuppressLint("UseCompatLoadingForDrawables")
    public FloatingActionView(Activity activity, FloatingActionView.LayoutParams layoutParams, int theme, Drawable backgroundDrawable, int position, View contentView, android.widget.FrameLayout.LayoutParams contentParams) {
        super(activity);
        this.setPosition(position, layoutParams);
        if (backgroundDrawable == null) {
            if (theme == 0) {
                backgroundDrawable = activity.getResources().getDrawable(R.drawable.button_action_selector);
            } else {
                backgroundDrawable = activity.getResources().getDrawable(R.drawable.button_action_dark_selector);
            }
        }

        this.setBackgroundResource(backgroundDrawable);
        if (contentView != null) {
            this.setContentView(contentView, contentParams);
        }

        this.setClickable(true);
        this.attach(layoutParams);
    }

    public void setPosition(int position, android.widget.FrameLayout.LayoutParams layoutParams) {
        byte gravity;
        switch(position) {
            case 1:
                gravity = 49;
                break;
            case 2:
                gravity = 53;
                break;
            case 3:
                gravity = 21;
                break;
            case 4:
            default:
                gravity = 85;
                break;
            case 5:
                gravity = 81;
                break;
            case 6:
                gravity = 83;
                break;
            case 7:
                gravity = 19;
                break;
            case 8:
                gravity = 51;
        }

        layoutParams.gravity = gravity;
        this.setLayoutParams(layoutParams);
    }

    public void setContentView(View contentView, android.widget.FrameLayout.LayoutParams contentParams) {
        this.contentView = contentView;
        android.widget.FrameLayout.LayoutParams params;
        if (contentParams == null) {
            params = new android.widget.FrameLayout.LayoutParams(-2, -2, 17);
            int margin = this.getResources().getDimensionPixelSize(R.dimen.action_button_content_margin);
            params.setMargins(margin, margin, margin, margin);
        } else {
            params = contentParams;
        }

        params.gravity = 17;
        contentView.setClickable(false);
        this.addView(contentView, params);
    }

    public void attach(android.widget.FrameLayout.LayoutParams layoutParams) {
        ((ViewGroup)this.getActivityContentView()).addView(this, layoutParams);
    }

    public void detach() {
        ((ViewGroup)this.getActivityContentView()).removeView(this);
    }

    public View getActivityContentView() {
        return ((Activity)this.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
    }

    private void setBackgroundResource(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= 16) {
            this.setBackground(drawable);
        } else {
            this.setBackgroundDrawable(drawable);
        }

    }

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

    public static class Builder {
        private Activity activity;
        private FloatingActionView.LayoutParams layoutParams;
        private int theme;
        private Drawable backgroundDrawable;
        private int position;
        private View contentView;
        private FloatingActionView.LayoutParams contentParams;

        public Builder(Activity activity) {
            this.activity = activity;
            int size = activity.getResources().getDimensionPixelSize(R.dimen.action_button_size);
            int margin = activity.getResources().getDimensionPixelSize(R.dimen.action_button_margin);
            FloatingActionView.LayoutParams layoutParams = new FloatingActionView.LayoutParams(size, size, 85);
            layoutParams.setMargins(margin, margin, margin, margin);
            this.setLayoutParams(layoutParams);
            this.setTheme(0);
            this.setPosition(4);
        }

        public FloatingActionView.Builder setLayoutParams(FloatingActionView.LayoutParams params) {
            this.layoutParams = params;
            return this;
        }

        public FloatingActionView.Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public FloatingActionView.Builder setBackgroundDrawable(Drawable backgroundDrawable) {
            this.backgroundDrawable = backgroundDrawable;
            return this;
        }

        public FloatingActionView.Builder setBackgroundDrawable(int drawableId) {
            return this.setBackgroundDrawable(this.activity.getResources().getDrawable(drawableId));
        }

        public FloatingActionView.Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public FloatingActionView.Builder setContentView(View contentView) {
            return this.setContentView(contentView, (FloatingActionView.LayoutParams)null);
        }

        public FloatingActionView.Builder setContentView(View contentView, FloatingActionView.LayoutParams contentParams) {
            this.contentView = contentView;
            this.contentParams = contentParams;
            return this;
        }

        public FloatingActionView build() {
            return new FloatingActionView(this.activity, this.layoutParams, this.theme, this.backgroundDrawable, this.position, this.contentView, this.contentParams);
        }
    }
}
