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
package org.messic.android.activities.albuminfo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class AlbumInfoSongAdapter extends RecyclerView.Adapter<AlbumInfoSongViewHolder> {

    @Inject
    Configuration config;
    @Inject
    UtilMusicPlayer ump;

    private List<MDMSong> songs;
    private AlbumInfoSongViewHolder.IViewHolderClicks listener;


    public AlbumInfoSongAdapter() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);
        clear();
    }


    public void setListener(AlbumInfoSongViewHolder.IViewHolderClicks listener) {
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
    public AlbumInfoSongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_albuminfo_songtrack, null);
        return new AlbumInfoSongViewHolder(layoutView, listener);
    }

    @Override
    public void onBindViewHolder(AlbumInfoSongViewHolder holder, int position) {
        MDMSong song = songs.get(position);
        holder.tvTrack.setText("" + song.getTrack());
        holder.tvSongName.setText(song.getName());
        holder.song = song;
        if (position % 2 == 0) {
            holder.vbackground.setBackgroundResource(R.color.activity_albuminfo_row);
        } else {
            holder.vbackground.setBackgroundResource(R.color.activity_albuminfo_rowodd);
        }

        boolean offline = config.isOffline();
        boolean downloaded = song.isDownloaded(config);
        boolean playableSong = ((offline && downloaded) || !offline);
        if (!offline) {
            if (downloaded) {
                holder.ivDownload.setVisibility(View.INVISIBLE);
                holder.ivRemove.setVisibility(View.VISIBLE);
                if (position % 2 == 0) {
                    holder.vbackground.setBackgroundResource(R.color.activity_albuminfo_row_downloaded);
                } else {
                    holder.vbackground.setBackgroundResource(R.color.activity_albuminfo_row_downloadedodd);
                }
            } else {
                holder.ivDownload.setVisibility(View.VISIBLE);
                holder.ivRemove.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.ivDownload.setVisibility(View.GONE);
            if (playableSong) {
                holder.ivRemove.setVisibility(View.VISIBLE);
            } else {
                holder.ivRemove.setVisibility(View.INVISIBLE);
            }
        }

        //if this song is being played
        MDMSong currentPlayed = ump.getCurrentSong();
        if (currentPlayed != null && ump.getCurrentSong().getSid() == holder.song.getSid() && ump.isPlaying()) {
            holder.vbackground.setBackgroundResource(R.color.activity_albuminfo_row_playing);
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
