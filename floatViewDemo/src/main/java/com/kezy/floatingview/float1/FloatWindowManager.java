package com.kezy.floatingview.float1;

import android.content.Context;
import android.view.WindowManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;


public class FloatWindowManager {
    private static RecordScreenView mBallView;
    private static WindowManager mWindowManager;

    public static void addBallView(Context context) {
        if (mBallView == null) {
            WindowManager windowManager = getWindowManager(context);
            int screenWidth = windowManager.getDefaultDisplay().getWidth();
            int screenHeight = windowManager.getDefaultDisplay().getHeight();
            mBallView = new RecordScreenView(context);
            LayoutParams params = new LayoutParams();
            params.x = screenWidth / 2;
            params.y = screenHeight / 2 + 150;
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.LEFT | Gravity.TOP;
            params.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mBallView.setLayoutParams(params);
            windowManager.addView(mBallView, params);
        }
    }

    public static void removeBallView(Context context) {
        if (mBallView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mBallView);
            mBallView = null;
        }
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }
}
