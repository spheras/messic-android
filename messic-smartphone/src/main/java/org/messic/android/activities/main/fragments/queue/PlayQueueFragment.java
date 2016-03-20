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
package org.messic.android.activities.main.fragments.queue;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.databinding.FragmentPlayqueueBinding;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.UtilFile;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PlayQueueFragment extends Fragment implements PlayQueueSongViewHolder.IViewHolderClicks, PlayerEventListener {


    @Inject
    PlayQueuePresenter presenter;
    @Inject
    PlayQueueSongAdapter mAdapter;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    Configuration config;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private Action1<MDMSong> playqueueSongsOnNext = new Action1<MDMSong>() {
        @Override
        public void call(MDMSong song) {
            mAdapter.addSong(song);
        }
    };
    private RecyclerView mRecycler;
    private ProgressBar mProgressBar;
    private Action1<Throwable> playqueueSongsOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };
    private Action0 playqueueSongsOnCompleted = new Action0() {
        @Override
        public void call() {
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    };
    private FragmentPlayqueueBinding binding;

    @Override
    public void onStart() {
        super.onStart();

        if (this.mAdapter.getItemCount() == 0) {
            updateSongs();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateSongs() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAdapter.clear();
                    Observable<MDMSong> observable = presenter.getQueueSongs();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(playqueueSongsOnNext, playqueueSongsOnError, playqueueSongsOnCompleted);
                }
            });

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        this.presenter.resume();

        if (this.subscription != null)
            RxDispatcher.get().unsubscribe(this.subscription);

        this.subscription = subscribe();

    }

    @Override
    public void onPause() {
        super.onPause();
        this.presenter.pause();
        RxDispatcher.get().unsubscribe(this.subscription);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);

        View view = bindData(inflater, container, savedInstanceState);
        setupLayout();
        this.presenter.initialize();

        return view;

    }

    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
            public void call(RxAction event) {
//                if (event.isType(RandomEvents.EVENT_SONG_ADDED)) {
//                    MDMSong song = (MDMSong) event.getSimpleData();
//                    Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + song.getName(),
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    /**
     * Binding information form the layout to objects
     */
    private View bindData(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playqueue, container, false);
        View view = inflater.inflate(R.layout.fragment_random, container, false);

        this.mAdapter.setListener(this);
        this.mAdapter.setCurrentSong(ump.getCursor());

        mRecycler = (RecyclerView) view.findViewById(R.id.random_recyclerview);
        mRecycler.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecycler.setAdapter(this.mAdapter);


        this.mProgressBar = (ProgressBar) view.findViewById(R.id.random_progress);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.random_swipe);
        this.swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                updateSongs();
            }
        });


//        LoginActivityBindingImpl user;
//        if (savedInstanceState == null) {
//            user = new LoginActivityBindingImpl("", "", true, "ccc", "dddd");
//        } else {
//            user = Parcels.unwrap(savedInstanceState.getParcelable(BINDING_PARCEL));
//        }

        this.binding.setEvents(this);
        ump.addListener(this);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelable(BINDING_PARCEL, Parcels.wrap(this.binding.getUser()));
    }

    /**
     * setting up the layout of the activity.
     * Here you must put elements, remove elements, manage events, ...
     */
    private void setupLayout() {

    }

    @Override
    public void onPlayAction(View caller, PlayQueueSongViewHolder holder) {
        this.presenter.playAction(holder.getAdapterPosition());
    }

    @Override
    public void onAuthor(View caller, PlayQueueSongViewHolder holder) {
        this.presenter.authorAction(holder.song);
        showAlbumInfo(holder.icover, holder.song.getAlbum());
    }

    /**
     * Show the AlbumInfo Activity
     *
     * @param cover
     * @param album
     */
    private void showAlbumInfo(final ImageView cover, MDMAlbum album) {
        Observable<MDMAlbum> observable = presenter.getAlbum(album);

        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMAlbum>() {
            @Override
            public void call(MDMAlbum mdmAlbum) {

                //lets show the albuminfoactivity
                Intent ssa = new Intent(getActivity(), AlbumInfoActivity.class);
                ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, mdmAlbum);
                Bitmap bitmap = ((BitmapDrawable) cover.getDrawable()).getBitmap();
                AlbumInfoActivity.defaultArt = bitmap;

                //ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_ART, bitmap);

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(getActivity(), cover, "cover");
                getActivity().startActivity(ssa, options.toBundle());

            }
        });


    }

    @Override
    public void onRemoveAction(View caller, PlayQueueSongViewHolder holder) {
        final int index = holder.getAdapterPosition();
        this.presenter.removeAction(index);

        mAdapter.removeSong(index);
        if (index < this.mAdapter.getCurrentSong()) {
            this.mAdapter.setCurrentSong(this.mAdapter.getCurrentSong() - 1);
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mAdapter.notifyItemRemoved(index);
            }
        });
    }

    @Override
    public void onShareAction(View itemView, final PlayQueueSongViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.share_song));

        builder.setMessage(getString(R.string.share_song_select))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.share_song_select_audio), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        shareAudio(holder);
                    }
                })
                .setNegativeButton(getString(R.string.share_song_select_text), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        shareTextRecommendation(holder);

                    }
                }).setNeutralButton(getString(R.string.share_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    private void shareTextRecommendation(PlayQueueSongViewHolder holder) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.share_song_text_start) + " \"" +
                holder.song.getName() + "\" " + getString(R.string.share_song_text_album) +
                " \"" + holder.song.getAlbum().getName() + "\"" +
                getString(R.string.share_song_text_of) + "\"" +
                holder.song.getAlbum().getAuthor().getName() + "\"" + " (I <3 messic)";
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_by)));
    }

    private void shareAudio(PlayQueueSongViewHolder holder) {
        File songPath = null;
        try {
            if (holder.song.isDownloaded(config)) {
                String folderPath = holder.song.calculateExternalStorageFolder();
                String filePath = folderPath + "/" + holder.song.calculateExternalFilename();
                songPath = new File(filePath);
            } else {
                //we create the tmp file at the messic folder
                String tmpFile = UtilFile.getMessicOfflineFolderAbsolutePath() + "/.tmp";
                File outputDir = new File(tmpFile);
                outputDir.mkdirs();
                outputDir = new File(tmpFile + "/share.mp3");
                if (outputDir.exists())
                    outputDir.delete();

                String sharePath = holder.song.getURL(config);
                URL url = new URL(sharePath);
                InputStream is = url.openStream();
                FileOutputStream fos = new FileOutputStream(outputDir);
                byte[] b = new byte[1024];
                int length;

                while ((length = is.read(b)) != -1) {
                    fos.write(b, 0, length);
                }
                is.close();
                fos.close();
                songPath = outputDir;
            }

            Uri uriOld = Uri.parse(songPath.getAbsolutePath());

            Uri uri2 = MediaStore.Audio.Media.getContentUriForPath(songPath.getAbsolutePath());
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.TITLE, holder.song.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.DATA, songPath.getAbsolutePath());
            values.put(MediaStore.Audio.Media.DATA, songPath.getAbsolutePath());
            values.put(MediaStore.Audio.Media.ARTIST, holder.song.getAlbum().getAuthor().getName());
            values.put(MediaStore.Audio.Media.ALBUM, holder.song.getAlbum().getName());
            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            Uri newUri = getActivity().getContentResolver().insert(uri2, values);

            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.setType("audio/*");

            String shareBody = getString(R.string.share_song_text_start) + " \"" +
                    holder.song.getName() + "\" " + getString(R.string.share_song_text_album) +
                    " \"" + holder.song.getAlbum().getName() + "\"" +
                    getString(R.string.share_song_text_of) + "\"" +
                    holder.song.getAlbum().getAuthor().getName() + "\"" + " (I <3 messic)";


            share.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            share.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//            ArrayList<Uri> uris = new ArrayList<>();
//            uris.add(uri);
            //share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            share.putExtra(Intent.EXTRA_STREAM, uriOld);

            startActivity(Intent.createChooser(share, getString(R.string.share_by)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paused(MDMSong song, int index) {

    }

    @Override
    public void playing(MDMSong song, boolean resumed, int index) {
        this.mAdapter.setCurrentSong(index);
        if (!resumed && isVisible()) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void completed(int index) {

    }

    @Override
    public void added(MDMSong song) {
        updateSongs();
    }

    @Override
    public void added(MDMAlbum album) {
        updateSongs();
    }

    @Override
    public void added(MDMPlaylist playlist) {
        updateSongs();
    }

    @Override
    public void removed(MDMSong song) {
        //we deal this directly
    }

    @Override
    public void empty() {
        updateSongs();
    }

    @Override
    public void connected() {
        updateSongs();
    }

    @Override
    public void disconnected() {

    }


}
