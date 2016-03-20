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
package org.messic.android.activities.main.fragments.explore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.databinding.FragmentExploreBinding;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;
import org.messic.android.views.fastscroller.FastScroller;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ExploreFragment extends Fragment implements ExploreAuthorsAdapter.EventListener {


    @Inject
    ExplorePresenter presenter;
    @Inject
    ExploreAuthorsAdapter mAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Subscription subscription;
    private RecyclerView mRecycler;
    private ProgressBar mProgressBar;
    private FragmentExploreBinding binding;
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;
    private FastScroller fastScroller;

    private Action1<Throwable> exploreAuthorOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            Timber.e(throwable.getMessage(), throwable);
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };
    private Action0 exploreAuthorOnCompleted = new Action0() {
        @Override
        public void call() {
            mAdapter.notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);
        }
    };
    private Action1<MDMAuthor> exploreAuthorOnNext = new Action1<MDMAuthor>() {
        @Override
        public void call(MDMAuthor author) {
            mAdapter.addAuthor(author);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        if (this.mAdapter.getItemCount() == 0) {
            updateAuthors();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateAuthors() {
        android.app.Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    Observable<MDMAuthor> observable = presenter.getAuthors();
                    mAdapter.clear();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(exploreAuthorOnNext, exploreAuthorOnError, exploreAuthorOnCompleted);
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
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore, container, false);
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        this.mAdapter.setContext(this.getActivity());
        this.mAdapter.setListener(this);

        mRecycler = (RecyclerView) view.findViewById(R.id.explore_recyclerview);
        mRecycler.setHasFixedSize(true);
        gaggeredGridLayoutManager = new StaggeredGridLayoutManager(3, 1);
        mRecycler.setLayoutManager(gaggeredGridLayoutManager);
        mRecycler.setAdapter(this.mAdapter);

        fastScroller = (FastScroller) view.findViewById(R.id.explore_fastscroller);
        fastScroller.setRecyclerView(mRecycler);

        mProgressBar = (ProgressBar) view.findViewById(R.id.explore_progress);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.explore_swipe);
        this.swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                updateAuthors();
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
    public void coverTouch(MDMAlbum album) {
        presenter.playAction(album);
        Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + album.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void coverLongTouch(MDMAlbum album) {
        presenter.longPlayAction(album);
        Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + album.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void textTouch(MDMAlbum album, ImageView cover) {
        showAlbumInfo(cover, album);

    }

    @Override
    public void moreTouch(final MDMAlbum album, View anchor, int index) {
        // Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(ExploreFragment.this.getActivity(), anchor);

        // Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.activity_explore_menu_album, popup.getMenu());

        // registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_album_item_play:
                        presenter.playAction(album);
                        break;
                    case R.id.menu_album_item_playnow:
                        presenter.longPlayAction(album);
                        break;
                }
                return true;
            }

        });

        popup.show();// showing popup menu
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
        BitmapDrawable d = ((BitmapDrawable) cover.getDrawable());
        if (d != null) {
            Bitmap bitmap = d.getBitmap();
            AlbumInfoActivity.defaultArt = bitmap;
            //ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_ART, bitmap);

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this.getActivity(), cover, "cover");
            getActivity().startActivity(ssa, options.toBundle());
        }
    }
}
