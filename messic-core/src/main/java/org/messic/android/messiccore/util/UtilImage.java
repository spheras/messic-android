package org.messic.android.messiccore.util;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

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
}
