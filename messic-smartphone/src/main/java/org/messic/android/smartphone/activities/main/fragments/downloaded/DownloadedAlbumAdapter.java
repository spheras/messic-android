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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.smartphone.MessicSmartphoneApp;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class DownloadedAlbumAdapter extends RecyclerView.Adapter<DownloadedAlbumViewHolder> {

    @Inject
    Configuration config;
    private List<MDMAlbum> albums;
    private DownloadedAlbumViewHolder.IViewHolderClicks listener;


    public DownloadedAlbumAdapter() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);
        clear();
    }

    public void removeAlbum(int index) {
        albums.remove(index);
    }

    public void setListener(DownloadedAlbumViewHolder.IViewHolderClicks listener) {
        this.listener = listener;
    }

    public void addAlbum(MDMAlbum album) {
        this.albums.add(album);
    }

    public void addAlbums(List<MDMAlbum> albums) {
        this.albums.addAll(albums);
    }

    public void clear() {
        this.albums = new ArrayList<MDMAlbum>();
    }

    @Override
    public DownloadedAlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_downloaded_item, parent, false);
        return new DownloadedAlbumViewHolder(layoutView, listener);
    }

    @Override
    public void onBindViewHolder(DownloadedAlbumViewHolder holder, int position) {
        MDMAlbum album = albums.get(position);
        MDMAuthor author = album.getAuthor();
        holder.tvAuthor.setText(author.getName());
        holder.album = album;
        holder.tvAlbum.setText(album.getName());
        if (position % 2 == 0) {
            holder.rlBackground.setBackgroundColor(Color.WHITE);
        } else {
            holder.rlBackground.setBackgroundColor(Color.parseColor("#F8F8F8"));
        }

        Bitmap coverbm = album.getOfflineCover(config);
        if (coverbm != null) {
            holder.ivCover.setImageBitmap(coverbm);
        } else {
            holder.ivCover.setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        if (albums != null) {
            return albums.size();
        }
        return 0;
    }
}
