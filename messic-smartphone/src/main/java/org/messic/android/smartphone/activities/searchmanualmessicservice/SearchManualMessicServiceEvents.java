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
package org.messic.android.smartphone.activities.searchmanualmessicservice;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.smartphone.rxevents.RxAction;
import org.messic.android.smartphone.rxevents.RxDispatcher;

import javax.inject.Inject;

public class SearchManualMessicServiceEvents {

    public static String EVENT_FINISH_ACTIVITY = "SEARCHMANUAL_FINISH_ACTIVITY";
    public static String EVENT_MANDATORY_FIELDS = "SEARCHMANUAL_MANDATORY_FIELDS";
    private static SearchManualMessicServiceEvents instance;

    @Inject
    RxDispatcher rxDispatcher;

    private SearchManualMessicServiceEvents() {
        MessicSmartphoneApp.getSmartphoneApp().getSmartphoneComponent().inject(this);
    }

    public static SearchManualMessicServiceEvents get() {
        if (instance == null) {
            instance = new SearchManualMessicServiceEvents();
        }
        return instance;
    }

    public void sendFinishActivity() {
        this.rxDispatcher.send(RxAction.create(SearchManualMessicServiceEvents.EVENT_FINISH_ACTIVITY).build());
    }

    public void sendMandatoryFields() {
        this.rxDispatcher.send(RxAction.create(SearchManualMessicServiceEvents.EVENT_MANDATORY_FIELDS).build());
    }

}
