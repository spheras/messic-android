/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.messic.android.messic_tv.activities.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.messic_tv.R;
import org.messic.android.messic_tv.util.Utils;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.net.URI;
import java.net.URISyntaxException;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class SongCardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    protected static Context mContext;

    //size in dps
    private static int CARD_WIDTH = 176;
    private static int CARD_HEIGHT = 176;

    static class ViewHolder extends Presenter.ViewHolder {
        private MDMSong mSong;
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.unknowncover, null);
        }

        public void setSong(MDMSong song) {
            mSong = song;
            mImageCardViewTarget.setSong(song);
        }

        public MDMSong getSong() {
            return mSong;
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.fastlane_background));
        return new ViewHolder(cardView);
    }


    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        MDMSong song = (MDMSong) item;
        ((ViewHolder) viewHolder).setSong(song);

        Log.d(TAG, "onBindViewHolder");
        //TODO offline mode?
        String coverOnlineURL =
                Configuration.getBaseUrl(mContext) + "/services/albums/" + song.getAlbum().getSid()
                        + "/cover?preferredWidth=" + Utils.convertDpToPixel(mContext, CARD_WIDTH) + "&preferredHeight=" + Utils.convertDpToPixel(mContext, CARD_HEIGHT) + "&messic_token="
                        + Configuration.getLastToken();

        //if (song.getCardImageUrl() != null) {
        ((ViewHolder) viewHolder).mCardView.setTitleText(song.getName());
        ((ViewHolder) viewHolder).mCardView.setContentText(song.getAlbum().getName());
        ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);


        try {
            ((ViewHolder) viewHolder).updateCardViewImage(new URI(coverOnlineURL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //}
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }

    public static class PicassoImageCardViewTarget implements Target {
        private ImageCardView mImageCardView;
        private MDMSong mSong;

        public void setSong(MDMSong song) {
            mSong = song;
        }

        public PicassoImageCardViewTarget(ImageCardView imageCardView) {
            mImageCardView = imageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            if (mSong != null && mSong instanceof MDMQueueSong) {
                int cursor = UtilMusicPlayer.getCursor(mContext);
                MDMQueueSong mqs = (MDMQueueSong) mSong;
                if (cursor == mqs.indexAtList) {


                    Bitmap bm_ic_pause = BitmapFactory.decodeResource(mContext.getResources(), (UtilMusicPlayer.isPlaying(mContext) ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp));

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
}
