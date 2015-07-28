package org.messic.android.messiccore.player;

import android.app.Service;

import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;

/**
 * Created by spheras on 21/07/15.
 */
public interface IMessicPlayerNotification extends PlayerEventListener {
    String ACTION_BACK = "org.messic.android.action.MESSIC_BACK";
    String ACTION_PLAY = "org.messic.android.action.MESSIC_PLAY";
    String ACTION_PAUSE = "org.messic.android.action.MESSIC_PAUSE";
    String ACTION_NEXT = "org.messic.android.action.MESSIC_NEXT";
    String ACTION_CLOSE = "org.messic.android.action.MESSIC_CLOSE";
    String ACTION_ALBUM = "org.messic.android.action.MESSIC_ALBUM";

    void cancel();

    void setService(Service service);

    void setPlayer(MessicPlayerQueue player);

    void paused(MDMSong song, int index);

    void playing(MDMSong song, boolean resumed, int index);

    void completed(int index);

    void added(MDMSong song);

    void added(MDMAlbum album);

    void added(MDMPlaylist playlist);

    void disconnected();

    void connected();

    void removed(MDMSong song);

    void empty();
}
