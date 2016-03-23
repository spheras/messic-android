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
package org.messic.android.smartphone.activities.main.fragments.downloaded;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.smartphone.rxevents.RxAction;
import org.messic.android.smartphone.rxevents.RxDispatcher;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.databinding.FragmentDownloadedBinding;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class DownloadedFragment extends Fragment implements DownloadedAlbumViewHolder.IViewHolderClicks {

    @Inject
    DownloadedPresenter presenter;
    @Inject
    DownloadedAlbumAdapter mAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Subscription subscription;
    private Action1<MDMAlbum> downloadedAlbumsOnNext = new Action1<MDMAlbum>() {
        @Override
        public void call(MDMAlbum album) {
            mAdapter.addAlbum(album);
            mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
        }
    };
    private RecyclerView mRecycler;
    private ProgressBar mProgressBar;
    private Action1<Throwable> downloadedAlbumsOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
        }
    };
    private Action0 downloadedAlbumsOnCompleted = new Action0() {
        @Override
        public void call() {
            mAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    };
    private FragmentDownloadedBinding binding;

    @Override
    public void onStart() {
        super.onStart();

        if (this.mAdapter.getItemCount() == 0) {
            updateAlbums();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateAlbums() {
        android.app.Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAdapter.clear();
                    Observable<MDMAlbum> observable = presenter.getDownloadedAlbums();
                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(downloadedAlbumsOnNext, downloadedAlbumsOnError, downloadedAlbumsOnCompleted);
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
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloaded, container, false);
        View view = inflater.inflate(R.layout.fragment_random, container, false);

        this.mAdapter.setListener(this);

        mRecycler = (RecyclerView) view.findViewById(R.id.random_recyclerview);
        mRecycler.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        mRecycler.setAdapter(this.mAdapter);


        this.mProgressBar = (ProgressBar) view.findViewById(R.id.random_progress);

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.random_swipe);
        this.swipeRefreshLayout.setColorSchemeColors(Color.GREEN);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                updateAlbums();
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
    public void onPlayAction(View caller, DownloadedAlbumViewHolder holder) {
        this.presenter.playAction(holder.album);
    }

    @Override
    public void onLongPlayAction(View caller, DownloadedAlbumViewHolder holder) {
        this.presenter.longPlayAction(holder.album);
    }

    /**
     * Show the AlbumInfo Activity
     *
     * @param holder
     */
    private void showAlbumInfo(DownloadedAlbumViewHolder holder, MDMAlbum album) {
        //lets show the albuminfoactivity
        Intent ssa = new Intent(getActivity(), AlbumInfoActivity.class);
        ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, album);
        Bitmap bitmap = ((BitmapDrawable) holder.ivCover.getDrawable()).getBitmap();
        AlbumInfoActivity.defaultArt = bitmap;
        //ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_ART, bitmap);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this.getActivity(), holder.ivCover, "cover");
        getActivity().startActivity(ssa, options.toBundle());
    }

    private void textAction(View caller, final DownloadedAlbumViewHolder holder) {
        showAlbumInfo(holder, holder.album);
    }


    @Override
    public void onBackgroundAction(View caller, DownloadedAlbumViewHolder holder) {
        textAction(caller, holder);
    }

    @Override
    public void onMoreAction(View caller, View anchor, final DownloadedAlbumViewHolder holder) {
        // Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(DownloadedFragment.this.getActivity(), anchor);

        // Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.activity_downloaded_menu_album, popup.getMenu());

        // registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_album_item_remove:
                        removeAlbum(holder.album, holder);
                        break;
                    case R.id.menu_album_item_play:
                        presenter.playAction(holder.album);
                        break;
                    case R.id.menu_album_item_playnow:
                        presenter.longPlayAction(holder.album);
                        break;
                }
                return true;
            }

        });

        popup.show();// showing popup menu
    }

    private void removeAlbum(final MDMAlbum album, final DownloadedAlbumViewHolder holder) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        final ProgressDialog pd =
                                ProgressDialog.show(getActivity(), getActivity().getString(R.string.action_removing_local_album_title),
                                        getActivity().getString(R.string.action_removing_local_album_content), true);

                        presenter.removeAlbum(album).subscribe(new Action1<MDMSong>() {
                            @Override
                            public void call(MDMSong mdmSong) {

                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Toast.makeText(getActivity(), "Error:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                Timber.e(throwable.getMessage(), throwable);
                                throwable.printStackTrace();
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                pd.dismiss();
                                Toast.makeText(getActivity(), getActivity().getString(R.string.action_local_album_removed) + album.getName(),
                                        Toast.LENGTH_SHORT).show();
                                mAdapter.removeAlbum(holder.getAdapterPosition());
                                mAdapter.notifyItemRemoved(holder.getAdapterPosition());
                            }
                        });

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        break;
                }
            }
        };
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.action_remove_local_album));
        builder.setPositiveButton(getActivity().getString(R.string.yes), dialogClickListener);
        builder.setNegativeButton(getActivity().getString(R.string.no), dialogClickListener);
        builder.show();
    }
}
