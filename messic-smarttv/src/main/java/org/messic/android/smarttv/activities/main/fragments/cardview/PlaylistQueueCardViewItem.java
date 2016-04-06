package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;

import javax.inject.Inject;

public class PlaylistQueueCardViewItem extends SongCardViewItem implements Target {
    @Inject
    UtilMusicPlayer ump;
    private int indexAtList;
    private Context context;
    private CardViewHolder holder;


    public PlaylistQueueCardViewItem(MDMSong song) {
        super(song);
        MessicSmarttvApp.getSmarttvApp().getSmarttvComponent().inject(this);
    }

    public int getIndexAtList() {
        return indexAtList;
    }

    public void setIndexAtList(int indexAtList) {
        this.indexAtList = indexAtList;
    }

    protected void loadCover(String coverOnlineURL, CardViewHolder viewHolder, Context context) {
        this.context = context;
        this.holder = viewHolder;
        this.holder.getImageCardView().invalidate();
        if (this.holder.getImageCardView() != null) {
            if (this.holder.getImageCardView().getMainImage() != null) {
                this.holder.getImageCardView().getMainImage().invalidateSelf();
            }
            if (this.holder.getImageCardView().getMainImageView() != null) {
                this.holder.getImageCardView().getMainImageView().invalidate();
            }
            this.holder.getImageCardView().setMainImage(context.getResources().getDrawable(R.drawable.unknowncover), true);
        }

        Picasso.with(context)
                .load(coverOnlineURL)
//                .resize(Utils.convertDpToPixel(context, DEFAULT_CARD_WIDTH),
//                        Utils.convertDpToPixel(context, DEFAULT_CARD_HEIGHT))
//                .error(viewHolder.getDefaultCardImage())
                .into(this);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row, UtilMusicPlayer ump, Configuration config, MainFragmentPresenter presenter, Context context) {
        PlaylistQueueCardViewItem pqcardview = (PlaylistQueueCardViewItem) oitem;
        if (pqcardview.getSong().getAlbum() != null) {
            if (ump.getCursor() == pqcardview.getIndexAtList()) {
                if (ump.isPlaying()) {
                    ump.pauseSong();
                } else {
                    ump.resumeSong();
                }
            } else {
                ump.setSong(pqcardview.getIndexAtList());
                ump.playSong();
            }
        } else {
            //the clear action item?
            ump.clearQueue();
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        int cursor = ump.getCursor();
        if (cursor == getIndexAtList()) {


            Bitmap bm_ic_pause = BitmapFactory.decodeResource(context.getResources(),
                    (ump.isPlaying() ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp));

            Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
            Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas c = new Canvas(mutableBitmap);
            int l = bitmap.getWidth() / 2 - bitmap.getWidth() / 4;
            int t = bitmap.getHeight() / 2 - bitmap.getHeight() / 4;

            Rect src = new Rect(0, 0, bm_ic_pause.getWidth(), bm_ic_pause.getHeight());
            Rect dst = new Rect(l, t, l + bitmap.getWidth() / 2, t + bitmap.getHeight() / 2);
            c.drawBitmap(bm_ic_pause, src, dst, new Paint());


            Drawable bitmapDrawable = new BitmapDrawable(context.getResources(), mutableBitmap);
            this.holder.getImageCardView().invalidate();
            this.holder.getImageCardView().setMainImage(bitmapDrawable, true);
        } else {
            Drawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
            this.holder.getImageCardView().invalidate();
            this.holder.getImageCardView().setMainImage(bitmapDrawable, true);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
