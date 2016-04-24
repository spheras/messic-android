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
package org.messic.android.messiccore.controllers;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.MessicPreferences;

import javax.inject.Inject;

public class Configuration {

    private static Configuration instance;

    @Inject
    MessicPreferences prefs;

    /**
     * flag to know if the static fields has been removed and need to be reloaded
     */
    private MDMMessicServerInstance serverInstance = null;
    private String lastToken = null;
    private boolean firstTime = false;
    private boolean offline;
    private LogoutListener logoutListener;

    //WARNING! this cannot be injected (or passed at construction) due to cyclic dependency, we inject it manually
    private DAOServerInstance daoServerInstance;

    private Configuration() {
        MessicCoreApp.getInstance().getComponent().inject(this);
        init();
    }

    public static Configuration get() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    /**
     * WARNING! this cannot be injected (or passed at construction) due to cyclic dependency, we inject it manually
     */
    public void injectManuallyDaoServerInstance(DAOServerInstance dao) {
        this.daoServerInstance = dao;
        init();
    }

    private void init() {
        lastToken = prefs.getCurrentToken();
        offline = prefs.getCurrentOffline();
        serverInstance = getLastMessicServerUsed();
    }

    public void setLogoutListener(LogoutListener listener) {
        this.logoutListener = listener;
    }

    public String getLastToken() {
        return lastToken;
    }

    public void logout() {
        setToken(null);
        if (this.logoutListener != null) {
            this.logoutListener.logout();
        }
    }

    public void setToken(String token) {
        lastToken = token;
        prefs.setCurrentToken(token);
    }

    public String getBaseUrl() {
        MDMMessicServerInstance instance = getCurrentMessicService();
        if (instance == null && !isOffline()) {
            // @TODO remove comment
            //Configuration.logout(ctx);
            return "http://localhost/messic";
        }
        return getBaseUrl(instance);
    }

    public String getBaseUrl(MDMMessicServerInstance instance) {
        return (instance.secured ? "https" : "http") + "://" + instance.ip + ":" + instance.port + "/messic";
    }

    public void setMessicService(MDMMessicServerInstance instance) {
        prefs.setLastMessicServerUsed(instance);
        this.serverInstance = instance;
    }

    public MDMMessicServerInstance getLastMessicServerUsed() {
        int serverSid = prefs.getLastMessicServerUsed();
        if (daoServerInstance != null) {
            return this.daoServerInstance.get(serverSid);
        } else {
            return null;
        }
    }

    public String getLastMessicUser() {
        if (serverInstance != null)
            return serverInstance.lastUser;
        else
            return null;
    }

    public String getLastMessicPassword() {
        if (serverInstance != null)
            return serverInstance.lastPassword;
        else
            return null;
    }

    /**
     * Obtain the messic server serverInstance used
     *
     * @return {@link String}
     */
    public MDMMessicServerInstance getCurrentMessicService() {
        return serverInstance;
    }

    /**
     * @return the offline
     */
    public boolean isOffline() {
        return offline;
    }

    /**
     * @param offline the offline to set
     */
    public void setOffline(boolean offline) {
        this.offline = offline;
        prefs.setCurrentOffline(offline);
    }

    /**
     * @return the firstTime
     */
    public boolean isFirstTime() {
        // we need to open the database to really check if it is the first time (the database will be created)
        DAOAlbum daoalbum = new DAOAlbum();
        daoalbum.open();
        daoalbum.close();
        return firstTime;
    }

    /**
     * @param firstTime the firstTime to set
     */
    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public interface LogoutListener {
        void logout();
    }

}
