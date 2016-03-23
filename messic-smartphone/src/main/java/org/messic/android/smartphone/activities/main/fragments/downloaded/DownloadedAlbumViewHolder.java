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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMAlbum;

public class DownloadedAlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public IViewHolderClicks listener;
    public MDMAlbum album;
    public RelativeLayout rlBackground;
    public ImageButton ibBackground;
    public ImageView ivCover;
    public TextView tvAuthor;
    public TextView tvAlbum;
    public ImageButton ibPlay;
    public ImageButton ibMore;

    public DownloadedAlbumViewHolder(final View itemView, final IViewHolderClicks listener) {
        super(itemView);
        this.listener = listener;
        bindComponents();
        bindEvents();
    }

    private void bindComponents() {
        this.rlBackground = (RelativeLayout) itemView.findViewById(R.id.downloaded_background);
        this.ibBackground = (ImageButton) itemView.findViewById(R.id.downloaded_ibBackground);
        this.ivCover = (ImageView) itemView.findViewById(R.id.downloaded_icover);
        this.tvAuthor = (TextView) itemView.findViewById(R.id.downloaded_tauthor);
        this.tvAlbum = (TextView) itemView.findViewById(R.id.downloaded_talbum);
        this.ibPlay = (ImageButton) itemView.findViewById(R.id.downloaded_play);
        this.ibMore = (ImageButton) itemView.findViewById(R.id.downloaded_ivmore);
    }

    private void bindEvents() {
        this.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayAction(itemView, DownloadedAlbumViewHolder.this);
            }
        });
        this.ibPlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongPlayAction(itemView, DownloadedAlbumViewHolder.this);
                return false;
            }
        });
        this.ibBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBackgroundAction(itemView, DownloadedAlbumViewHolder.this);
            }
        });
        this.ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMoreAction(itemView, ibMore, DownloadedAlbumViewHolder.this);
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    public static interface IViewHolderClicks {
        public void onPlayAction(View caller, DownloadedAlbumViewHolder holder);

        public void onLongPlayAction(View caller, DownloadedAlbumViewHolder holder);

        public void onBackgroundAction(View caller, DownloadedAlbumViewHolder holder);

        public void onMoreAction(View caller, View anchor, DownloadedAlbumViewHolder holder);
    }
}
