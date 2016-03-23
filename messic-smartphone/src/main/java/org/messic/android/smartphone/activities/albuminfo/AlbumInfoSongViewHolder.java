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
package org.messic.android.smartphone.activities.albuminfo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;

public class AlbumInfoSongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public IViewHolderClicks listener;
    public MDMSong song;
    public TextView tvTrack;
    public TextView tvSongName;
    public ImageView ivPlay;
    public ImageView ivDownload;
    public ImageView ivRemove;
    public View vbackground;

    public AlbumInfoSongViewHolder(final View itemView, final IViewHolderClicks listener) {
        super(itemView);
        this.listener = listener;
        bindComponents();
        bindEvents();
    }

    private void bindComponents() {
        this.tvTrack = (TextView) itemView.findViewById(R.id.albuminfo_songtrack_ttrack);
        this.tvSongName = (TextView) itemView.findViewById(R.id.albuminfo_songtrack_tsongname);
        this.ivPlay = (ImageView) itemView.findViewById(R.id.albuminfo_songtrack_ivplay);
        this.ivDownload = (ImageView) itemView.findViewById(R.id.albuminfo_songtrack_ivdownload);
        this.ivRemove = (ImageView) itemView.findViewById(R.id.albuminfo_songtrack_ivremove);
        this.vbackground = itemView.findViewById(R.id.albuminfo_songtrack_background);
    }

    private void bindEvents() {
        this.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayAction(itemView, AlbumInfoSongViewHolder.this);
            }
        });
        this.ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRemove(itemView, AlbumInfoSongViewHolder.this);
            }
        });
        this.ivPlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongPlayAction(itemView, AlbumInfoSongViewHolder.this);
                return false;
            }
        });
        this.ivDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDownload(itemView, AlbumInfoSongViewHolder.this);
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    public static interface IViewHolderClicks {
        public void onPlayAction(View caller, AlbumInfoSongViewHolder holder);

        public void onLongPlayAction(View caller, AlbumInfoSongViewHolder holder);

        public void onRemove(View caller, AlbumInfoSongViewHolder holder);

        public void onDownload(View caller, AlbumInfoSongViewHolder holder);
    }
}
