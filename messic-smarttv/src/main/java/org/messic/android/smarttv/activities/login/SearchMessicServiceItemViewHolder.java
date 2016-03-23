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
package org.messic.android.smarttv.activities.login;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;

public class SearchMessicServiceItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnKeyListener {

    public TextView hostname;
    public TextView ip;
    public TextView version;
    public View vStatus;
    public View vbackground;
    public MDMMessicServerInstance instance;
    private IViewHolderClicks listener;
    private int SELECTED_COLOR = 0x5588FF88;
    private int SELECTED_DOWN = 0xEE55AA55;

    public SearchMessicServiceItemViewHolder(@NonNull View itemView, @NonNull IViewHolderClicks listener) {
        super(itemView);
        itemView.setOnClickListener(this);

        hostname = (TextView) itemView.findViewById(R.id.searchmessicservice_item_hostname);
        ip = (TextView) itemView.findViewById(R.id.searchmessicservice_item_ip);
        version = (TextView) itemView.findViewById(R.id.searchmessicservice_item_version);
        vStatus = itemView.findViewById(R.id.searchmessicservice_item_hostname_vstatus);
        vbackground = itemView.findViewById(R.id.searchmessicservice_item_background);
        vbackground.setBackgroundColor(Color.TRANSPARENT);
        this.instance = instance;

        vbackground.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    vbackground.setBackgroundColor(SELECTED_COLOR);
                } else {
                    vbackground.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
        vbackground.setOnClickListener(this);
        vbackground.setOnKeyListener(this);

        this.listener = listener;
    }


    @Override
    public void onClick(View view) {
        this.listener.onItemSelect(view, this);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_BUTTON_SELECT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                vbackground.setBackgroundColor(SELECTED_DOWN);
                this.listener.onItemSelect(v, this);
            } else {
                vbackground.setBackgroundColor(SELECTED_COLOR);
            }
        }


        return false;
    }

    public static interface IViewHolderClicks {
        public void onItemSelect(View caller, SearchMessicServiceItemViewHolder holder);
    }
}
