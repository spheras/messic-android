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
package org.messic.android.activities.login;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;

import javax.inject.Inject;

public class LoginEvents {

    public static String EVENT_FINISH_ACTIVITY = "LOGIN_FINISH";
    public static String EVENT_SERVER_STATUS = "LOGIN_EVENT_SERVER_STATUS";
    public static String EVENT_SHOW_SCREEN = "LOGIN_EVENT_SHOW_SCREEN";
    public static String SCREEN_SEARCH_MESSSIC_SERVICE = "LOGIN_SCREEN_SEARCHMESSICSERVICE";
    public static String SCREEN_MAIN = "LOGIN_SCREEN_MAIN";
    private static LoginEvents instance;

    @Inject
    RxDispatcher rxDispatcher;

    private LoginEvents() {
        MessicSmartphoneApp.getSmartphoneApp().getSmartphoneComponent().inject(this);
    }

    public static LoginEvents get() {
        if (instance == null) {
            instance = new LoginEvents();
        }
        return instance;
    }

    public void sendFinishActivity() {
        this.rxDispatcher.send(RxAction.create(LoginEvents.EVENT_FINISH_ACTIVITY).build());
    }

    public void sendServerStatus(UtilNetwork.MessicServerConnectionStatus mscs) {
        this.rxDispatcher.send(RxAction.create(LoginEvents.EVENT_SERVER_STATUS).bundle(LoginEvents.EVENT_SERVER_STATUS, mscs).build());
    }

    public void sendShowSearchServerMessicScreen() {
        this.rxDispatcher.send(RxAction.create(LoginEvents.EVENT_SHOW_SCREEN).bundle(LoginEvents.EVENT_SHOW_SCREEN, SCREEN_SEARCH_MESSSIC_SERVICE).build());
    }

    public void sendShowMainScreen() {
        this.rxDispatcher.send(RxAction.create(LoginEvents.EVENT_SHOW_SCREEN).bundle(LoginEvents.EVENT_SHOW_SCREEN, SCREEN_MAIN).build());
    }
}
