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
package org.messic.android.activities.main.fragments.queue;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;

public class PlayQueueSongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView icover;
    public IViewHolderClicks listener;
    public TextView tvAuthor;
    public TextView tvAlbum;
    public TextView tvSongName;
    public ImageView bPlay;
    public MDMSong song;
    public RelativeLayout rlBase;
    public ImageView bRemove;
    public ImageButton ibBackground;
    public ImageButton ibShare;

    public PlayQueueSongViewHolder(final View itemView, final IViewHolderClicks listener) {
        super(itemView);
        this.listener = listener;
        bindComponents();
        bindEvents();
    }

    private void bindComponents() {
        this.rlBase = (RelativeLayout) itemView.findViewById(R.id.playqueue_item_rlbase);
        this.icover = (ImageView) itemView.findViewById(R.id.playqueue_item_icover);
        this.tvAuthor = (TextView) itemView.findViewById(R.id.playqueue_item_tauthor);
        this.tvAlbum = (TextView) itemView.findViewById(R.id.playqueue_item_talbum);
        this.tvSongName = (TextView) itemView.findViewById(R.id.playqueue_item_tsong);
        this.bRemove = (ImageView) itemView.findViewById(R.id.playqueue_item_ivremove);
        this.bPlay = (ImageView) itemView.findViewById(R.id.playqueue_item_play);
        this.ibBackground = (ImageButton) itemView.findViewById(R.id.playqueue_ib_background);
        this.ibShare = (ImageButton) itemView.findViewById(R.id.playqueue_item_ibshare);
    }

    private void bindEvents() {
        this.ibBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAuthor(v, PlayQueueSongViewHolder.this);
            }
        });
        this.bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayAction(itemView, PlayQueueSongViewHolder.this);
            }
        });
        this.bRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRemoveAction(itemView, PlayQueueSongViewHolder.this);
            }
        });
        this.ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onShareAction(itemView, PlayQueueSongViewHolder.this);
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    public static interface IViewHolderClicks {
        public void onPlayAction(View caller, PlayQueueSongViewHolder holder);

        public void onAuthor(View caller, PlayQueueSongViewHolder holder);

        public void onRemoveAction(View caller, PlayQueueSongViewHolder holder);

        public void onShareAction(View itemView, PlayQueueSongViewHolder playQueueSongViewHolder);
    }
}
