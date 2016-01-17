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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;

public class MessicPreferences
{
    public static final String PREFERENCE_LAST_MESSIC_SERVER = "LAST_MESSIC_SERVER";

    public static final String PREFERENCE_MESSIC_SERVER_REMEMBER = "MESSIC_SERVER_REMEMBER";

    public static final String PREFERENCE_MESSIC_CURRENT_OFFLINE = "MESSIC_SERVER_CURRENT_OFFILINE";

    public static final String PREFERENCE_MESSIC_CURRENT_TOKEN = "MESSIC_SERVER_CURRENT_TOKEN";

    private SharedPreferences sp = null;

    private Context context;

    public MessicPreferences( Context context )
    {
        this.sp = PreferenceManager.getDefaultSharedPreferences( context );
        this.context = context;
    }

    public void setLastMessicServerUsed( MDMMessicServerInstance msi )
    {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt( PREFERENCE_LAST_MESSIC_SERVER, msi.lsid );
        editor.commit();
    }

    public MDMMessicServerInstance getLastMessicServerUsed()
    {
        int sid = this.sp.getInt(PREFERENCE_LAST_MESSIC_SERVER, 0);
        if ( sid > 0 )
        {
            DAOServerInstance dao = new DAOServerInstance( context );
            return dao.get( sid );
        }
        else
        {
            return null;
        }
    }

    public boolean getCurrentOffline(){
        return this.sp.getBoolean( PREFERENCE_MESSIC_CURRENT_OFFLINE, false );
    }
    public void setCurrentOffline(boolean offline){
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean( PREFERENCE_MESSIC_CURRENT_OFFLINE, offline );
        editor.commit();
    }

    public String getCurrentToken(){
        return this.sp.getString( PREFERENCE_MESSIC_CURRENT_TOKEN, "" );
    }
    public void setCurrentToken(String token){
        SharedPreferences.Editor editor = sp.edit();
        editor.putString( PREFERENCE_MESSIC_CURRENT_TOKEN, token );
        editor.commit();
    }

    public boolean getRemember()
    {
        return this.sp.getBoolean( PREFERENCE_MESSIC_SERVER_REMEMBER, false );
    }

    public void setRemember( Context ctx, boolean remember, String username, String password )
    {
        SharedPreferences.Editor editor = sp.edit();
        if ( remember )
        {
            editor.putBoolean( PREFERENCE_MESSIC_SERVER_REMEMBER, true );
            MDMMessicServerInstance instance = Configuration.getCurrentMessicService(ctx);
            instance.lastUser = username;
            instance.lastPassword = password;
            DAOServerInstance dao = new DAOServerInstance( context );
            dao.save( instance );
        }
        else
        {
            editor.putBoolean( PREFERENCE_MESSIC_SERVER_REMEMBER, false );
            MDMMessicServerInstance instance = Configuration.getCurrentMessicService(ctx);
            instance.lastUser = "";
            instance.lastPassword = "";
            DAOServerInstance dao = new DAOServerInstance( context );
            dao.save( instance );
        }
        editor.commit();
    }
}
