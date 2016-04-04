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

package org.messic.android.smarttv.activities.main.fragments;

import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.utils.Utils;

import javax.inject.Inject;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class SongCardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";
    protected static Context mContext;
    //size in dps
    @Inject
    Configuration config;
    @Inject
    UtilMusicPlayer ump;

    public SongCardPresenter() {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setBackgroundColor(mContext.getResources().getColor(R.color.fastlane_background));
        return new SongCardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        SongCardViewHolder scviewHolder = ((SongCardViewHolder) viewHolder);

        if (item instanceof MDMSong) {
            MDMSong song = (MDMSong) item;
            scviewHolder.setSong(song);

            Log.d(TAG, "onBindViewHolder");

            if (song instanceof MDMQueueSong && song.getAlbum() == null) {
                //then it is the empty list action?
                scviewHolder.mCardView.setTitleText("Clear List");
                scviewHolder.mCardView.setContentText("Clear the Queue PlayList");
                scviewHolder.mCardView.setMainImageDimensions(SongCardViewHolder.CARD_WIDTH, SongCardViewHolder.CARD_HEIGHT);
                scviewHolder.mCardView.setMainImage(mContext.getResources().getDrawable(R.drawable.ic_delete_white_48dp));
                scviewHolder.mCardView.setBadgeImage(mContext.getResources().getDrawable(R.drawable.ic_delete_white_48dp));

            } else {
                String coverOnlineURL =
                        config.getBaseUrl() + "/services/albums/" + song.getAlbum().getSid()
                                + "/cover?preferredWidth=" + Utils.convertDpToPixel(mContext, SongCardViewHolder.CARD_WIDTH) +
                                "&preferredHeight=" + Utils.convertDpToPixel(mContext, SongCardViewHolder.CARD_HEIGHT) + "&messic_token="
                                + config.getLastToken();


                scviewHolder.mCardView.setTitleText(song.getName());
                scviewHolder.mCardView.setContentText(song.getAlbum().getName());
                scviewHolder.mCardView.setMainImageDimensions(SongCardViewHolder.CARD_WIDTH, SongCardViewHolder.CARD_HEIGHT);
                scviewHolder.mCardView.setBadgeImage(mContext.getResources().getDrawable(R.mipmap.ic_launcher));


                Picasso.with(MessicCoreApp.getInstance()).load(coverOnlineURL).into(scviewHolder.mCardView.getMainImageView());

//                try {
//                    scviewHolder.updateCardViewImage(new URI(coverOnlineURL));
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
            }
        } else if (item instanceof MDMAlbum) {
            MDMAlbum album = (MDMAlbum) item;
            String coverOnlineURL =
                    config.getBaseUrl() + "/services/albums/" + album.getSid()
                            + "/cover?preferredWidth=" + Utils.convertDpToPixel(mContext, SongCardViewHolder.CARD_WIDTH) + "&preferredHeight=" + Utils.convertDpToPixel(mContext, SongCardViewHolder.CARD_HEIGHT) + "&messic_token="
                            + config.getLastToken();


            scviewHolder.mCardView.setTitleText(album.getName());
            scviewHolder.mCardView.setContentText(album.getAuthor().getName());
            scviewHolder.mCardView.setMainImageDimensions(SongCardViewHolder.CARD_WIDTH, SongCardViewHolder.CARD_HEIGHT);
            Picasso.with(MessicCoreApp.getInstance()).load(coverOnlineURL).into(scviewHolder.mCardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }


}
