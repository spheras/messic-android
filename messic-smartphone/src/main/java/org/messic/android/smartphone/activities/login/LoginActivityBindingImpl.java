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
package org.messic.android.smartphone.activities.login;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;

import org.messic.android.smartphone.utils.ObservableFieldCheckSync;
import org.messic.android.smartphone.utils.ObservableFieldStringSync;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class LoginActivityBindingImpl extends BaseObservable implements LoginActivityBinding {
    public ObservableField<String> username = new ObservableField<>();
    public ObservableFieldStringSync usernameSync = new ObservableFieldStringSync(username);
    public ObservableField<String> password = new ObservableField<>();
    public ObservableFieldStringSync passwordSync = new ObservableFieldStringSync(password);
    public ObservableField<Boolean> remember = new ObservableField<>();
    public ObservableFieldCheckSync rememberSync = new ObservableFieldCheckSync(remember);

    public ObservableField<String> serverName = new ObservableField<>();
    public ObservableField<String> serverIP = new ObservableField<>();


    @ParcelConstructor
    public LoginActivityBindingImpl() {
    }

    public LoginActivityBindingImpl(String username, String password, Boolean remember, String serverName, String serverIP) {
        this.username.set(username);
        this.password.set(password);
        this.remember.set(remember);
        this.serverName.set(serverName);
        this.serverIP.set(serverIP);
    }

    @Override
    public String getUsername() {
        return username.get();
    }

    @Override
    public void setUsername(String username) {
        this.username.set(username);
    }

    @Override
    public String getPassword() {
        return password.get();
    }

    @Override
    public void setPassword(String password) {
        this.password.set(password);
    }

    @Override
    public Boolean getRemember() {
        return remember.get();
    }

    @Override
    public void setRemember(Boolean remember) {
        this.remember.set(remember);
    }

    @Override
    public String getServername() {
        return serverName.get();
    }

    @Override
    public void setServername(String servername) {
        this.serverName.set(servername);
    }

    @Override
    public String getServerip() {
        return serverIP.get();
    }

    @Override
    public void setServerip(String serverip) {
        this.serverIP.set(serverip);
    }
}