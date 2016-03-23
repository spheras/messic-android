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
package org.messic.android.smartphone.activities.main.fragments.playlist;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.smartphone.rxevents.RxAction;
import org.messic.android.smartphone.rxevents.RxDispatcher;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.databinding.FragmentPlaylistBinding;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PlaylistFragment extends Fragment implements PlaylistAdapter.EventListener {


    @Inject
    PlaylistPresenter presenter;
    @Inject
    PlaylistAdapter mAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private ExpandableListView gvExpandable;
    private Action1<MDMPlaylist> playlistsOnNext = new Action1<MDMPlaylist>() {
        @Override
        public void call(MDMPlaylist playlist) {
            mAdapter.addPlaylist(playlist);
        }
    };
    private ProgressBar mProgressBar;
    private Action1<Throwable> playlistsOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };
    private Action0 playlistsOnCompleted = new Action0() {
        @Override
        public void call() {
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    };
    private FragmentPlaylistBinding binding;

    @Override
    public void onStart() {
        super.onStart();

        if (this.mAdapter.getGroupCount() == 0) {
            updatePlaylists();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void updatePlaylists() {
        android.app.Activity activity = getActivity();
        if (activity != null) {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAdapter.clear();
                    Observable<MDMPlaylist> observable = presenter.getPlaylists();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(playlistsOnNext, playlistsOnError, playlistsOnCompleted);
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
            @Override
            public void call(RxAction event) {
//                if (event.isType(RandomEvents.EVENT_SONG_ADDED)) {
//                    MDMSong song = (MDMSong) event.getSimpleData();
//                    Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + song.getName(),
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }

    ViewGroup findParent(int position) {
        int count = gvExpandable.getChildCount();
        int ilocation = -1;
        int i = 0;
        for (; i < count && ilocation < position; i++) {
            if (gvExpandable.getChildAt(i).getTag().equals("parent")) {
                ilocation++;
            }
        }
        return (ViewGroup) gvExpandable.getChildAt(i - 1);
    }

    /**
     * Binding information form the layout to objects
     */
    private View bindData(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        this.mAdapter.setActivity(this.getActivity());
        this.mAdapter.setListener(this);

        this.mProgressBar = (ProgressBar) view.findViewById(R.id.playlist_progress);

        this.gvExpandable = (ExpandableListView) view.findViewById(R.id.playlist_elistview);
        this.gvExpandable.setAdapter(mAdapter);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.playlist_swipe);
        this.swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                updatePlaylists();
            }
        });


//        LoginActivityBindingImpl user;
//        if (savedInstanceState == null) {
//            user = new LoginActivityBindingImpl("", "", true, "ccc", "dddd");
//        } else {
//            user = Parcels.unwrap(savedInstanceState.getParcelable(BINDING_PARCEL));
//        }

        this.binding.setEvents(this);
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
    public void coverTouch(MDMSong song, int index) {
        presenter.playAction(song);
        Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + song.getName(),
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void coverLongTouch(MDMSong song, int index) {
        presenter.longPlayAction(song);
        Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + song.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void textTouch(MDMSong song, int index, final ImageView cover) {
        Observable<MDMAlbum> observable = this.presenter.authorAction(song);

        mProgressBar.setVisibility(View.VISIBLE);
        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMAlbum>() {
            @Override
            public void call(MDMAlbum mdmAlbum) {
                mProgressBar.setVisibility(View.GONE);
                showAlbumInfo(cover, mdmAlbum);
            }
        });

    }

    /**
     * Show the AlbumInfo Activity
     *
     * @param cover
     * @param album
     */

    private void showAlbumInfo(ImageView cover, MDMAlbum album) {
        //lets show the albuminfoactivity
        Intent ssa = new Intent(getActivity(), AlbumInfoActivity.class);
        ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, album);
        Bitmap bitmap = ((BitmapDrawable) cover.getDrawable()).getBitmap();
        AlbumInfoActivity.defaultArt=bitmap;
        //ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_ART, bitmap);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this.getActivity(), cover, "cover");
        getActivity().startActivity(ssa, options.toBundle());
    }

    @Override
    public void playlistTouch(MDMPlaylist playlist, int index) {
        presenter.playlistAction(playlist);
    }
}
