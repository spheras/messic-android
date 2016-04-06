package org.messic.android.smarttv.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;

import java.io.IOException;

/**
 * Created by Fco Javier Coira on 01/09/2015.
 */
public class PicassoMessicUtil {

    public static String getCoverURL(Configuration config, Context aContext, MDMSong song) {
        return
                config.getBaseUrl() + "/services/albums/" + song.getAlbum().getSid()
                        + "/cover?preferredWidth=" + Utils.convertDpToPixel(aContext, 256) + "&preferredHeight=" + Utils.convertDpToPixel(aContext, 256) + "&messic_token="
                        + config.getLastToken();
    }

    public static Bitmap getCover(Configuration config, Context aContext, MDMSong song) throws IOException {
        String coverURL = getCoverURL(config, aContext, song);
        return Picasso.with(aContext).load(coverURL).error(aContext.getResources().getDrawable(R.drawable.unknowncover, null)).get();
    }

    public static void loadCover(Configuration config, Context aContext, MDMSong song, Target coverTarget) {
        String coverURL = getCoverURL(config, aContext, song);
        Picasso.with(aContext).load(coverURL).error(aContext.getResources().getDrawable(R.drawable.unknowncover, null)).into(coverTarget);
    }
}
