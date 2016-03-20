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
package org.messic.android.activities.searchmessicservice;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.messic.android.R;
import org.messic.android.databinding.ActivitySearchmessicserviceItemBinding;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.util.UtilNetwork;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class SearchMessicServiceAdapter extends RecyclerView.Adapter<SearchMessicServiceItemViewHolder> {
    SearchMessicServiceItemViewHolder.IViewHolderClicks listener;
    private List<MDMMessicServerInstance> instances = new ArrayList<MDMMessicServerInstance>();
    private ActivitySearchmessicserviceItemBinding binding;

    public SearchMessicServiceAdapter(@NonNull SearchMessicServiceItemViewHolder.IViewHolderClicks listener) {
        this.listener = listener;
    }

    public void updateAdapter() {
        notifyDataSetChanged();
    }

    public SearchMessicServiceItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.activity_searchmessicservice_item, parent, false);
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_searchmessicservice_item, null);
        return new SearchMessicServiceItemViewHolder(layoutView, this.listener);
    }

    public void onBindViewHolder(final SearchMessicServiceItemViewHolder holder, int position) {

        holder.vstatus.setBackgroundColor(Color.YELLOW);
        final MDMMessicServerInstance msi = this.instances.get(position);
        holder.hostname.setText(msi.name);
        holder.ip.setText(msi.ip);
        holder.version.setText(msi.version);
        holder.version.setTag(position);


        UtilNetwork.get().checkMessicServerUpAndRunning(msi, AndroidSchedulers.mainThread(),
                // On Next
                new Action1<UtilNetwork.MessicServerConnectionStatus>() {
                    @Override
                    public void call(UtilNetwork.MessicServerConnectionStatus result1) {
                        if (result1.reachable && result1.running) {
                            msi.setLastCheckedStatus(MDMMessicServerInstance.STATUS_RUNNING);
                            holder.vstatus.setBackgroundColor(Color.GREEN);
                        } else {
                            msi.setLastCheckedStatus(MDMMessicServerInstance.STATUS_DOWN);
                            holder.vstatus.setBackgroundColor(Color.RED);
                        }
                    }
                },

                // On Error
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        msi.setLastCheckedStatus(MDMMessicServerInstance.STATUS_DOWN);
                        holder.vstatus.setBackgroundColor(Color.RED);
                    }
                },

                // On Complete
                new Action0() {
                    @Override
                    public void call() {
                    }
                }
        );

    }

    public void clear() {
        instances = new ArrayList<MDMMessicServerInstance>();
    }

    public int getCount() {
        return instances.size();
    }

    public List<MDMMessicServerInstance> getInstances() {
        return instances;
    }

    public MDMMessicServerInstance getItem(int count) {
        if (count < this.instances.size()) {
            return this.instances.get(count);
        } else {
            return null;
        }
    }

    public int getItemCount() {
        return this.instances.size();
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
    }

    public boolean addInstance(MDMMessicServerInstance instance) {
        for (int i = 0; i < instances.size(); i++) {
            MDMMessicServerInstance md = instances.get(i);
            if (md.ip.equals(instance.ip) && md.port == instance.port && md.secured == instance.secured) {
                if (md.lastCheckedStatus == MDMMessicServerInstance.STATUS_RUNNING) {
                    return false;
                } else {
                    md.setLastCheckedStatus(MDMMessicServerInstance.STATUS_RUNNING);
                    notifyDataSetChanged();
                    return false;
                }
            }
        }
        this.instances.add(instance);
        return true;
    }
}
