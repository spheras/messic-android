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
package org.messic.android.activities.searchmanualmessicservice;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;

import javax.inject.Inject;

import timber.log.Timber;

public class SearchManualMessicServicePresenterImpl implements SearchManualMessicServicePresenter {


    @Inject
    DAOServerInstance dao;
    @Inject
    SearchManualMessicServiceEvents events;

    public SearchManualMessicServicePresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a SearchManualMessicService Presenter");
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

    @Override
    public void saveAction(SearchManualMessicServiceActivityBinding binding) {
        if (checkMandatoryFields(binding)) {

            dao.open();

            MDMMessicServerInstance msi = new MDMMessicServerInstance();
            msi.name = binding.getName();
            msi.ip = binding.getHostname();
            msi.secured = binding.getSecured();
            msi.description = binding.getDescription();
            msi.port = binding.getIntPort();
            dao.save(msi);

            dao.close();

            events.sendFinishActivity();
        } else {
            events.sendMandatoryFields();
        }
    }

    private boolean checkMandatoryFields(SearchManualMessicServiceActivityBinding binding) {
        if (binding.getName().length() > 0 && binding.getHostname().length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void cancelAction() {
        //it is directly in the activity, forgive me !!
    }
}
