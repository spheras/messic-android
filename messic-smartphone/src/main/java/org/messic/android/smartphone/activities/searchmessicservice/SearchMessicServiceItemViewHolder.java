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
package org.messic.android.smartphone.activities.searchmessicservice;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.messic.android.R;

public class SearchMessicServiceItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView hostname;
    public TextView ip;
    public TextView version;
    public View vstatus;
    private IViewHolderClicks listener;

    public SearchMessicServiceItemViewHolder(@NonNull View itemView, @NonNull IViewHolderClicks listener) {
        super(itemView);
        itemView.setOnClickListener(this);

        hostname = (TextView) itemView.findViewById(R.id.searchmessicservice_item_hostname);
        ip = (TextView) itemView.findViewById(R.id.searchmessicservice_item_ip);
        version = (TextView) itemView.findViewById(R.id.searchmessicservice_item_version);
        vstatus = (View) itemView.findViewById(R.id.searchmessicservice_item_vstatus);
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        this.listener.onItemTouch(view, this);
    }

    public static interface IViewHolderClicks {
        public void onItemTouch(View caller, SearchMessicServiceItemViewHolder holder);
    }
}
