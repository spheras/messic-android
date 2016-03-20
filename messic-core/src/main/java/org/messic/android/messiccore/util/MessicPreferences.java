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
package org.messic.android.messiccore.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;

import java.io.Serializable;

public class MessicPreferences implements Serializable {
    public static final String PREFERENCE_LAST_MESSIC_SERVER = "LAST_MESSIC_SERVER";

    public static final String PREFERENCE_MESSIC_SERVER_REMEMBER = "MESSIC_SERVER_REMEMBER";

    public static final String PREFERENCE_MESSIC_CURRENT_OFFLINE = "MESSIC_SERVER_CURRENT_OFFILINE";

    public static final String PREFERENCE_MESSIC_CURRENT_TOKEN = "MESSIC_SERVER_CURRENT_TOKEN";

    public static final String PREFERENCE_MESSIC_LAST_NOTIFICATION_CLASS = "MESSIC_SERVER_LAST_NOTIFICATION_CLASS";
    public static final String PREFERENCE_MESSIC_LAST_SERVICE_CLASS = "MESSIC_SERVER_LAST_SERVICE_CLASS";

    private static MessicPreferences instance = null;

    /*
    WARNING: DON'T INJECT CONFIG IN THIS CLASES DUE ITS DEPENDENCY WITH CONFIGURATION (AVOID CIRCULAR REFERENCES)
    @Inject
    Configuration configs;
    */

    private SharedPreferences sp = null;


    private MessicPreferences() {
        this.sp = PreferenceManager.getDefaultSharedPreferences(MessicCoreApp.getInstance());
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public static MessicPreferences get() {
        if (instance == null) {
            instance = new MessicPreferences();
        }
        return instance;
    }

    public String getLastMessicNotificationClassUsed() {
        return this.sp.getString(PREFERENCE_MESSIC_LAST_NOTIFICATION_CLASS, "");
    }

    public void setLastMessicNotificationClassUsed(String lastNotificationClass) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_MESSIC_LAST_NOTIFICATION_CLASS, lastNotificationClass);
        editor.commit();
    }

    public String getLastMessicServiceClassUsed() {
        return this.sp.getString(PREFERENCE_MESSIC_LAST_SERVICE_CLASS, "");
    }

    public void setLastMessicServiceClassUsed(String lastServiceClass) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_MESSIC_LAST_SERVICE_CLASS, lastServiceClass);
        editor.commit();
    }

    public MDMMessicServerInstance getLastMessicServerUsed() {
        int sid = this.sp.getInt(PREFERENCE_LAST_MESSIC_SERVER, 0);
        if (sid > 0) {
            DAOServerInstance dao = new DAOServerInstance();
            return dao.get(sid);
        } else {
            return null;
        }
    }

    public void setLastMessicServerUsed(MDMMessicServerInstance msi) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREFERENCE_LAST_MESSIC_SERVER, msi.lsid);
        editor.commit();
    }

    public boolean getCurrentOffline() {
        return this.sp.getBoolean(PREFERENCE_MESSIC_CURRENT_OFFLINE, false);
    }

    public void setCurrentOffline(boolean offline) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREFERENCE_MESSIC_CURRENT_OFFLINE, offline);
        editor.commit();
    }

    public String getCurrentToken() {
        return this.sp.getString(PREFERENCE_MESSIC_CURRENT_TOKEN, "");
    }

    public void setCurrentToken(String token) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_MESSIC_CURRENT_TOKEN, token);
        editor.commit();
    }

    public boolean getRemember() {
        return this.sp.getBoolean(PREFERENCE_MESSIC_SERVER_REMEMBER, false);
    }

    public void setRemember(boolean remember, String username, String password, MDMMessicServerInstance currentMessicService) {
        SharedPreferences.Editor editor = sp.edit();
        if (remember) {
            editor.putBoolean(PREFERENCE_MESSIC_SERVER_REMEMBER, true);
            currentMessicService.lastUser = username;
            currentMessicService.lastPassword = password;
            DAOServerInstance dao = new DAOServerInstance();
            dao.save(currentMessicService);
        } else {
            editor.putBoolean(PREFERENCE_MESSIC_SERVER_REMEMBER, false);
            currentMessicService.lastUser = "";
            currentMessicService.lastPassword = "";
            DAOServerInstance dao = new DAOServerInstance();
            dao.save(currentMessicService);
        }
        editor.commit();
    }
}
