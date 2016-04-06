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

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.main.MainActivity;
import org.messic.android.smarttv.activities.main.fragments.cardview.ActionCardViewItem;
import org.messic.android.smarttv.activities.main.fragments.cardview.ActionEmptyCardViewItem;
import org.messic.android.smarttv.activities.main.fragments.cardview.AlbumCardViewItem;
import org.messic.android.smarttv.activities.main.fragments.cardview.CardViewItem;
import org.messic.android.smarttv.activities.main.fragments.cardview.IndexCardViewItem;
import org.messic.android.smarttv.activities.main.fragments.cardview.PlaylistQueueCardViewItem;
import org.messic.android.smarttv.activities.main.fragments.cardview.SongCardViewItem;
import org.messic.android.smarttv.activities.recommendations.UpdateRecommendationsService;
import org.messic.android.smarttv.activities.search.SearchActivity;
import org.messic.android.smarttv.rxevents.RxAction;
import org.messic.android.smarttv.rxevents.RxDispatcher;
import org.messic.android.smarttv.utils.PicassoBackgroundManagerTarget;
import org.messic.android.smarttv.utils.UtilMessic;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainFragment extends BrowseFragment implements PlayerEventListener {
    private static final String TAG = "MainFragment";
    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int NO_NOTIFICATION = -1;
    private final Handler mHandler = new Handler();
    @Inject
    MainFragmentPresenter presenter;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    Presenter mCardPresenter;
    @Inject
    ArrayObjectAdapter mRowsAdapter;
    @Inject
    Configuration config;

    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private Subscription subscription;
    private Action1<MDMRandomList> randomPlaylistOnNext = new Action1<MDMRandomList>() {
        @Override
        public void call(MDMRandomList rl) {
            //we add the randomlist
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
            for (int j = 0; j < rl.getSongs().size(); j++) {
                SongCardViewItem songcard = new SongCardViewItem(rl.getSongs().get(j));
                listRowAdapter.add(songcard);
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
                AlbumCardViewItem albumcard = new AlbumCardViewItem(album);
                listRowAdapter.add(albumcard);
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
    private URI mBackgroundURI;
    private Timer mBackgroundTimer;

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
        ArrayObjectAdapter aoa = new ArrayObjectAdapter(mCardPresenter);
        aoa.addAll(0, IndexCardViewItem.getIndexList(getHeadersFragment(), mRowsAdapter.size()));
        mRowsAdapter.add(new ListRow(headerSeparator, aoa));

        android.app.Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //mProgressBar.setVisibility(View.VISIBLE);
                    //mRowsAdapter.clear();
                    Observable<MDMAuthor> observable = presenter.loadAuthors();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(authorlistOnNext, authorlistOnError, authorCompleteAction);
                }
            });
        }
    }

    private void updateQueuePlayList() {
        List<MDMSong> queue = ump.getQueue();
        //this is for the empty action
        ActionCardViewItem actionEmpty = new ActionEmptyCardViewItem();
        List<PlaylistQueueCardViewItem> newQueue = new ArrayList<PlaylistQueueCardViewItem>();
        for (int i = 0; i < queue.size(); i++) {
            PlaylistQueueCardViewItem queueCard = new PlaylistQueueCardViewItem(queue.get(i));
            queueCard.setIndexAtList(i);
            newQueue.add(queueCard);
        }
        HeaderItem header = new HeaderItem(0, getString(R.string.QueuePlayList));
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
        listRowAdapter.add(0, actionEmpty);
        if (newQueue.size() > 0) {
            listRowAdapter.addAll(1, newQueue);
        }
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

        //in case an incoming petition of recommendation
        MDMSong selectedSong = getActivity().getIntent()
                .getExtras() != null ? (MDMSong) getActivity().getIntent()
                .getExtras().get(MainActivity.Song) : null;
        if (selectedSong != null) {
            removeNotification(getActivity().getIntent()
                    .getIntExtra(MainActivity.NOTIFICATION_ID, NO_NOTIFICATION));
            ump.getMessicPlayerService().getPlayer().addAndPlay(selectedSong);
        }
    }

    private void removeNotification(int notificationId) {
        if (notificationId != NO_NOTIFICATION) {
            NotificationManager notificationManager = (NotificationManager) getActivity()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }

    /**
     * Binding information form the layout to objects
     */

    private void bindData(Bundle savedInstanceState) {
        setupEventListeners();
        setAdapter(mRowsAdapter);
        ump.addListener(this);
        updateRecommendations();
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
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
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

    private void updatePlayQueue() {
        List<MDMSong> queue = ump.getQueue();
        //this is for the empty action
        ActionCardViewItem actionEmpty = new ActionEmptyCardViewItem();
        List<PlaylistQueueCardViewItem> newQueue = new ArrayList<PlaylistQueueCardViewItem>();
        for (int i = 0; i < queue.size(); i++) {
            PlaylistQueueCardViewItem queueCard = new PlaylistQueueCardViewItem(queue.get(i));
            queueCard.setIndexAtList(i);
            newQueue.add(queueCard);
        }
        if (mRowsAdapter != null && mRowsAdapter.size() > 0) {
            ArrayObjectAdapter listRowAdapter = (ArrayObjectAdapter) ((ListRow) mRowsAdapter.get(0)).getAdapter();
            listRowAdapter.clear();
            listRowAdapter.add(actionEmpty);
            listRowAdapter.addAll(0, newQueue);

            listRowAdapter.notifyArrayItemRangeChanged(0, listRowAdapter.size());
        }

    }

    @Override
    public void paused(MDMSong song, int index) {
        updatePlayQueue();
    }

    @Override
    public void playing(MDMSong song, boolean resumed, int index) {
        updatePlayQueue();
    }

    @Override
    public void completed(int index) {

    }

    @Override
    public void added(MDMSong song) {
        updatePlayQueue();
    }

    @Override
    public void added(MDMAlbum album) {
        updatePlayQueue();
    }

    @Override
    public void added(MDMPlaylist playlist) {
        updatePlayQueue();
    }

    @Override
    public void removed(MDMSong song) {
        updatePlayQueue();
    }

    @Override
    public void empty() {
        updatePlayQueue();
    }

    @Override
    public void connected() {
    }

    @Override
    public void disconnected() {
        ump.removeListener(this);
    }

    private void updateRecommendations() {
        Intent recommendationIntent = new Intent(getActivity(), UpdateRecommendationsService.class);
        getActivity().startService(recommendationIntent);
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    protected void setDefaultBackground(Drawable background) {
        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {
        mDefaultBackground = getResources().getDrawable(resourceId);
    }

    protected void updateBackground(URI uri) {
        Picasso.with(getActivity())
                .load(uri.toString())
                .resize(mMetrics.widthPixels, mMetrics.heightPixels)
                .centerCrop()
                .error(mDefaultBackground)
                .into(mBackgroundTarget);
    }

    protected void updateBackground(Drawable drawable) {
        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }

    protected void clearBackground() {
        BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {

        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object oitem,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (oitem == null) {
                return;
            }

            CardViewItem cardView = (CardViewItem) oitem;
            Object iitem = cardView.getItem();
            if (iitem instanceof MDMSong) {

                MDMSong song = (MDMSong) iitem;
                if (song.getAlbum() == null) {
                    return;
                }

                String coverOnlineURL =
                        config.getBaseUrl() + "/services/albums/" +
                                song.getAlbum().getSid() + "/cover?messic_token=" +
                                config.getLastToken();

                try {
                    mBackgroundURI = new URL(coverOnlineURL).toURI();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                startBackgroundTimer();
            } else if (iitem instanceof MDMAlbum) {

                MDMAlbum album = (MDMAlbum) iitem;

                String coverOnlineURL =
                        config.getBaseUrl() + "/services/albums/" +
                                album.getSid() + "/cover?messic_token=" +
                                config.getLastToken();

                try {
                    mBackgroundURI = new URL(coverOnlineURL).toURI();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                startBackgroundTimer();


            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBackgroundURI != null) {
                        updateBackground(mBackgroundURI);
                    }
                }
            });

        }
    }

    /**
     * When clicking an element
     */
    private final class ItemViewClickedListener implements OnItemViewClickedListener {

        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row) {
            CardViewItem cardView = (CardViewItem) oitem;
            cardView.onItemClicked(itemViewHolder, oitem, rowViewHolder, row, ump, config, presenter, MainFragment.this.getActivity());
        }
    }
}
