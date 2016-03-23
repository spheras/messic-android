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
package org.messic.android.smartphone;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import org.messic.android.smartphone.dagger2.ApplicationSmartphoneComponent;
//important to import (it doesn't matter that it gives you error at the ide)
import org.messic.android.smartphone.dagger2.DaggerApplicationSmartphoneComponent;
import org.messic.android.smartphone.dagger2.SmartphoneModule;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.dagger2.ApplicationCoreComponent;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smartphone.notifications.DownloadNotification;
import org.messic.android.smartphone.notifications.MessicPlayerNotification;

import javax.inject.Inject;

public class MessicSmartphoneApp extends MessicCoreApp {

    @Inject
    UtilMusicPlayer ump;
    @Inject
    UtilDownloadService uds;

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        private boolean flagPlaying = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                //Incoming call: Pause music
                if (ump.isPlaying()) {
                    ump.pauseSong();
                    flagPlaying = true;
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                //Not in call: Play music
                if (flagPlaying) {
                    flagPlaying = false;
                    ump.resumeSong();
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                //A call is dialing, active or on hold
                if (ump.isPlaying()) {
                    ump.pauseSong();
                    flagPlaying = true;
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };
    private ApplicationSmartphoneComponent smartphoneComponent;

    public static MessicSmartphoneApp getSmartphoneApp() {
        return (MessicSmartphoneApp) getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform injection so that when this call returns all dependencies will be available for use.
        this.getSmartphoneComponent().inject(this);

        startServices();
    }

    @Override
    public ApplicationCoreComponent initializeDependencyInjector() {
        //the error "cannot resolve symbol..." is solved after the compilation (dagger compiler will create this class)
        this.smartphoneComponent = DaggerApplicationSmartphoneComponent.builder()
                .smartphoneModule(new SmartphoneModule(this)).build();
        return this.smartphoneComponent;
    }

    public ApplicationSmartphoneComponent smartphoneComponent() {
        return this.smartphoneComponent;
    }

    protected void startServices() {
        ump.startMessicMusicService(MessicPlayerNotification.class);
        uds.startDownloadService(new DownloadNotification());
        listenToPhoneStatus();
    }

    public ApplicationSmartphoneComponent getSmartphoneComponent() {
        return this.smartphoneComponent;
    }

    private void listenToPhoneStatus() {
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

    }

}
