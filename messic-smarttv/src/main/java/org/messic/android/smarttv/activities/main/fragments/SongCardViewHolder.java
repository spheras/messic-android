package org.messic.android.smarttv.activities.main.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;

import com.squareup.picasso.Picasso;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.smarttv.utils.Utils;

import java.net.URI;

public class SongCardViewHolder extends Presenter.ViewHolder {
    public static final int CARD_WIDTH = 176;
    public static final int CARD_HEIGHT = 176;
    public MDMSong mSong;
    public ImageCardView mCardView;
    public Drawable mDefaultCardImage;
    public PicassoImageCardViewTarget mImageCardViewTarget;
    private Context mContext;


    public SongCardViewHolder(View view) {
        super(view);
        mCardView = (ImageCardView) view;
        mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView, view.getContext());
        this.mContext = view.getContext();
        mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.unknowncover);
    }

    public MDMSong getSong() {
        return mSong;
    }

    public void setSong(MDMSong song) {
        mSong = song;
        mImageCardViewTarget.setSong(song);
    }

    public ImageCardView getCardView() {
        return mCardView;
    }

    protected void updateCardViewImage(URI uri) {
        Picasso.with(mContext)
                .load(uri.toString())
                .resize(Utils.convertDpToPixel(mContext, CARD_WIDTH),
                        Utils.convertDpToPixel(mContext, CARD_HEIGHT))
                .error(mDefaultCardImage).into(mImageCardViewTarget);
    }
}
