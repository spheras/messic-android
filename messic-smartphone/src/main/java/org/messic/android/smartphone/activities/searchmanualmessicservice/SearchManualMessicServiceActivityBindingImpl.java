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
package org.messic.android.smartphone.activities.searchmanualmessicservice;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;

import org.messic.android.smartphone.utils.ObservableFieldCheckSync;
import org.messic.android.smartphone.utils.ObservableFieldStringSync;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class SearchManualMessicServiceActivityBindingImpl extends BaseObservable implements SearchManualMessicServiceActivityBinding {
    public ObservableField<String> name = new ObservableField<String>();
    public ObservableFieldStringSync nameSync = new ObservableFieldStringSync(name);
    public ObservableField<String> hostname = new ObservableField<String>();
    public ObservableFieldStringSync hostnameSync = new ObservableFieldStringSync(hostname);
    public ObservableField<Boolean> secured = new ObservableField<>();
    public ObservableFieldCheckSync securedSync = new ObservableFieldCheckSync(secured);
    public ObservableField<String> port = new ObservableField<>();
    public ObservableFieldStringSync portSync = new ObservableFieldStringSync(port);
    public ObservableField<String> description = new ObservableField<>();
    public ObservableFieldStringSync descriptionSync = new ObservableFieldStringSync(description);

    @ParcelConstructor
    public SearchManualMessicServiceActivityBindingImpl() {
    }

    public SearchManualMessicServiceActivityBindingImpl(String name, String hostname, boolean secured, int port, String description) {
        setName(name);
        setHostname(hostname);
        setSecured(secured);
        setPort("" + port);
        setDescription(description);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    public String getHostname() {
        return this.hostname.get();
    }

    @Override
    public void setHostname(String hostname) {
        this.hostname.set(hostname);
    }

    @Override
    public String getPort() {
        return port.get();
    }

    @Override
    public void setPort(String port) {
        this.port.set(port);
    }

    @Override
    public int getIntPort() {
        return Integer.valueOf(getPort());
    }

    @Override
    public Boolean getSecured() {
        return secured.get();
    }

    @Override
    public void setSecured(Boolean secured) {
        this.secured.set(secured);
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public void setDescription(String description) {
        this.description.set(description);
    }
}