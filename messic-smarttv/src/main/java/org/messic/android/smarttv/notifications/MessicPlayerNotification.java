package org.messic.android.smarttv.notifications;

import android.app.Service;

import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.IMessicPlayerNotification;
import org.messic.android.messiccore.player.MessicPlayerQueue;

public class MessicPlayerNotification
        implements IMessicPlayerNotification {

    @Override
    public void cancel() {

    }

    @Override
    public void setService(Service service) {

    }

    @Override
    public void setPlayer(MessicPlayerQueue player) {

    }

    @Override
    public void paused(MDMSong song, int index) {

    }

    @Override
    public void playing(MDMSong song, boolean resumed, int index) {

    }

    @Override
    public void completed(int index) {

    }

    @Override
    public void added(MDMSong song) {

    }

    @Override
    public void added(MDMAlbum album) {

    }

    @Override
    public void added(MDMPlaylist playlist) {

    }

    @Override
    public void disconnected() {

    }

    @Override
    public void connected() {

    }

    @Override
    public void removed(MDMSong song) {

    }

    @Override
    public void empty() {

    }
}

