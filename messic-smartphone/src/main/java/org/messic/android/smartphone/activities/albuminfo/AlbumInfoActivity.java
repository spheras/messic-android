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
package org.messic.android.smartphone.activities.albuminfo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.messic.android.R;
import org.messic.android.databinding.ActivityAlbuminfoBinding;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.smartphone.activities.MessicBaseActivity;
import org.messic.android.smartphone.activities.searchmessicservice.SearchMessicServiceActivity;
import org.messic.android.smartphone.rxevents.RxAction;
import org.messic.android.smartphone.rxevents.RxDispatcher;

import java.util.Locale;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AlbumInfoActivity extends MessicBaseActivity implements AlbumInfoSongViewHolder.IViewHolderClicks, PlayerEventListener {
    public static final String EXTRA_ALBUM_SID = "ALBUM_SID";
    //@TODO if we pass this by parcel it causes error due to the size
    public static Bitmap defaultArt;
    @Inject
    AlbumInfoPresenter presenter;
    @Inject
    Configuration config;
    @Inject
    UtilDownloadService uds;
    private ActivityAlbuminfoBinding binding;
    private Subscription subscription;
    private MDMAlbum album;

    private ImageView ivArt;
    private TextView tvAuthor;
    private TextView tvAlbum;
    private ImageButton bPlay;
    private RecyclerView rvList;
    private AlbumInfoSongAdapter mAdapter;
    private ScrollView mScroll;
    private ImageButton mDownload;
    private ImageButton ibRemove;

    @Override
    protected void onStart() {
        super.onStart();
        // put the layout considering the situation
        setupLayout();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.presenter.resume();

        if (this.subscription != null)
            RxDispatcher.get().unsubscribe(this.subscription);

        this.subscription = subscribe();

        this.mScroll.smoothScrollTo(0, 0);

    }

    @Override
    public void onPause() {
        super.onPause();
        this.presenter.pause();
        RxDispatcher.get().unsubscribe(this.subscription);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) getApplication()).getSmartphoneComponent().inject(this);


        startServices();
        bindData(savedInstanceState);
        //setupLayout(); this time we will do it at onStart
        setupToolbar(false);
        this.presenter.initialize();
        setupWindowAnimations();
    }

    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
            public void call(RxAction event) {
//                if (event.isType(LoginEvents.EVENT_FINISH_ACTIVITY)) {
//                    AlbumInfoActivity.this.finish();
//                }
            }
        });
    }

    private void bindData(Bundle savedInstanceState) {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_albuminfo);
        this.binding.setEvents(this);

        this.album = (MDMAlbum) getIntent().getExtras().get(EXTRA_ALBUM_SID);

        this.ivArt = (ImageView) findViewById(R.id.albuminfo_art);
        this.tvAuthor = (TextView) findViewById(R.id.albuminfo_author);
        this.tvAlbum = (TextView) findViewById(R.id.albuminfo_album);
        this.ibRemove = (ImageButton) findViewById(R.id.albuminfo_remove);
        this.bPlay = (ImageButton) findViewById(R.id.albuminfo_play);
        this.rvList = (RecyclerView) findViewById(R.id.albuminfo_recyclerview);
        this.mAdapter = new AlbumInfoSongAdapter();
        this.mAdapter.setListener(this);
        this.mDownload = (ImageButton) findViewById(R.id.albuminfo_download);
        this.mAdapter.addSongs(this.album.getSongs());
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(this.mAdapter);
        this.mScroll = (ScrollView) findViewById(R.id.albuminfo_scroll);
        final View titleContainer = findViewById(R.id.albuminfo_title_container);
        this.mScroll.scrollTo(0, 0);

        this.tvAuthor.setText(this.album.getAuthor().getName());
        this.tvAlbum.setText(this.album.getName());

        Bitmap bmCover = album.getOfflineCover(config);
        if (bmCover == null) {
            if (!config.isOffline()) {
                String baseURL =
                        config.getBaseUrl() + "/services/albums/" + album.getSid()
                                + "/cover?preferredWidth=500&preferredHeight=500&messic_token="
                                + config.getLastToken();

                RequestCreator rc = Picasso.with(this).load(baseURL);
                if (defaultArt != null) {
                    this.ivArt.setImageBitmap(defaultArt);
                    rc = rc.placeholder(new BitmapDrawable(defaultArt));

                }
                rc.into(this.ivArt);
            }
        } else {
            this.ivArt.setImageBitmap(bmCover);
        }
        if (defaultArt != null) {
            Palette.PaletteAsyncListener paletteListener = new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    titleContainer.setBackgroundColor(palette.getDarkMutedColor(getResources().getColor(R.color.activity_albuminfo_title)));
                }
            };
            Palette.from(defaultArt).generate(paletteListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putParcelable(BINDING_PARCEL, Parcels.wrap(this.binding.getUser()));
    }

    /**
     * setting up the layout of the activity.
     * Here you must put elements, remove elements, manage events, ...
     */
    private void setupLayout() {
        if (config.isOffline() || album.isFlagFromLocalDatabase()) {
            this.ibRemove.setVisibility(View.VISIBLE);
            this.mDownload.setVisibility(View.GONE);
        } else {
            this.ibRemove.setVisibility(View.GONE);
            this.mDownload.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Setting up the animations for this activty, mainly enter and exit transitions
     */

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
//            getWindow().setEnterTransition(transition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_login_action_scan) {
            Intent ssa = new Intent(AlbumInfoActivity.this, SearchMessicServiceActivity.class);
            AlbumInfoActivity.this.startActivity(ssa);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlayAction(View caller, AlbumInfoSongViewHolder holder) {
        this.presenter.playAction(holder.song);
        Toast.makeText(this, getResources().getText(R.string.player_added) + holder.song.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongPlayAction(View caller, AlbumInfoSongViewHolder holder) {
        this.presenter.longPlayAction(holder.song);
        Toast.makeText(this, getResources().getText(R.string.player_added) + holder.song.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemove(View caller, final AlbumInfoSongViewHolder holder) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:

                        Subscription _subscription = presenter.removeAction(holder.song)//
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Boolean>() {
                                    @Override
                                    public void onCompleted() {
                                        mAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Toast.makeText(AlbumInfoActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Timber.e(e.getMessage(), e);
                                        e.printStackTrace();
                                        mAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        //nothing
                                    }
                                });

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumInfoActivity.this);
        builder.setMessage(getString(R.string.action_remove_local_song));
        builder.setPositiveButton(getString(R.string.yes), dialogClickListener);
        builder.setNegativeButton(getString(R.string.no), dialogClickListener);
        builder.show();
    }

    @Override
    public void onDownload(View caller, final AlbumInfoSongViewHolder holder) {

        Observable<Float> observable = this.presenter.downloadAction(holder.song);
        Subscription _subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Float>() {
                               @Override
                               public void onCompleted() {
                                   AlbumInfoActivity.this.mAdapter.notifyDataSetChanged();
                                   Toast.makeText(AlbumInfoActivity.this, AlbumInfoActivity.this.getResources().getText(R.string.download_added) + holder.song.getName(),
                                           Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   Toast.makeText(AlbumInfoActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                   Timber.e(e.getMessage(), e);
                                   e.printStackTrace();
                                   mAdapter.notifyDataSetChanged();
                               }

                               @Override
                               public void onNext(Float aFloat) {
                                   holder.tvSongName.setText(holder.song.getName() + "(" + aFloat + ")");
                                   //AlbumInfoActivity.this.mAdapter.notifyDataSetChanged();
                               }
                           }
                );


    }

    public void onRemoveAlbumButton(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        final ProgressDialog pd =
                                ProgressDialog.show(AlbumInfoActivity.this, AlbumInfoActivity.this.getString(R.string.action_removing_local_album_title),
                                        AlbumInfoActivity.this.getString(R.string.action_removing_local_album_content), true);

                        presenter.removeAlbum(album).subscribe(new Action1<MDMSong>() {
                            @Override
                            public void call(MDMSong mdmSong) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Toast.makeText(AlbumInfoActivity.this, "Error:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                Timber.e(throwable.getMessage(), throwable);
                                throwable.printStackTrace();
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                pd.dismiss();
                                Toast.makeText(AlbumInfoActivity.this, AlbumInfoActivity.this.getString(R.string.action_local_album_removed) + album.getName(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        break;
                }
            }
        };
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AlbumInfoActivity.this);
        builder.setMessage(AlbumInfoActivity.this.getString(R.string.action_remove_local_album));
        builder.setPositiveButton(AlbumInfoActivity.this.getString(R.string.yes), dialogClickListener);
        builder.setNegativeButton(AlbumInfoActivity.this.getString(R.string.no), dialogClickListener);
        builder.show();
    }

    public void onPlayButton(View v) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewAnimationUtils.createCircularReveal(v, v.getWidth() / 2, v.getHeight() / 2, 0, v.getHeight() / 2).start();
        }
        this.presenter.playAction(this.album);
        Toast.makeText(this, getResources().getText(R.string.player_added) + album.getName(),
                Toast.LENGTH_SHORT).show();
    }


    public boolean onLongPlayButton(View v) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewAnimationUtils.createCircularReveal(v, v.getWidth() / 2, v.getHeight() / 2, 0, v.getHeight() / 2).start();
        }
        this.presenter.longPlayAction(this.album);
        Toast.makeText(this, getResources().getText(R.string.player_added) + album.getName(),
                Toast.LENGTH_SHORT).show();

        return false;
    }

    public void onBackButton(View v) {
        finish();
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
//        this.startActivity(intent);
    }

    public void onShareAlbumButton(View v) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.share_album_text_start) + " \"" + album.getName() + "\" " + getString(R.string.share_album_text_of) + " \"" + album.getAuthor().getName() + "\" (I <3 messic)";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_by)));
    }

    public void onWikipediaButton(View v) {
        String surl =
                "https://" + Locale.getDefault().getLanguage().toLowerCase() + ".wikipedia.org/wiki/"
                        + album.getAuthor().getName();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(surl));
        startActivity(browserIntent);
    }

    public void onDownloadAlbumButton(View v) {
        Observable<MDMSong> observable = this.presenter.downloadAction(album);

        Toast.makeText(AlbumInfoActivity.this, getResources().getText(R.string.download_added) + album.getName(),
                Toast.LENGTH_SHORT).show();

        Subscription _subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MDMSong>() {
                               @Override
                               public void onCompleted() {
                                   AlbumInfoActivity.this.mAdapter.notifyDataSetChanged();
                                   Toast.makeText(AlbumInfoActivity.this, getResources().getText(R.string.download_added) + album.getName(),
                                           Toast.LENGTH_SHORT).show();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   Toast.makeText(AlbumInfoActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                   Timber.e(e.getMessage(), e);
                                   e.printStackTrace();
                                   mAdapter.notifyDataSetChanged();
                               }

                               @Override
                               public void onNext(MDMSong aSong) {
                                   AlbumInfoActivity.this.mAdapter.notifyDataSetChanged();
                               }
                           }
                );


    }

    @Override
    public void paused(MDMSong song, int index) {
        this.mAdapter.notifyDataSetChanged();
    }

    @Override
    public void playing(MDMSong song, boolean resumed, int index) {
        this.mAdapter.notifyDataSetChanged();
    }

    @Override
    public void completed(int index) {
        this.mAdapter.notifyDataSetChanged();
    }

    @Override
    public void added(MDMSong song) {

    }

    @Override
    public void added(MDMAlbum album) {

    }

    @Override
    public void added(MDMPlaylist playlist) {

    }

    @Override
    public void removed(MDMSong song) {

    }

    @Override
    public void empty() {

    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }
}
