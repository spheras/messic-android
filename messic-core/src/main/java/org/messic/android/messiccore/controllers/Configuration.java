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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.util.MessicPreferences;

public class Configuration {

    /**
     * flag to know if the static fields has been removed and need to be reloaded
     */
    private static Object objFlag = null;

    private static MDMMessicServerInstance instance = null;

    private static String lastToken = null;

    private static boolean firstTime = false;

    private static Class loginActivityClass;

    private static boolean offline;

    public static void setLoginActivityClass(Class loginActivityClass) {
        Configuration.loginActivityClass = loginActivityClass;
    }

    public static Class getLoginActivityClass() {
        return Configuration.loginActivityClass;
    }

    public static String getLastToken(Context ctx) {
        checkConfiguration(ctx);
        return lastToken;
    }

    /**
     * Check if the static values has been removed and need to be reloaded from preferences
     */
    private static void checkConfiguration(Context ctx) {
        if (objFlag == null) {
            objFlag = new Object();
            MessicPreferences mp = new MessicPreferences(ctx);
            lastToken = mp.getCurrentToken();
            offline = mp.getCurrentOffline();
        }
    }

    public static void logout(Context context) {
        Configuration.setToken(context, null);

        if (getLoginActivityClass() != null) {
            Intent intent = new Intent(context, getLoginActivityClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }


        if (context instanceof Activity) {
            ((Activity) context).finish(); // call this to finish the current activity
        }
    }


    public static void setToken(Context ctx, String token) {
        lastToken = token;
        new MessicPreferences(ctx).setCurrentToken(token);
    }

    public static String getBaseUrl(Context ctx) {
        checkConfiguration(ctx);

        MDMMessicServerInstance instance = getCurrentMessicService(ctx);
        if (instance == null && !Configuration.isOffline(ctx)) {
            // @TODO remove comment
            //Configuration.logout(ctx);
            return "http://localhost/messic";
        }
        return getBaseUrl(instance);
    }

    public static String getBaseUrl(MDMMessicServerInstance instance) {
        return (instance.secured ? "https" : "http") + "://" + instance.ip + ":" + instance.port + "/messic";
    }

    public static void setMessicService(Context context, MDMMessicServerInstance instance) {
        MessicPreferences mp = new MessicPreferences(context);
        mp.setLastMessicServerUsed(instance);
        Configuration.instance = instance;
    }

    public static MDMMessicServerInstance getLastMessicServerUsed(Context context) {
        MessicPreferences p = new MessicPreferences(context);
        MDMMessicServerInstance prefferedServer = p.getLastMessicServerUsed();
        instance = prefferedServer;
        return instance;
    }

    public static String getLastMessicUser(Context ctx) {
        checkConfiguration(ctx);

        if (instance != null)
            return instance.lastUser;
        else
            return null;
    }

    public static String getLastMessicPassword(Context ctx) {
        checkConfiguration(ctx);

        if (instance != null)
            return instance.lastPassword;
        else
            return null;
    }

    /**
     * Obtain the messic server instance used
     *
     * @return {@link String}
     */
    public static MDMMessicServerInstance getCurrentMessicService(Context ctx) {
        checkConfiguration(ctx);
        return instance;
    }

    /**
     * @return the offline
     */
    public static boolean isOffline(Context ctx) {
        checkConfiguration(ctx);

        return offline;
    }

    /**
     * @param offline the offline to set
     */
    public static void setOffline(Context ctx, boolean offline) {
        Configuration.offline = offline;
        new MessicPreferences(ctx).setCurrentOffline(offline);
    }

    /**
     * @return the firstTime
     */
    public static boolean isFirstTime(Context ctx) {
        // we need to open the database to really check if it is the first time (the database will be created)
        DAOAlbum daoalbum = new DAOAlbum(ctx);
        daoalbum.open();
        daoalbum.close();
        return firstTime;
    }

    /**
     * @param firstTime the firstTime to set
     */
    public static void setFirstTime(boolean firstTime) {
        Configuration.firstTime = firstTime;
    }

}
