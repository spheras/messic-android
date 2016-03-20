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
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMLogin;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilDatabase;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import timber.log.Timber;

public class LoginPresenterImpl implements LoginPresenter {

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

    public LoginPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Login Presenter");
    }

    /**
     * select what components should be visible or not depending on different reasons
     *
     * @return ShowControl
     */
    public ShowControl selectLayout(LoginActivityBinding binding) {
        ShowControl result = new ShowControl();

        if (config.isFirstTime()) {
            config.setFirstTime(false);
            result.showWelcomeActivity = true;
            return result;
        }

        boolean flagShowOnline = false;
        boolean flagShowOffline = false;

        // 1. is there a last messic server used?
        MDMMessicServerInstance prefferedServer = config.getLastMessicServerUsed();
        if (prefferedServer != null && prefferedServer.ip.trim().length() > 0) {
            // we show the online login against this last server
            result.showSearchOnline = false;
            result.showLoginOnline = true;
            flagShowOnline = true;

            // 1.1 Is this last server used online?
            utilNetwork.checkMessicServerUpAndRunning(prefferedServer, AndroidSchedulers.mainThread(),
                    // On Next
                    new Action1<UtilNetwork.MessicServerConnectionStatus>() {
                        @Override
                        public void call(UtilNetwork.MessicServerConnectionStatus result1) {
                            le.sendServerStatus(result1);
                        }
                    },

                    // On Error
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.e("error", throwable);
                            throwable.printStackTrace();
                            UtilNetwork.MessicServerConnectionStatus ss = new UtilNetwork.MessicServerConnectionStatus(false, false);
                            le.sendServerStatus(ss);
                        }
                    },

                    // On Complete
                    new Action0() {
                        @Override
                        public void call() {
                            Timber.d("MessicServerConnectionStatus request completed");
                        }
                    });
        } else {
            result.showLoginOnline = false;
            result.showSearchOnline = true;
        }

        // 2. is there offline music available?
        if (ud.checkEmptyDatabase()) {
            // yes! no offline option to play music
            result.showLoginOffline = false;
        } else {
            // no!, let's show the offline button
            result.showLoginOffline = true;
            flagShowOffline = true;
        }

        if (!result.showLoginOffline && !result.showLoginOnline) {
            result.showSearchActivity = true;
            return result; //we don't want to show nothing
        }

        if (result.showLoginOnline) {
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
            }
        }

        return result;
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


    public boolean loginAction(final boolean remember, final String username, final String password) {
        utilNetwork.nukeNetwork();

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
