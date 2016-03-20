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
package org.messic.android.activities.main.fragments.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMSong;

public class SearchSongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView icover;
    public IViewHolderClicks listener;
    public TextView tvAuthor;
    public TextView tvAlbum;
    public TextView tvSongName;
    public ImageView bPlay;
    public MDMSong song;
    public View vBackground;
    public View ibBackground;

    public SearchSongViewHolder(final View itemView, final IViewHolderClicks listener) {
        super(itemView);
        this.listener = listener;
        bindComponents();
        bindEvents();
    }

    private void bindComponents() {
        this.icover = (ImageView) itemView.findViewById(R.id.search_item_icover);
        this.tvAuthor = (TextView) itemView.findViewById(R.id.search_item_tauthor);
        this.tvAlbum = (TextView) itemView.findViewById(R.id.search_item_talbum);
        this.tvSongName = (TextView) itemView.findViewById(R.id.search_item_tsongname);
        this.bPlay = (ImageView) itemView.findViewById(R.id.search_item_bplay);
        this.vBackground = itemView.findViewById(R.id.search_item_background);
        this.ibBackground = (ImageButton) itemView.findViewById(R.id.search_item_ib_background);
    }

    private void bindEvents() {
        this.bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlayAction(itemView, SearchSongViewHolder.this);
            }
        });
        this.bPlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongPlayAction(itemView, SearchSongViewHolder.this);
                return false;
            }
        });
        this.ibBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBackgroundAction(itemView, SearchSongViewHolder.this);
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    public static interface IViewHolderClicks {
        public void onPlayAction(View caller, SearchSongViewHolder holder);

        public void onLongPlayAction(View caller, SearchSongViewHolder holder);

        public void onBackgroundAction(View caller, SearchSongViewHolder holder);
    }
}
