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
package org.messic.android.smartphone.activities.searchmessicservice;

import org.messic.android.smartphone.activities.Presenter;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;

import java.util.List;

public interface SearchMessicServicePresenter extends Presenter {
    boolean isSwipeable(MDMMessicServerInstance msi);

    void swipe(MDMMessicServerInstance msi);

    ShowControl selectLayout();

    void manualSearchAction();

    void loginOfflineAction();

    void searchMessicServices(final MessicDiscovering.SearchListener sl);

    void cancelSearch();

    List<MDMMessicServerInstance> getSavedSessions();

    SearchMessicServicePresenterImpl.InstanceClickActionCommand instanceClickAction(MDMMessicServerInstance instance);

    class ShowControl {
        public boolean showLoginOffline;
    }
}
