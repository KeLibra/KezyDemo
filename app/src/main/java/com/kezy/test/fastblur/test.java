package com.kezy.test.fastblur;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * @Author Kezy
 * @Time 2022/5/30
 * @Description
 */
public class test {

    public static Bitmap handleImageNegative(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int color;
        int r, g, b, a;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] oldPx = new int[width * height];
        int[] newPx = new int[width * height];
        bm.getPixels(oldPx, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++) {
            color = oldPx[i];
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);
            //
//            r = 255 - r;
//            g = 255 - g;
//            b = 255 - b;
            b = 85;

//            if (r > 255) {
//                r = 255;
//            } else if (r < 0) {
//                r = 0;
//            }
//            if (g > 255) {
//                g = 255;
//            } else if (g < 0) {
//                g = 0;
//            }
//            if (b > 255) {
//                b = 255;
//            } else if (b < 0) {
//                b = 0;
//            }
            newPx[i] = Color.argb(a, r, g, b);
        }
        bmp.setPixels(newPx, 0, width, 0, 0, width, height);
        return bmp;
    }
}
