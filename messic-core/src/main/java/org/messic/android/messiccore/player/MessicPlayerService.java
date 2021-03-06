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
package org.messic.android.messiccore.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class MessicPlayerService
        extends Service {

    private final IBinder musicBind = new MusicBinder();
    @Inject
    UtilMusicPlayer ump;

    private MessicPlayerQueue playerqueue = null;

    public MessicPlayerQueue getPlayer() {
        return this.playerqueue;
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");
        super.onCreate();
        MessicCoreApp.getInstance().getComponent().inject(this);


    }

    @Override
    public void onDestroy() {
        Log.d("MessicPlayerService", "onDestroy");
        // Toast.makeText( this, "onDestroy", Toast.LENGTH_SHORT ).show();

        this.playerqueue.stop();
        this.playerqueue.clearQueue();
        this.playerqueue = null;

        IMessicPlayerNotification playerNotification = ump.getMessicPlayerNotification();
        playerNotification.cancel();
        ump.setMessicPlayerNotification(null);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MessicPlayerService", "onStartCommands");
        // Toast.makeText( this, "onStartCommands", Toast.LENGTH_SHORT ).show();

        IMessicPlayerNotification playerNotification = ump.getMessicPlayerNotification();
        if (playerNotification != null) {
            playerNotification.setService(this);

            if (this.playerqueue == null) {
                this.playerqueue = new MessicPlayerQueue();
                //this.playernotification = new MessicPlayerNotification( this, this.playerqueue );
                this.playerqueue.addListener(playerNotification);
                playerNotification.setPlayer(this.playerqueue);
            }

        } else {
            Log.d("MessicPlayerService", "null notification!!!");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("MessicPlayerService", "onBind");
        // Toast.makeText( this, "onBind", Toast.LENGTH_SHORT ).show();

        List<PlayerEventListener> listeners = this.playerqueue.getListeners();
        for (PlayerEventListener listener : listeners) {
            listener.connected();
        }

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("MessicPlayerService", "onUnbind");
        // Toast.makeText( this, "onUnbind", Toast.LENGTH_SHORT ).show();

        // playerqueue.stop();

        List<PlayerEventListener> listeners = this.playerqueue.getListeners();
        for (PlayerEventListener listener : listeners) {
            listener.disconnected();
        }

        return false;
    }

    public class MusicBinder
            extends Binder {
        public MessicPlayerService getService() {
            return MessicPlayerService.this;
        }
    }

}
