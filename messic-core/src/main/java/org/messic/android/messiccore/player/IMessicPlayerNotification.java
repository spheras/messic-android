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

import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;

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
