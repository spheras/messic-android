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
package org.messic.android.activities.main.fragments.random;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;

import javax.inject.Inject;

public class RandomEvents {

    public static String EVENT_SONG_ADDED = "RANDOM_SONG_ADDED";
    public static String EVENT_SHOW_SCREEN = "RANDOM_EVENT_SHOW_SCREEN";
    public static String SCREEN_MAIN = "RANDOM_SCREEN_ALBUM";
    private static RandomEvents instance;

    @Inject
    RxDispatcher rxDispatcher;

    private RandomEvents() {
        MessicSmartphoneApp.getSmartphoneApp().getSmartphoneComponent().inject(this);
    }

    public static RandomEvents get() {
        if (instance == null) {
            instance = new RandomEvents();
        }
        return instance;
    }

    public void sendSongAdded(MDMSong song) {
        this.rxDispatcher.send(RxAction.create(RandomEvents.EVENT_SONG_ADDED).bundle(RandomEvents.EVENT_SONG_ADDED, song).build());
    }

    public void sendShowMainScreen() {
        this.rxDispatcher.send(RxAction.create(RandomEvents.EVENT_SHOW_SCREEN).bundle(RandomEvents.EVENT_SHOW_SCREEN, SCREEN_MAIN).build());
    }
}
