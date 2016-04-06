package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import com.squareup.picasso.Picasso;

import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;
import org.messic.android.smarttv.utils.Utils;

public class SongCardViewItem implements CardViewItem {

    private MDMSong song;

    public SongCardViewItem(MDMSong song) {
        this.song = song;
    }

    @Override
    public Object getItem() {
        return song;
    }

    @Override
    public void onBindViewHolder(Configuration config, Context context, CardViewHolder viewHolder) {
        String coverOnlineURL =
                config.getBaseUrl() + "/services/albums/" + song.getAlbum().getSid()
                        + "/cover?preferredWidth=" + Utils.convertDpToPixel(context, DEFAULT_CARD_WIDTH) +
                        "&preferredHeight=" + Utils.convertDpToPixel(context, DEFAULT_CARD_HEIGHT) + "&messic_token="
                        + config.getLastToken();


        viewHolder.getImageCardView().setTitleText(song.getName());
        viewHolder.getImageCardView().setContentText(song.getAlbum().getName());
        viewHolder.getImageCardView().setMainImageDimensions(DEFAULT_CARD_WIDTH, DEFAULT_CARD_HEIGHT);
        viewHolder.getImageCardView().setBadgeImage(context.getResources().getDrawable(R.mipmap.ic_launcher));

        loadCover(coverOnlineURL, viewHolder, context);
    }

    protected void loadCover(String coverOnlineURL, CardViewHolder viewHolder, Context context) {
        Picasso.with(MessicCoreApp.getInstance()).load(coverOnlineURL).into(viewHolder.getImageCardView().getMainImageView());
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row, UtilMusicPlayer ump, Configuration config, MainFragmentPresenter presenter, Context context) {
        ump.addSong(((SongCardViewItem) oitem).getSong());
    }

    public MDMSong getSong() {
        return this.song;
    }
}
