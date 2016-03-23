/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.smartphone.views.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.AlbumCoverCache;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PlayerView
        extends RelativeLayout
        implements PlayerEventListener {

    private static final Integer STATUS_PLAY = 1;
    private static final Integer STATUS_PAUSE = 2;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    Configuration config;
    @Inject
    PlayerPresenter presenter;
    @Inject
    AlbumCoverCache cache;
    private ImageView ivprevsong;
    private ImageView ivnextsong;
    private ImageView ivplaypause;
    private ImageView vcover;
    private TextView vtvauthor;
    private TextView vtvsong;

    public PlayerView(Context context) {
        super(context);
        init();
    }

    public PlayerView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public PlayerView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            //inject dependencies
            ((MessicSmartphoneApp) MessicSmartphoneApp.getInstance()).getSmartphoneComponent().inject(this);
            bindData();
            setupLayout();
            ump.addListener(this);
            update();
        }

    }

    private void bindData() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View vThis = inflater.inflate(R.layout.view_player, this, true);

        vcover = (ImageView) findViewById(R.id.player_ivcurrent_cover);
        vtvauthor = (TextView) findViewById(R.id.player_tvcurrent_author);
        vtvsong = (TextView) findViewById(R.id.player_tvcurrent_song);

        ivprevsong = (ImageView) vThis.findViewById(R.id.player_ivback);
        ivnextsong = (ImageView) vThis.findViewById(R.id.player_ivnext);
        ivplaypause = (ImageView) vThis.findViewById(R.id.player_ivplaypause);

        ivprevsong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ump.prevSong();
            }

        });
        ivnextsong.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ump.nextSong();
            }

        });
        ivplaypause.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (ivplaypause.getTag().equals(STATUS_PLAY)) {
                    ump.resumeSong();
                } else {
                    ump.pauseSong();
                }
            }
        });
        View.OnClickListener vlistener = new View.OnClickListener() {
            public void onClick(View v) {
                MDMSong song = ump.getCurrentSong();
                Observable<MDMAlbum> observable = presenter.getAlbum(song);

                observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMAlbum>() {
                    @Override
                    public void call(MDMAlbum mdmAlbum) {

                        //lets show the albuminfoactivity
                        Intent ssa = new Intent(getContext(), AlbumInfoActivity.class);
                        ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, mdmAlbum);
                        Bitmap bitmap = ((BitmapDrawable) vcover.getDrawable()).getBitmap();
                        AlbumInfoActivity.defaultArt = bitmap;

                        //ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_ART, bitmap);

                        android.app.Activity host = (android.app.Activity) getContext();
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(host, vcover, "cover");
                        getContext().startActivity(ssa, options.toBundle());

                    }
                });
            }
        };
        vcover.setOnClickListener(vlistener);
        vtvauthor.setOnClickListener(vlistener);
        vtvsong.setOnClickListener(vlistener);
    }

    private void setupLayout() {
        fillData(null);
    }

    private void update() {
        MDMSong song = ump.getCurrentSong();
        if (song != null && ump.isPlaying()) {
            playing(song, false, 0);
        } else {
            if (song != null) {
                playing(song, false, 0);
                paused(song, 0);
                post(new Runnable() {
                    public void run() {
                        setVisibility(View.VISIBLE);
                        invalidate();
                    }
                });
            } else {
                post(new Runnable() {
                    public void run() {
                        setVisibility(View.GONE);
                        invalidate();
                    }
                });
            }
        }
    }

    private void fillData(MDMSong song) {
        String authorname = (song != null ? song.getAlbum().getAuthor().getName() : "");
        String songname = (song != null ? song.getName() : "");

        vtvauthor.setText(authorname);
        vtvsong.setText(songname);

        if (song != null) {
            Bitmap bmcover = song.getAlbum().getOfflineCover(config);
            if (bmcover == null) {
                if (!config.isOffline()) {
                    String baseURL =
                            config.getBaseUrl() + "/services/albums/" + song.getAlbum().getSid()
                                    + "/cover?preferredWidth=100&preferredHeight=100&messic_token="
                                    + config.getLastToken();
                    Picasso.with(MessicCoreApp.getInstance()).load(baseURL).into(vcover);
                }
            } else {
                vcover.setImageBitmap(bmcover);
            }
        } else {

        }
    }

    public void paused(MDMSong song, int index) {
        post(new Runnable() {
            public void run() {
                setVisibility(View.VISIBLE);
                invalidate();
            }
        });
        ivplaypause.setTag(STATUS_PLAY);
        ivplaypause.setBackgroundResource(R.drawable.ic_play_arrow_white_36dp);
        ivplaypause.invalidate();
    }

    public void playing(MDMSong song, boolean resumed, int index) {
        post(new Runnable() {
            public void run() {
                setVisibility(View.VISIBLE);
                invalidate();
            }
        });
        ivplaypause.setTag(STATUS_PAUSE);
        ivplaypause.setBackgroundResource(R.drawable.ic_pause_white_36dp);
        ivplaypause.invalidate();
        if (!resumed) {
            fillData(song);
        }
    }

    public void completed(int index) {
        this.setVisibility(View.VISIBLE);
        ivplaypause.setTag(STATUS_PLAY);
        ivplaypause.setBackgroundResource(R.drawable.ic_play_arrow_white_36dp);
        ivplaypause.invalidate();
    }

    public void added(MDMSong song) {
        // nothing to do
    }

    public void added(MDMAlbum album) {
        // nothing to do
    }

    public void added(MDMPlaylist playlist) {
        // nothing to do
    }

    public void disconnected() {
        // nothing to do
    }

    public void connected() {
        // nothing to do
    }

    public void removed(MDMSong song) {
        MDMSong newsong = ump.getCurrentSong();
        if (newsong != null) {
            post(new Runnable() {
                public void run() {
                    setVisibility(View.VISIBLE);
                    invalidate();
                }
            });
        } else {
            post(new Runnable() {
                public void run() {
                    setVisibility(View.GONE);
                    invalidate();
                }
            });
        }
    }

    public void empty() {
        removed(null);
    }

}
