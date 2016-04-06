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

package org.messic.android.smarttv.activities.search;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.RowsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.main.fragments.cardview.SongCardViewItem;
import org.messic.android.smarttv.rxevents.RxAction;
import org.messic.android.smarttv.rxevents.RxDispatcher;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {
    private static final String TAG = "SearchFragment";
    private static final boolean DEBUG = true;
    private static final boolean FINISH_ON_RECOGNIZER_CANCELED = true;
    private static final int REQUEST_SPEECH = 0x00000010;
    private static final long SEARCH_DELAY_MS = 1000L;
    private final Handler mHandler = new Handler();
    @Inject
    SearchFragmentPresenter presenter;
    @Inject
    SearchArrayObjectAdapter mRowsAdapter;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    Presenter mCardPresenter;

    private String mQuery;
    private final Runnable mDelayedLoad = new Runnable() {
        @Override
        public void run() {
            loadRows();
        }
    };
    private Subscription subscription;

    @Override
    public void onStart() {
        super.onStart();
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
        mHandler.removeCallbacksAndMessages(null);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);


        setSearchResultProvider(this);

        setOnItemViewClickedListener(new ItemViewClickedListener());

        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            // SpeechRecognitionCallback is not required and if not provided recognition will be handled
            // using internal speech recognizer, in which case you must have RECORD_AUDIO permission
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    if (DEBUG) Log.v(TAG, "recognizeSpeech");
                    try {
                        startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Cannot find activity for speech recognizer", e);
                    }
                }
            });
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.v(TAG, "onActivityResult requestCode=" + requestCode +
                    " resultCode=" + resultCode +
                    " data=" + data);
        }
        switch (requestCode) {
            case REQUEST_SPEECH:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setSearchQuery(data, true);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Once recognizer canceled, user expects the current activity to process
                        // the same BACK press as user doesn't know about overlay activity.
                        // However, you may not want this behaviour as it makes harder to
                        // fall back to keyboard input.
                        if (FINISH_ON_RECOGNIZER_CANCELED) {
                            if (!hasResults()) {
                                if (DEBUG) Log.v(TAG, "Delegating BACK press from recognizer");
                                getActivity().onBackPressed();
                            }
                        }
                        break;
                    // the rest includes various recognizer errors, see {@link RecognizerIntent}
                }
                break;
        }
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        Log.i(TAG, String.format("Search Query Text Change %s", newQuery));
        loadQuery(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, String.format("Search Query Text Submit %s", query));
        loadQuery(query);
        return true;
    }

    public boolean hasResults() {
        return mRowsAdapter.size() > 0;
    }

    private boolean hasPermission(final String permission) {
        final Context context = getActivity();
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(
                permission, context.getPackageName());
    }

    private void loadQuery(String query) {
        mHandler.removeCallbacks(mDelayedLoad);
        if (!TextUtils.isEmpty(query) && !query.equals("nil")) {
            mQuery = query;
            mHandler.postDelayed(mDelayedLoad, SEARCH_DELAY_MS);
        }
    }

    private void loadRows() {
//        mRowsAdapter.clear();
        final ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(mCardPresenter);
        HeaderItem header = new HeaderItem(getActivity().getString(R.string.searchResults) + ": " + mQuery);
        final ListRow lr = new ListRow(header, listRowAdapter);
        mRowsAdapter.add(0, lr);
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
        final RowsFragment fragment = (RowsFragment) getChildFragmentManager().findFragmentById(android.support.v17.leanback.R.id.lb_results_frame);
        fragment.setSelectedPosition(0);

        Observable<MDMSong> observable = presenter.search(mQuery);
        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMSong>() {

            @Override
            public void call(MDMSong mdmSong) {
                listRowAdapter.add(new SongCardViewItem(mdmSong));
                listRowAdapter.notifyArrayItemRangeChanged(listRowAdapter.size() - 1, 1);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                //mProgressBar.setVisibility(View.GONE);
                //swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
            }
        }, new Action0() {
            @Override
            public void call() {
                fragment.setSelectedPosition(0);
            }
        });
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof String) {
                Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                        .show();
            } else if (item instanceof SongCardViewItem) {
                ump.addSong(((SongCardViewItem) item).getSong());
            }
        }
    }

}
