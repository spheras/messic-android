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
package org.messic.android.smarttv.activities.login;

import android.os.AsyncTask;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMLogin;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilDatabase;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class LoginPresenterImpl implements LoginPresenter {
    @Inject
    DAOServerInstance daoServerInstance;

    @Inject
    Configuration config;
    @Inject
    UtilNetwork utilNetwork;
    @Inject
    MessicPreferences pref;
    @Inject
    UtilRestJSONClient urj;
    @Inject
    UtilDatabase ud;
    @Inject
    LoginEvents le;
    private MessicDiscovering service = null;

    public LoginPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmarttvApp app = MessicSmarttvApp.getSmarttvApp();
        if (app != null)
            app.getSmarttvComponent().inject(this);

        Timber.d("Creating a Login Presenter");
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

    public boolean fillUserPassword(LoginActivityBinding binding) {

        //filling the user password
        MDMMessicServerInstance instance = config.getCurrentMessicService();
        if (instance != null) {
            binding.setServername(instance.name);
            binding.setServerip(instance.ip);
            binding.setRemember(pref.getRemember());
            if (pref.getRemember()) {
                String user = config.getLastMessicUser();
                binding.setUsername((user != null ? user : ""));
                String password = config.getLastMessicPassword();
                binding.setPassword((password != null ? password : ""));
            }
            return true;
        } else {
            binding.setUsername("");
            binding.setPassword("");
            return false;
        }
    }


    public void searchOnlineAction() {
        le.sendShowSearchServerMessicScreen();
    }

    public void loginOfflineAction() {
        config.setOffline(true);
        le.sendShowMainScreen();
    }

    public void statusOnlineAction() {
        le.sendShowSearchServerMessicScreen();
    }

    /**
     * Fill the adapter with the stored messic services
     */
    public List<MDMMessicServerInstance> getSavedSessions() {
        return daoServerInstance.getAll();
    }

    public boolean loginAction(final MDMMessicServerInstance instance, final boolean remember, final String username, final String password) {
        utilNetwork.nukeNetwork();
        config.setMessicService(instance);

        final String baseURL = config.getBaseUrl() + "/messiclogin";
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.add("j_username", username);
        formData.add("j_password", password);

        try {
            MDMLogin response = urj.post(baseURL, formData, MDMLogin.class);
            pref.setRemember(remember, username, password, config.getCurrentMessicService());
            config.setToken(response.getMessic_token());

            config.setOffline(false);
            le.sendShowMainScreen();
            return true;
        } catch (Exception e) {
            //this is due to the fact that the server doesn't response a valid MDMLogin structure when not user/pass not valid
            //the JSON parser throw an exception instead when the post return this: {"success":false, "message": "Username/Password are invalid"}
            Timber.e("login", e.getMessage(), e);
            return false;
        }
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
}
