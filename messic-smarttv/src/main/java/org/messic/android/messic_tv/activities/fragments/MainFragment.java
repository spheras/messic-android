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

package org.messic.android.messic_tv.activities.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.messic_tv.R;
import org.messic.android.messic_tv.activities.SearchActivity;
import org.messic.android.messic_tv.activities.presenters.MDMQueueSong;
import org.messic.android.messic_tv.activities.presenters.PlayQueueSongCardPresenter;
import org.messic.android.messic_tv.activities.presenters.SongCardPresenter;
import org.messic.android.messic_tv.controllers.AuthorsController;
import org.messic.android.messic_tv.controllers.SearchController;
import org.messic.android.messic_tv.util.PicassoBackgroundManagerTarget;
import org.messic.android.messic_tv.util.UtilMessic;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends BrowseFragment implements PlayerEventListener {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 15;

    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private Target mBackgroundTarget;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private final Handler mHandler = new Handler();
    private URI mBackgroundURI;
    Presenter mCardPresenter;

    private SearchController searchController;
    private AuthorsController authorsController;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        this.authorsController = new AuthorsController();
        this.searchController = new SearchController();

        prepareBackgroundManager();

        setupUIElements();

        //loadRows();

        setupEventListeners();

        authorsController.loadRandomPlaylists(this);
        UtilMusicPlayer.addListener(this.getActivity(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    public void loadRows(MDMRandomList[] randomlists) {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mCardPresenter = new PlayQueueSongCardPresenter();

        //loading queue playlist
        {
            List<MDMSong> queue = UtilMusicPlayer.getQueue(getActivity());
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


        //loading random lists
        mCardPresenter = new SongCardPresenter();
        int i;
        for (i = 0; i < randomlists.length; i++) {
            MDMRandomList rl = randomlists[i];
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
            //for (int j = 0; j < NUM_COLS; j++) {
            for (int j = 0; j < rl.getSongs().size(); j++) {
                listRowAdapter.add(rl.getSongs().get(j));
            }

            HeaderItem header = new HeaderItem(i + 30, UtilMessic.getRandomTitle(this.getActivity(), randomlists[i]));
            mRowsAdapter.add(new ListRow(header, listRowAdapter));

        }



        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAdapter(mRowsAdapter);
            }
        });

    }

    private void prepareBackgroundManager() {

        BackgroundManager backgroundManager = BackgroundManager.getInstance(getActivity());
        backgroundManager.attach(getActivity().getWindow());
        mBackgroundTarget = new PicassoBackgroundManagerTarget(backgroundManager);

        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);

        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
//                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
//                        .show();
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private void updatePlayQueue() {

        List<MDMSong> queue = UtilMusicPlayer.getQueue(getActivity());
        List<MDMQueueSong> newQueue = new ArrayList<MDMQueueSong>();
        newQueue.add(new MDMQueueSong(new MDMSong())); //this is for the empty action, just a trick
        for (int i = 0; i < queue.size(); i++) {
            MDMQueueSong mqs = new MDMQueueSong(queue.get(i));
            mqs.indexAtList = i;
            newQueue.add(mqs);
        }

        ArrayObjectAdapter listRowAdapter = (ArrayObjectAdapter) ((ListRow) mRowsAdapter.get(0)).getAdapter();
        listRowAdapter.clear();
        listRowAdapter.addAll(0, newQueue);

        listRowAdapter.notifyArrayItemRangeChanged(0, listRowAdapter.size());

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
        UtilMusicPlayer.addListener(this.getActivity(), this);
    }

    @Override
    public void disconnected() {
        UtilMusicPlayer.removeListener(this.getActivity(), this);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {

        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof String) {
                Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                        .show();
            } else if (item instanceof MDMQueueSong) {
                if (((MDMQueueSong) item).getAlbum() != null) {
                    if (UtilMusicPlayer.getCursor(getActivity()) == ((MDMQueueSong) item).indexAtList) {
                        if (UtilMusicPlayer.isPlaying(getActivity())) {
                            UtilMusicPlayer.pauseSong(getActivity());
                        } else {
                            UtilMusicPlayer.resumeSong(getActivity());
                        }
                    } else {
                        UtilMusicPlayer.setSong(getActivity(), ((MDMQueueSong) item).indexAtList);
                        UtilMusicPlayer.playSong(getActivity());
                    }
                } else {
                    //the clear action item?
                    UtilMusicPlayer.clearQueue(getActivity());
                }

            } else if (item instanceof MDMSong) {
                UtilMusicPlayer.addSong(getActivity(), (MDMSong) item);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof MDMSong) {

                MDMSong song = (MDMSong) item;
                if (song.getAlbum() == null) {
                    return;
                }

                String coverOnlineURL =
                        Configuration.getBaseUrl(MainFragment.this.getActivity()) + "/services/albums/" + song.getAlbum().getSid() + "/cover?messic_token=" + Configuration.getLastToken();

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

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
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

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackground(getResources().getDrawable(R.drawable.background_darkfish_green, null));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

}
