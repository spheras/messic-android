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
package org.messic.android.smartphone.activities.main.fragments.search;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.databinding.FragmentSearchBinding;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.smartphone.activities.main.MainActivity;
import org.messic.android.smartphone.rxevents.RxAction;
import org.messic.android.smartphone.rxevents.RxDispatcher;
import org.messic.android.smartphone.utils.WrapContentLinearLayoutManager;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SearchFragment extends Fragment implements SearchSongViewHolder.IViewHolderClicks {


    public static final String SEARCH_CONTENT_KEY = "SEARCH_CONTENT";
    private static String searchContent = "";
    @Inject
    SearchPresenter presenter;
    @Inject
    SearchSongAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private Action1<MDMSong> searchSongsOnNext = new Action1<MDMSong>() {
        @Override
        public void call(MDMSong song) {
            mAdapter.addSong(song);
            mAdapter.notifyDataSetChanged();
        }
    };
    private RecyclerView mRecycler;
    private ProgressBar mProgressBar;
    private Action1<Throwable> searchSongsOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };
    private FragmentSearchBinding binding;
    private TextView tvSearchDescription;
    private Action0 searchSongsOnCompleted = new Action0() {
        @Override
        public void call() {
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            tvSearchDescription.setText(searchContent);
        }
    };

    /**
     * binding event for the close operation
     *
     * @param v
     */
    public void onCloseSearchTab(View v) {
        MainActivity main = (MainActivity) getActivity();
        main.closeSearchTab();
    }


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
        android.app.Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAdapter.clear();
                    Observable<MDMSong> observable = presenter.getSearchedSongs(searchContent);
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(searchSongsOnNext, searchSongsOnError, searchSongsOnCompleted);
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

    public void updateSearch(String searchContent) {
        this.searchContent = searchContent;
        if (getActivity() != null) {
            this.tvSearchDescription.setText(getText(R.string.search_searching));
            updateSongs();
        }
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
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        this.mAdapter.setListener(this);

        mRecycler = (RecyclerView) view.findViewById(R.id.search_recyclerview);
        mRecycler.setLayoutManager(new WrapContentLinearLayoutManager(this.getActivity()));
        mRecycler.setAdapter(this.mAdapter);


        this.tvSearchDescription = (TextView) view.findViewById(R.id.search_description);

        this.mProgressBar = (ProgressBar) view.findViewById(R.id.search_progress);

        /*
        view.findViewById(R.id.search_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseSearchTab(v);
            }
        });*/

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.search_swipe);
        this.swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                updateSongs();
            }
        });

        //it is a problem when creating new fragments with new arguments, frame adapter doesn't seems to be working removing old fragments
        //searchContent = getArguments().getString(SEARCH_CONTENT_KEY);
        tvSearchDescription.setText(searchContent);

        if (savedInstanceState != null) {
            searchContent = savedInstanceState.getString(SEARCH_CONTENT_KEY);
        }

        this.binding.setEvents(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_CONTENT_KEY, searchContent);
    }

    /**
     * setting up the layout of the activity.
     * Here you must put elements, remove elements, manage events, ...
     */
    private void setupLayout() {

    }

    @Override
    public void onPlayAction(View caller, SearchSongViewHolder holder) {
        this.presenter.playAction(holder.song);
    }

    @Override
    public void onLongPlayAction(View caller, SearchSongViewHolder holder) {
        this.presenter.longPlayAction(holder.song);
    }

    /**
     * Show the AlbumInfo Activity
     *
     * @param holder
     */
    private void showAlbumInfo(SearchSongViewHolder holder, MDMAlbum album) {
        //lets show the albuminfoactivity
        Intent ssa = new Intent(getActivity(), AlbumInfoActivity.class);
        ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, album);

        Bitmap bitmap = ((BitmapDrawable) holder.icover.getDrawable()).getBitmap();
        AlbumInfoActivity.defaultArt = bitmap;

        //ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_ART, bitmap);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this.getActivity(), holder.icover, "cover");
        getActivity().startActivity(ssa, options.toBundle());
    }

    private void textAction(View caller, final SearchSongViewHolder holder) {
        Observable<MDMAlbum> observable = this.presenter.authorAction(holder.song);
        mProgressBar.setVisibility(View.VISIBLE);
        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMAlbum>() {
            @Override
            public void call(MDMAlbum mdmAlbum) {
                mProgressBar.setVisibility(View.GONE);
                showAlbumInfo(holder, mdmAlbum);
            }
        });
    }


    @Override
    public void onBackgroundAction(View caller, SearchSongViewHolder holder) {
        textAction(caller, holder);
    }
}
