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
package org.messic.android.messic_tv.activities.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.messic.android.messic_tv.R;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.util.UtilNetwork;

import java.util.ArrayList;
import java.util.List;

public class SearchMessicServiceAdapter
        extends BaseAdapter {
    public static final String HIGHLIGHTED_ITEM_COLOR = "#2200FF00";
    private List<MDMMessicServerInstance> instances = new ArrayList<MDMMessicServerInstance>();

    private LayoutInflater inflater = null;

    private int selectedItem = -1;

    private Activity parentActivity;

    public interface SearchMessicServiceAdapterListener {
        void selected(MDMMessicServerInstance instance);
    }

    private SearchMessicServiceAdapterListener listener;

    public SearchMessicServiceAdapter(Activity context, SearchMessicServiceAdapterListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.parentActivity = context;
    }

    public void clear() {
        instances = new ArrayList<MDMMessicServerInstance>();
    }

    public int getCount() {
        return instances.size();
    }

    public int getSelected() {
        return selectedItem;
    }

    public void select(int i) {
        if (instances.size() >= i + 1) {
            this.selectedItem = i;
            this.listener.selected(instances.get(i));
        } else {
            this.selectedItem = -1;
        }

        notifyDataSetChanged();
    }

    public List<MDMMessicServerInstance> getInstances() {
        return instances;
    }

    public Object getItem(int count) {
        if (count < this.instances.size()) {
            return this.instances.get(count);
        } else {
            return null;
        }
    }

    public long getItemId(int arg0) {
        return arg0;
    }

    public boolean existInstance(MDMMessicServerInstance instance) {
        for (MDMMessicServerInstance i : instances) {
            if (i.lastCheckedStatus == MDMMessicServerInstance.STATUS_RUNNING) {
                if (i.ip.equalsIgnoreCase(instance.ip) && i.name.equalsIgnoreCase(instance.name)
                        && i.version.equalsIgnoreCase(instance.version) && i.port == instance.port
                        && i.secured == instance.secured) {
                    return true;
                }
            }
        }

        return false;
    }

    public void removeItem(int count) {
        this.instances.remove(count);

        if (count == selectedItem) {
            selectedItem = -1;
        }
    }

    public boolean addInstance(final MDMMessicServerInstance instance) {
        for (int i = 0; i < instances.size(); i++) {
            MDMMessicServerInstance md = instances.get(i);
            if (md.ip.equals(instance.ip) && md.port == instance.port && md.secured == instance.secured) {
                if (md.lastCheckedStatus == MDMMessicServerInstance.STATUS_RUNNING) {
                    return false;
                } else {
                    md.setLastCheckedStatus(MDMMessicServerInstance.STATUS_RUNNING);
                    return false;
                }
            }
        }
        this.instances.add(instance);


        // start checking the availability
        UtilNetwork.MessicServerStatusListener listener = new UtilNetwork.MessicServerStatusListener() {
            public void setResponse(final boolean reachable, final boolean running) {
                if (reachable && running) {
                    instance.setLastCheckedStatus(MDMMessicServerInstance.STATUS_RUNNING);
                } else {
                    instance.setLastCheckedStatus(MDMMessicServerInstance.STATUS_DOWN);
                }

                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        };
        UtilNetwork.checkMessicServerUpAndRunning(instance, listener);

        if (this.instances.size() == 1) {
            select(0);
        }

        return true;
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View counterView, final ViewGroup parent) {
        if (counterView == null) {
            counterView = this.inflater.inflate(R.layout.search_messic_service_item, null);
        }

        TextView hostname = (TextView) counterView.findViewById(R.id.searchmessicservice_item_hostname);
        TextView ip = (TextView) counterView.findViewById(R.id.searchmessicservice_item_ip);
        TextView version = (TextView) counterView.findViewById(R.id.searchmessicservice_item_version);
        final View vstatus = (View) counterView.findViewById(R.id.searchmessicservice_item_hostname_vstatus);

        vstatus.setBackgroundColor(Color.YELLOW);
        final MDMMessicServerInstance msi = this.instances.get(position);
        hostname.setText(msi.name);
        ip.setText(msi.ip);
        version.setText(msi.version);
        version.setTag(position);
        final View parentView = counterView;

        if (selectedItem == position) {
            counterView.setBackgroundColor(Color.parseColor(HIGHLIGHTED_ITEM_COLOR));

        } else {
            counterView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (msi.getLastCheckedStatus() == MDMMessicServerInstance.STATUS_RUNNING) {
            vstatus.setBackgroundColor(Color.GREEN);
        } else if (msi.getLastCheckedStatus() == MDMMessicServerInstance.STATUS_DOWN) {
            vstatus.setBackgroundColor(Color.RED);
        } else if (msi.getLastCheckedStatus() == MDMMessicServerInstance.STATUS_DOWN) {
            vstatus.setBackgroundColor(Color.YELLOW);
        }

        final View fcounterview = counterView;
        counterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView lv = (ListView) parent.findViewById(R.id.login_lvresults);
                lv.requestFocus();

                selectedItem = position;
                listener.selected(msi);
                fcounterview.setBackgroundColor(Color.parseColor(HIGHLIGHTED_ITEM_COLOR));

                notifyDataSetChanged();
            }
        });


        return counterView;
    }
}
