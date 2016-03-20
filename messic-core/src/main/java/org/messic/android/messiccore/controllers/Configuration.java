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
    private Class loginActivityClass;
    private boolean offline;

    private Configuration() {
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public static Configuration get() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public void init() {
        lastToken = prefs.getCurrentToken();
        offline = prefs.getCurrentOffline();
        serverInstance = prefs.getLastMessicServerUsed();
    }


    public Class getLoginActivityClass() {
        return loginActivityClass;
    }

    public void setLoginActivityClass(Class loginActivityClass) {
        loginActivityClass = loginActivityClass;
    }

    public String getLastToken() {
        return lastToken;
    }

    public void logout() {
        setToken(null);

        /*
        @TODO OUT OF HERE!
        if (getLoginActivityClass() != null) {
            Intent intent = new Intent(context, getLoginActivityClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }


        if (context instanceof Activity) {
            ((Activity) context).finish(); // call this to finish the current activity
        }
        */
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
        MDMMessicServerInstance prefferedServer = prefs.getLastMessicServerUsed();
        serverInstance = prefferedServer;
        return serverInstance;
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

}
