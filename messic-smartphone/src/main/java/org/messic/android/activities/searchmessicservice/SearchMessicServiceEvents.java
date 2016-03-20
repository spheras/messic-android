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
package org.messic.android.activities.searchmessicservice;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;

import java.util.List;

import javax.inject.Inject;

public class SearchMessicServiceEvents {

    public static String EVENT_SHOW_SCREEN = "SEARCH_EVENT_SHOW_SCREEN";
    public static String EVENT_ADD_INSTANCES = "SEARCH_ADD_INSTANCES";
    public static String SCREEN_MANUAL_SEARCH = "SEARCH_SCREEN_MANUALSEARCH";
    public static String SCREEN_LOGINOFFLINE = "SEARCH_SCREEN_LOGINOFFLINE";
    private static SearchMessicServiceEvents instance;

    @Inject
    RxDispatcher rxDispatcher;

    private SearchMessicServiceEvents() {
        MessicSmartphoneApp.getSmartphoneApp().getSmartphoneComponent().inject(this);
    }

    public static SearchMessicServiceEvents get() {
        if (instance == null) {
            instance = new SearchMessicServiceEvents();
        }
        return instance;
    }

    public void sendShowLoginOffline() {
        this.rxDispatcher.send(RxAction.create(SearchMessicServiceEvents.EVENT_SHOW_SCREEN).bundle(SearchMessicServiceEvents.EVENT_SHOW_SCREEN, SCREEN_LOGINOFFLINE).build());
    }

    public void sendShowManualSearch() {
        this.rxDispatcher.send(RxAction.create(SearchMessicServiceEvents.EVENT_SHOW_SCREEN).bundle(SearchMessicServiceEvents.EVENT_SHOW_SCREEN, SCREEN_MANUAL_SEARCH).build());
    }

    public void sendInstances(List<MDMMessicServerInstance> servers) {
        this.rxDispatcher.send(RxAction.create(SearchMessicServiceEvents.EVENT_ADD_INSTANCES).bundle(SearchMessicServiceEvents.EVENT_ADD_INSTANCES, servers).build());
    }
}
