package com.kezy.test.fastblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Process;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.nio.ByteBuffer;

/**
 * @author xmly
 */

public class Blur {

    public static int DARK_DEFAULT = 50;
    public static int RADIUS_DEFAULT = 25;
    private static RenderScript mRenderScript;

    public static Bitmap fastBlur(Context context, Bitmap sentBitmap, int radius) {
        return fastBlur(context, sentBitmap, radius, 0);
    }

    public static boolean isColorDark(int color){
        double darkness = 1-(0.299* Color.red(color) + 0.587* Color.green(color) + 0.114* Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    public static Bitmap fastBlur(Context context, Bitmap sentBitmap, int radius, int darkness){
        return fastBlur(context, sentBitmap, radius, darkness, null);
    }

    /**
     * RGB 565格式图片转RGB 8888格式图片
     * @param src
     * @return
     */
    public static Bitmap getTransformBitmap(Bitmap src) {
        int width = src.getWidth();         // 宽
        int height = src.getHeight();       // 高
        int count = src.getByteCount();     // 获取图片的RGB 565颜色数组总数
        ByteBuffer buffer = ByteBuffer.allocate(count);
        src.copyPixelsToBuffer(buffer);
        byte[] data = buffer.array();       //获取数组
        int sum = width * height;
        int[] pixels = new int[sum];

        for (int i = 0; i < sum; i++) {
            int tmpint = data[i * 2 + 0] + data[i * 2 + 1] * 256;
            int a = 0xff;                           //透明度
            int r = (tmpint & 0xf800) >> 11;        //红
            int g = (tmpint & 0x07e0) >> 5;         //绿
            int b = (tmpint & 0x001f);              //蓝

            r = r << 3;
            g = g << 2;
            b = b << 3;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | (b);
        }
        Bitmap bmp = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmp;    //旋转屏幕
    }

        /**
         *
         * @param context
         * @param sentBitmap
         * @param radius 模糊程度
         * @param darkness 其实是darkness,增加black的百分比黑度，取值0-100
         * @return
         */
    public static synchronized Bitmap fastBlur(Context context, Bitmap sentBitmap, int radius, int darkness, String url) {
        if (context == null)
            return null;
        if (sentBitmap == null){
            return null;
        }
        //不需要模糊
        if (radius == 0)
            return sentBitmap;
        if (sentBitmap.getWidth() == 0 || sentBitmap.getHeight() == 0){
            return sentBitmap;
        }
        int byteCount = sentBitmap.getByteCount()/sentBitmap.getWidth()/sentBitmap.getHeight();
        if (byteCount == 2){
            Bitmap bitmap = null;
            try {
                bitmap = getTransformBitmap(sentBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                return sentBitmap;
            }
            if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0){
                return sentBitmap;
            }
            byteCount = bitmap.getByteCount()/bitmap.getWidth()/bitmap.getHeight();
            if (byteCount != 4){
                return sentBitmap;
            }
            sentBitmap = bitmap;
        }
        if (byteCount != 4){
            return sentBitmap;
        }
        //适配不同明暗的图片
//        Palette palette = Palette.from(sentBitmap).generate();
//        Palette.Swatch swatch = palette.getDominantSwatch();
//        if(swatch!=null) {
//            int color = swatch.getRgb();
//            double darknessRatio = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
//            darknessRatio = darknessRatio+0.5>1?1:darknessRatio+0.5;
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            sentBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
//            byte[] bitmapBytes = baos.toByteArray();
//            Log.d("blur","lightness0:"+darkness+" darknessRatio:"+darknessRatio+" color:"+color+" url:"+url+" bitmapmd5:"+ MD5.md5(bitmapBytes));
//            darkness = (int) (darkness*darknessRatio);
//            Log.d("blur","lightness1:"+darkness);
//        }else{
//            Log.d("blur","getDominantSwatch null lightness2:"+darkness);
//        }
        //部分手机使用RenderScript 4.2.2会崩溃
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Bitmap bitmap = null;
                int maxWidth = 70;
                if (sentBitmap.getWidth() > maxWidth) {
                    bitmap = ThumbnailUtils.extractThumbnail(sentBitmap, maxWidth, (int) (maxWidth * (sentBitmap.getHeight() * 1.0f / sentBitmap.getWidth())));
                } else {
                    bitmap = sentBitmap.copy(sentBitmap.getConfig() == null ? Bitmap.Config.ARGB_8888 : sentBitmap.getConfig(), true);
                }
                if(mRenderScript == null)
                    mRenderScript = RenderScript.create(context);
                final Allocation input = Allocation.createFromBitmap(mRenderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                        Allocation.USAGE_SCRIPT);
                final Allocation output = Allocation.createTyped(mRenderScript, input.getType());
                final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
                if (radius > 25) {
                    radius = 25;
                }
                script.setRadius(radius);
                script.setInput(input);
                script.forEach(output);
                output.copyTo(bitmap);
                if(darkness!=0){
                    bitmap = darkenBitMap(bitmap,darkness);
                }
                input.destroy();
                output.destroy();
                script.destroy();
                return bitmap;
            } catch (Throwable e) {
//                XDCSCollectUtil.statErrorToXDCS("fastBlurError", "错误的信息是 == " + e.getMessage());
                e.printStackTrace();
            }finally {
                try{
                    if (mRenderScript!=null)mRenderScript.destroy();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        radius+=15;
        try {
            if (sentBitmap == null) {
                return null;
            }
            Thread.currentThread().setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig() == null ? Bitmap.Config.ARGB_8888 : sentBitmap.getConfig(), true);

            if (radius < 1) {
                return (null);
            }

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
//			Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )77 66 36
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
                    if (darkness != 0) {
                        pix[yi] = adjustLightness(pix[yi], darkness);
                    }
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

//			Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
            return (bitmap);
        } catch (Exception e) {
//            XDCSCollectUtil.statErrorToXDCS("fastBlurError", "错误的信息是2 == " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap darkenBitMap(Bitmap bm, int lightness) {

        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        int overlay = Color.argb((int) (255 * (Math.abs(lightness) / 100f)), 0, 0, 0);
        ColorFilter filter = new PorterDuffColorFilter(overlay, PorterDuff.Mode.SRC_OVER);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bm, 0, 0, paint);

        return bm;
    }


    private static int adjustLightness(int color, int lightness) {
        int pixelsR, pixelsG, pixelsB, pixelsA;
        pixelsA = Color.alpha(color);
        pixelsR = Color.red(color);
        pixelsG = Color.green(color);
        pixelsB = Color.blue(color);

        //转换
        pixelsR = (pixelsR - lightness);
        pixelsG = (pixelsG - lightness);
        pixelsB = (pixelsB - lightness);
        //均小于等于255大于等于0
        if (pixelsR > 255) {
            pixelsR = 255;
        } else if (pixelsR < 0) {
            pixelsR = 0;
        }
        if (pixelsG > 255) {
            pixelsG = 255;
        } else if (pixelsG < 0) {
            pixelsG = 0;
        }
        if (pixelsB > 255) {
            pixelsB = 255;
        } else if (pixelsB < 0) {
            pixelsB = 0;
        }
        //根据新的RGB生成新像素
        return Color.argb(pixelsA, pixelsR, pixelsG, pixelsB);
    }

}
