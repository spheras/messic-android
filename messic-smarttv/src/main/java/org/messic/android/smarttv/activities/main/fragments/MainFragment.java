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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.Presenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Target;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.rxevents.RxAction;
import org.messic.android.smarttv.rxevents.RxDispatcher;
import org.messic.android.smarttv.utils.PicassoBackgroundManagerTarget;
import org.messic.android.smarttv.utils.UtilMessic;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainFragment extends BrowseFragment implements PlayerEventListener {
    private static final String TAG = "MainFragment";
    @Inject
    MainFragmentPresenter presenter;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    Presenter mCardPresenter;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private Subscription subscription;
    private ArrayObjectAdapter mRowsAdapter;
    private Action1<MDMRandomList> randomPlaylistOnNext = new Action1<MDMRandomList>() {
        @Override
        public void call(MDMRandomList rl) {
            //we add the randomlist
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
            for (int j = 0; j < rl.getSongs().size(); j++) {
                listRowAdapter.add(rl.getSongs().get(j));
            }

            HeaderItem header = new HeaderItem(mRowsAdapter.size() + 30, UtilMessic.getRandomTitle(MainFragment.this.getActivity(), rl));
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
            mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.size() - 1, 1);
        }
    };
    private Action1<Throwable> randomPlaylistOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
            //mProgressBar.setVisibility(View.GONE);
            //swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };

    private Action0 authorCompleteAction = new Action0() {
        @Override
        public void call() {
            //@TODO
        }
    };

    private Action1<MDMAuthor> authorlistOnNext = new Action1<MDMAuthor>() {
        @Override
        public void call(MDMAuthor author) {
            //we add the author

            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
            for (int j = 0; j < author.getAlbums().size(); j++) {
                MDMAlbum album = author.getAlbums().get(j);
                album.setAuthor(author);
                listRowAdapter.add(album);
            }

            HeaderItem header = new HeaderItem(mRowsAdapter.size() + 30, author.getName());
            mRowsAdapter.add(new ListRow(header, listRowAdapter));

            mRowsAdapter.notifyArrayItemRangeChanged(mRowsAdapter.size() - 1, 1);
        }
    };
    private Action1<Throwable> authorlistOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
            //mProgressBar.setVisibility(View.GONE);
            //swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        updateAllLists();
    }

    private void updateAllLists() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRowsAdapter.clear();
                updateQueuePlayList();
                updateRandomPlayLists(new Action0() {
                                          @Override
                                          public void call() {
                                              mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
                                              updateAuthorsList();
                                              //mProgressBar.setVisibility(View.GONE);
                                              //swipeRefreshLayout.setRefreshing(false);
                                          }
                                      }
                );
            }
        });

    }

    private void updateAuthorsList() {
        HeaderItem headerSeparator = new HeaderItem(0, "░░ " + getString(R.string.header_title_authors) + " ░░░░░░░░░░░░░░░░░░░░░░░░");
        mRowsAdapter.add(new ListRow(headerSeparator, new ArrayObjectAdapter()));

        android.app.Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mProgressBar.setVisibility(View.VISIBLE);
                    mRowsAdapter.clear();
                    Observable<MDMAuthor> observable = presenter.loadAuthors();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(authorlistOnNext, authorlistOnError, authorCompleteAction);
                }
            });
        }
    }

    private void updateQueuePlayList() {
        List<MDMSong> queue = ump.getQueue();
        List<MDMQueueSong> newQueue = new ArrayList<MDMQueueSong>();
        newQueue.add(new MDMQueueSong(new MDMSong())); //this is for the empty action, just a trick
        for (int i = 0; i < queue.size(); i++) {
            MDMQueueSong mqs = new MDMQueueSong(queue.get(i));
            mqs.indexAtList = i;
            newQueue.add(mqs);
        }
        HeaderItem header = new HeaderItem(0, getString(R.string.QueuePlayList));
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
        listRowAdapter.addAll(0, newQueue);
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void updateRandomPlayLists(final Action0 completeAction) {
        android.app.Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mProgressBar.setVisibility(View.VISIBLE);
                    Observable<MDMRandomList> observable = presenter.loadRandomPlaylists();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(randomPlaylistOnNext, randomPlaylistOnError, completeAction);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);

        bindData(savedInstanceState);
        setupLayout();
        this.presenter.initialize();

        //@TODO
//        MDMSong selectedSong = getActivity().getIntent()
//                .getExtras() != null ? (MDMSong) getActivity().getIntent()
//                .getExtras().get(MainActivity.Song) : null;
//        if (selectedSong != null) {
//            removeNotification(getActivity().getIntent()
//                    .getIntExtra(MainActivity.NOTIFICATION_ID, NO_NOTIFICATION));
//            UtilMusicPlayer.getMessicPlayerService(getActivity()).getPlayer().addAndPlay(selectedSong);
//        }
    }

    /**
     * Binding information form the layout to objects
     */

    private void bindData(Bundle savedInstanceState) {
        setupEventListeners();
        ump.addListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //@TODO
        //outState.putParcelable(BINDING_PARCEL, Parcels.wrap(this.binding.getUser()));
    }

    private void setupLayout() {
        prepareBackgroundManager();
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void prepareBackgroundManager() {
        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //@TODO
//                Intent intent = new Intent(getActivity(), SearchActivity.class);
//                startActivity(intent);
            }
        });

//        setOnItemViewClickedListener(new ItemViewClickedListener());
//        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }


    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
                                                public void call(RxAction event) {
//                                                    if (event.isType(LoginEvents.EVENT_FINISH_ACTIVITY)) {
//                                                        LoginActivity.this.finish();
//                                                    } else if (event.isType(LoginEvents.EVENT_SERVER_STATUS)) {
//                                                        UtilNetwork.MessicServerConnectionStatus result = (UtilNetwork.MessicServerConnectionStatus) event.getSimpleData();
//                                                        showServerStatus(result.reachable, result.running);
//                                                    } else if (event.isType(LoginEvents.EVENT_SHOW_SCREEN)) {
//                                                        String screen = (String) event.getSimpleData();
//
//                                                        if (screen.equals(LoginEvents.SCREEN_MAIN)) {
//                                                            Intent ssa = new Intent(LoginActivity.this, MainActivity.class);
//                                                            LoginActivity.this.startActivity(ssa);
//                                                        }
//
//                                                    }
//
                                                }
                                            }

        );
    }

    @Override
    public void paused(MDMSong song, int index) {

    }

    @Override
    public void playing(MDMSong song, boolean resumed, int index) {

    }

    @Override
    public void completed(int index) {

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
