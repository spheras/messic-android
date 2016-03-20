package org.messic.android.messiccore.util;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class UtilImage {

    /**
     * Resize a bitmap to the notification status bar size dimensions
     *
     * @param context {@link Context} utility
     * @param bm      {@link Bitmap} to resize
     * @return resized bitmap
     */
    public static Bitmap resizeToNotificationImageSize(Context context, Bitmap bm, int notification_cover_width, int notification_cover_height) {
        Resources res = context.getResources();
        int height = (int) res.getDimension(notification_cover_height);
        int width = (int) res.getDimension(notification_cover_width);
        return Bitmap.createScaledBitmap(bm, width, height, false);
    }

    /**
     * http://developer.android.com/intl/es/training/displaying-bitmaps/load-bitmap.html
     * To tell the decoder to subsample the image, loading a smaller version into memory, set inSampleSize to true in your BitmapFactory.Options object. For example, an image with resolution 2048x1536 that is decoded with an inSampleSize of 4 produces a bitmap of approximately 512x384. Loading this into memory uses 0.75MB rather than 12MB for the full image (assuming a bitmap configuration of ARGB_8888). Here’s a method to calculate a sample size value that is a power of two based on a target width and height:
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
