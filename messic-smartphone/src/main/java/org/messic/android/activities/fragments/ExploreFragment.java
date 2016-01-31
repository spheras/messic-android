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
package org.messic.android.activities.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.activities.AlbumInfoActivity;
import org.messic.android.activities.adapters.AlbumAdapter;
import org.messic.android.activities.adapters.ExploreAuthorRecyclerViewAdapter;
import org.messic.android.activities.controllers.AlbumController;
import org.messic.android.activities.controllers.ExploreController;
import org.messic.android.fastscroller.FastScroller;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.util.List;

public class ExploreFragment
        extends Fragment
        implements TitleFragment {
    private ExploreController controller = new ExploreController();

    private ExploreAuthorRecyclerViewAdapter sa = null;

    private String title = "Explore";
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;


    public String getTitle() {
        return this.title;
    }

    public ExploreFragment setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().findViewById(R.id.explore_progress).setVisibility(View.VISIBLE);
        controller.getExploreAlbums(sa, getActivity(), this, true, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);

        getMessicService();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.explore_recyclerview);
        recyclerView.setHasFixedSize(true);
        gaggeredGridLayoutManager = new StaggeredGridLayoutManager(3, 1);
        recyclerView.setLayoutManager(gaggeredGridLayoutManager);

        FastScroller fastScroller = (FastScroller) rootView.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(recyclerView);

        sa = new ExploreAuthorRecyclerViewAdapter(getActivity(), new AlbumAdapter.EventListener() {

            public void textTouch(MDMAlbum album) {
                // AlbumController.getAlbumInfo( ExploreFragment.this.getActivity(), album.getSid() );
                AlbumController.getAlbumInfoOffline(ExploreFragment.this.getActivity(), album);
            }

            public void coverTouch(MDMAlbum album) {
                addAlbum(album);
            }

            public void coverLongTouch(MDMAlbum album) {
                playNowAlbum(album);
            }

            public void moreTouch(final MDMAlbum album, View anchor, final int index) {
                // Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ExploreFragment.this.getActivity(), anchor);

                // Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.menu_album, popup.getMenu());

                // registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_album_item_download:
                                downloadAlbum(album);
                                break;
                            case R.id.menu_album_item_play:
                                addAlbum(album);
                                break;
                            case R.id.menu_album_item_playnow:
                                playNowAlbum(album);
                                break;
                            case R.id.menu_album_item_remove:
                                AlbumInfoActivity.removeAlbum(getActivity(), album);
                                break;
                        }
                        return true;
                    }

                });

                popup.show();// showing popup menu
            }
        });


        recyclerView.setAdapter(sa);


        /*
        ListView gv = (ListView) rootView.findViewById(R.id.explore_recyclerview);

        sa = new AlbumAdapter(getActivity(), new AlbumAdapter.EventListener() {

            public void textTouch(MDMAlbum album) {
                // AlbumController.getAlbumInfo( ExploreFragment.this.getActivity(), album.getSid() );
                AlbumController.getAlbumInfoOffline(ExploreFragment.this.getActivity(), album);
            }

            public void coverTouch(MDMAlbum album) {
                addAlbum(album);
            }

            public void coverLongTouch(MDMAlbum album) {
                playNowAlbum(album);
            }

            public void moreTouch(final MDMAlbum album, View anchor, final int index) {
                // Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ExploreFragment.this.getActivity(), anchor);

                // Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.menu_album, popup.getMenu());

                // registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_album_item_download:
                                downloadAlbum(album);
                                break;
                            case R.id.menu_album_item_play:
                                addAlbum(album);
                                break;
                            case R.id.menu_album_item_playnow:
                                playNowAlbum(album);
                                break;
                            case R.id.menu_album_item_remove:
                                AlbumInfoActivity.removeAlbum(getActivity(), album);
                                break;
                        }
                        return true;
                    }

                });

                popup.show();// showing popup menu
            }

        });
        gv.setAdapter(sa);

        gv.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0 && !ExploreController.downloading) {
                    int lastInScreen = firstVisibleItem + visibleItemCount;
                    if (lastInScreen == totalItemCount) {
                        getActivity().findViewById(R.id.explore_progress).setVisibility(View.VISIBLE);
                        controller.getExploreAlbums(sa, getActivity(), ExploreFragment.this, false, null,
                                totalItemCount, visibleItemCount * 2);
                    }
                }
            }
        });
        */

        final SwipeRefreshLayout srl = (SwipeRefreshLayout) rootView.findViewById(R.id.explore_swipe);
        srl.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    public void run() {
                        if (getActivity() != null) {
                            View v = getActivity().findViewById(R.id.explore_progress);
                            if (v != null) {
                                v.setVisibility(View.VISIBLE);
                                controller.getExploreAlbums(sa, getActivity(), ExploreFragment.this, true, srl);
                            }
                        }
                    }
                });
            }
        });

        return rootView;
    }

    /**
     * The info have been loaded, and we need to remove the progress component
     */
    public void eventExploreInfoLoaded() {
        final Activity act = getActivity();
        if (act != null) {
            act.runOnUiThread(new Runnable() {

                public void run() {
                    if (act != null) {
                        View progress = act.findViewById(R.id.explore_progress);
                        if (progress != null) {
                            progress.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

    private void getMessicService() {
        UtilMusicPlayer.getMessicPlayerService(getActivity());
    }

    private void downloadAlbum(MDMAlbum album) {
        List<MDMSong> songs = album.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            MDMSong song = songs.get(i);
            song.setAlbum(album);
        }

        UtilDownloadService.addDownload(getActivity(), album, null);
        Toast.makeText(getActivity(), getResources().getText(R.string.download_added) + album.getName(),
                Toast.LENGTH_SHORT).show();
    }

    private void playNowAlbum(MDMAlbum album) {
        List<MDMSong> songs = album.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            MDMSong song = songs.get(i);
            song.setAlbum(album);
        }

        UtilMusicPlayer.addSongsAndPlay(getActivity(), album.getSongs());
    }

    private void addAlbum(MDMAlbum album) {
        UtilMusicPlayer.addAlbum(getActivity(), album);
        Toast.makeText(getActivity(), getResources().getText(R.string.player_added) + album.getName(),
                Toast.LENGTH_SHORT).show();
    }
}
