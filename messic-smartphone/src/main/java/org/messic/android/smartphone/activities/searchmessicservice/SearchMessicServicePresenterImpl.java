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

import android.os.AsyncTask;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.UtilDatabase;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class SearchMessicServicePresenterImpl implements SearchMessicServicePresenter {

    @Inject
    DAOServerInstance daoServerInstance;
    @Inject
    UtilDatabase ud;
    @Inject
    Configuration config;
    @Inject
    SearchMessicServiceEvents smsevents;


    private MessicDiscovering service = null;


    public SearchMessicServicePresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a SearchMessicService Presenter");
    }

    @Override
    public void initialize() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    public boolean isSwipeable(MDMMessicServerInstance msi) {
        return !(msi.getLastCheckedStatus() == MDMMessicServerInstance.STATUS_RUNNING);
    }

    public void swipe(MDMMessicServerInstance msi) {
        daoServerInstance.remove(msi);
    }

    public ShowControl selectLayout() {
        ShowControl sc = new ShowControl();
        sc.showLoginOffline = !ud.checkEmptyDatabase();
        return sc;
    }

    public void manualSearchAction() {
        smsevents.sendShowManualSearch();
    }

    public void loginOfflineAction() {
        config.setOffline(true);
        smsevents.sendShowLoginOffline();
    }

    /**
     * Function to search messic services over the network. This function sends a broadcast over the network to find any
     * messic service
     *
     * @param sl {@link MessicDiscovering.SearchListener} to search events
     */
    public void searchMessicServices(final MessicDiscovering.SearchListener sl) {
        AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (service != null) {
                    //cancelling previously
                    service.cancel();
                }
                service = new MessicDiscovering();
                service.searchMessicServices(sl);
                return null;
            }
        };
        at.execute();
    }


    /**
     * Cancel the service searching a messic service
     */
    public void cancelSearch() {
        if (service != null) {
            service.cancel();
        }
    }

    /**
     * Fill the adapter with the stored messic services
     */
    public List<MDMMessicServerInstance> getSavedSessions() {
        return daoServerInstance.getAll();
    }


    public InstanceClickActionCommand instanceClickAction(MDMMessicServerInstance instance) {
        if (!(instance.getLastCheckedStatus() == MDMMessicServerInstance.STATUS_DOWN)) {
            instance = daoServerInstance.save(instance);
            config.setMessicService(instance);
            return new ShowLoginActivity() {
            };
        } else {
            return new ShowToastServiceDown() {
            };
        }
    }

    public static interface InstanceClickActionCommand {
    }

    public static interface ShowLoginActivity extends InstanceClickActionCommand {
    }

    public static interface ShowToastServiceDown extends InstanceClickActionCommand {
    }

}
