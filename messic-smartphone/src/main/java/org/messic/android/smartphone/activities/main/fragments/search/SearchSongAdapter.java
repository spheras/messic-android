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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.smartphone.MessicSmartphoneApp;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class SearchSongAdapter extends RecyclerView.Adapter<SearchSongViewHolder> {

    @Inject
    Configuration config;
    private List<MDMSong> songs;
    private SearchSongViewHolder.IViewHolderClicks listener;


    public SearchSongAdapter() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);
        clear();
    }

    public List<MDMSong> getSongs() {
        return songs;
    }


    public void setListener(SearchSongViewHolder.IViewHolderClicks listener) {
        this.listener = listener;
    }

    public void addSong(MDMSong song) {
        this.songs.add(song);
    }

    public void addSongs(List<MDMSong> songs) {
        this.songs.addAll(songs);
    }

    public void clear() {
        this.songs = new ArrayList<MDMSong>();
    }

    @Override
    public SearchSongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_search_item, parent, false);
        return new SearchSongViewHolder(layoutView, listener);
    }

    @Override
    public void onBindViewHolder(SearchSongViewHolder holder, int position) {
        MDMSong song = songs.get(position);
        MDMAlbum album = song.getAlbum();
        MDMAuthor author = album.getAuthor();
        holder.tvAuthor.setText(author.getName());
        holder.tvAlbum.setText(album.getName());
        holder.tvSongName.setText(song.getTrack() + "-" + song.getName());
        holder.song = song;
        if (position % 2 == 0) {
            holder.vBackground.setBackgroundColor(Color.WHITE);
        } else {
            holder.vBackground.setBackgroundColor(Color.parseColor("#F8F8F8"));
        }


        Bitmap bmCover = holder.song.getAlbum().getOfflineCover(config);
        if (bmCover != null) {
            holder.icover.setImageBitmap(bmCover);
        } else {
            final String baseURL =
                    config.getBaseUrl() + "/services/albums/" + album.getSid()
                            + "/cover?preferredWidth=100&preferredHeight=100&messic_token="
                            + config.getLastToken();
            Picasso.with(MessicCoreApp.getInstance()).load(baseURL).into(holder.icover);
        }
    }

    @Override
    public int getItemCount() {
        if (songs != null) {
            return songs.size();
        }
        return 0;
    }
}
