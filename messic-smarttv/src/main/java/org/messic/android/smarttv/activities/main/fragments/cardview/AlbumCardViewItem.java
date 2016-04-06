package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;
import org.messic.android.smarttv.utils.Utils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AlbumCardViewItem implements CardViewItem {

    private MDMAlbum album;

    public AlbumCardViewItem(MDMAlbum album) {
        this.album = album;
    }

    @Override
    public Object getItem() {
        return album;
    }

    @Override
    public void onBindViewHolder(Configuration config, Context context, CardViewHolder viewHolder) {
        String coverOnlineURL =
                config.getBaseUrl() + "/services/albums/" + album.getSid()
                        + "/cover?preferredWidth=" + Utils.convertDpToPixel(context, DEFAULT_CARD_WIDTH) +
                        "&preferredHeight=" + Utils.convertDpToPixel(context, DEFAULT_CARD_HEIGHT) + "&messic_token="
                        + config.getLastToken();


        viewHolder.getImageCardView().setTitleText(album.getName());
        viewHolder.getImageCardView().setContentText(album.getAuthor().getName());
        viewHolder.getImageCardView().setMainImageDimensions(DEFAULT_CARD_WIDTH, DEFAULT_CARD_HEIGHT);
        viewHolder.getImageCardView().setBadgeImage(context.getResources().getDrawable(R.drawable.ic_delete_white_48dp));
        Picasso.with(MessicCoreApp.getInstance()).load(coverOnlineURL).into(viewHolder.getImageCardView().getMainImageView());

    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row, UtilMusicPlayer ump, Configuration config, MainFragmentPresenter presenter, final Context context) {
        final MDMAlbum album = ((AlbumCardViewItem) oitem).getAlbum();

        //we get the row and the adapter of the row
        ListRow lrow = (ListRow) row;
        final ArrayObjectAdapter aoa = (ArrayObjectAdapter) lrow.getAdapter();
        //we try to find the position at the row of the album
        int pos = 0;
        for (pos = 0; pos < aoa.size(); pos++) {
            Object obj = aoa.get(pos);
            if (obj instanceof AlbumCardViewItem) {
                MDMAlbum posalbum = ((AlbumCardViewItem) obj).getAlbum();
                if (posalbum.getSid() == album.getSid()) {
                    break;
                }
            }
        }


        //finally, we add the songs instead
        final int posFound = pos;
        Observable<MDMSong> observable = presenter.loadAlbum(album);
        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMSong>() {
            int posFoundi = posFound + 1;

            @Override
            public void call(MDMSong song) {
                song.setAlbum(album);
                aoa.add(posFoundi, new SongCardViewItem(song));
                posFoundi++;
                aoa.notifyArrayItemRangeChanged(posFoundi, 1);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                //mProgressBar.setVisibility(View.GONE);
                //swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show();
            }
        }, new Action0() {
            @Override
            public void call() {
                //we remove the album
                aoa.removeItems(posFound, 1);
                aoa.notifyArrayItemRangeChanged(posFound, 1);
            }
        });

    }

    public MDMAlbum getAlbum() {
        return this.album;
    }
}
