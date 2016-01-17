package org.messic.android.messic_tv.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.messic_tv.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;

import java.io.IOException;

/**
 * Created by Fco Javier Coira on 01/09/2015.
 */
public class PicassoMessicUtil {

    public static String getCoverURL(Context aContext, MDMSong song) {
        return
                Configuration.getBaseUrl(aContext) + "/services/albums/" + song.getAlbum().getSid()
                        + "/cover?preferredWidth=" + Utils.convertDpToPixel(aContext, 256) + "&preferredHeight=" + Utils.convertDpToPixel(aContext, 256) + "&messic_token="
                        + Configuration.getLastToken(aContext);
    }

    public static Bitmap getCover(Context aContext, MDMSong song) throws IOException {
        String coverURL = getCoverURL(aContext, song);
        return Picasso.with(aContext).load(coverURL).error(aContext.getResources().getDrawable(R.drawable.unknowncover, null)).get();
    }

    public static void loadCover(Context aContext, MDMSong song, Target coverTarget) {
        String coverURL = getCoverURL(aContext, song);
        Picasso.with(aContext).load(coverURL).error(aContext.getResources().getDrawable(R.drawable.unknowncover, null)).into(coverTarget);
    }
}
