package org.messic.android.smarttv.activities.main.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;

import javax.inject.Inject;

public class PicassoImageCardViewTarget implements Target {
    @Inject
    UtilMusicPlayer ump;
    private ImageCardView mImageCardView;
    private MDMSong mSong;
    private Context mContext;

    public PicassoImageCardViewTarget(ImageCardView imageCardView, Context context) {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
        mImageCardView = imageCardView;
        this.mContext = context;
    }

    public void setSong(MDMSong song) {
        mSong = song;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        if (mSong != null && mSong instanceof MDMQueueSong) {
            int cursor = ump.getCursor();
            MDMQueueSong mqs = (MDMQueueSong) mSong;
            if (cursor == mqs.indexAtList) {


                Bitmap bm_ic_pause = BitmapFactory.decodeResource(mContext.getResources(), (ump.isPlaying() ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp));

                Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
                Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas c = new Canvas(mutableBitmap);
                int l = bitmap.getWidth() / 2 - bitmap.getWidth() / 4;
                int t = bitmap.getHeight() / 2 - bitmap.getHeight() / 4;

                Rect src = new Rect(0, 0, bm_ic_pause.getWidth(), bm_ic_pause.getHeight());
                Rect dst = new Rect(l, t, l + bitmap.getWidth() / 2, t + bitmap.getHeight() / 2);
                c.drawBitmap(bm_ic_pause, src, dst, new Paint());


                Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), mutableBitmap);
                mImageCardView.setMainImage(bitmapDrawable);
            } else {
                Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
                mImageCardView.setMainImage(bitmapDrawable);
            }
        } else {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        mImageCardView.setMainImage(drawable);
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {
        // Do nothing, default_background manager has its own transitions
    }
}