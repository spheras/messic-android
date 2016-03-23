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
package org.messic.android.smartphone.activities.main.fragments.random;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;

public class RandomSongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView icover;
    public IViewHolderClicks listener;
    public TextView tvAuthor;
    public TextView tvAlbum;
    public TextView tvSongName;
    public ImageView bPlay;
    public ImageView bRightPlay;
    public MDMSong song;
    public View vBackground;
    public View ibBackground;

    public RandomSongViewHolder(final View itemView, final IViewHolderClicks listener) {
        super(itemView);
        this.listener = listener;
        bindComponents();
        bindEvents();
    }

    private void bindComponents() {
        this.icover = (ImageView) itemView.findViewById(R.id.songtrack_icover);
        this.tvAuthor = (TextView) itemView.findViewById(R.id.songtrack_tauthor);
        this.tvAlbum = (TextView) itemView.findViewById(R.id.songtrack_talbum);
        this.tvSongName = (TextView) itemView.findViewById(R.id.songtrack_tsongname);
        this.bPlay = (ImageView) itemView.findViewById(R.id.songtrack_bplay);
        this.bRightPlay = (ImageView) itemView.findViewById(R.id.songtrack_detailed_play);
        this.vBackground = itemView.findViewById(R.id.songtrack_background);
        this.ibBackground = (ImageButton) itemView.findViewById(R.id.songtrack_ib_background);
    }

    private void bindEvents() {
        this.bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayAction(itemView, RandomSongViewHolder.this);
            }
        });
        this.bRightPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayAction(itemView, RandomSongViewHolder.this);
            }
        });
        this.bPlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongPlayAction(itemView, RandomSongViewHolder.this);
                return false;
            }
        });
        this.bRightPlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongPlayAction(itemView, RandomSongViewHolder.this);
                return false;
            }
        });
        this.ibBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBackgroundAction(itemView, RandomSongViewHolder.this);
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    public static interface IViewHolderClicks {
        public void onPlayAction(View caller, RandomSongViewHolder holder);

        public void onLongPlayAction(View caller, RandomSongViewHolder holder);

        public void onBackgroundAction(View caller, RandomSongViewHolder holder);
    }
}
