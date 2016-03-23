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
package org.messic.android.smarttv;

import org.messic.android.smarttv.dagger2.ApplicationSmarttvComponent;
import org.messic.android.smarttv.dagger2.DaggerApplicationSmarttvComponent;
import org.messic.android.smarttv.dagger2.SmarttvModule;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.dagger2.ApplicationCoreComponent;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.notifications.MessicPlayerNotification;

import javax.inject.Inject;

//important to import (it doesn't matter that it gives you error at the ide)

public class MessicSmarttvApp extends MessicCoreApp {

    @Inject
    UtilMusicPlayer ump;
    @Inject
    UtilDownloadService uds;


    private ApplicationSmarttvComponent smarttvComponent;

    public static MessicSmarttvApp getSmarttvApp() {
        return (MessicSmarttvApp) getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform injection so that when this call returns all dependencies will be available for use.
        this.getSmarttvComponent().inject(this);

        startServices();
    }

    @Override
    public ApplicationCoreComponent initializeDependencyInjector() {
        //the error "cannot resolve symbol..." is solved after the compilation (dagger compiler will create this class)
        this.smarttvComponent = DaggerApplicationSmarttvComponent.builder()
                .smarttvModule(new SmarttvModule(this)).build();
        return this.smarttvComponent;
    }

    public ApplicationSmarttvComponent smarttvComponent() {
        return this.smarttvComponent;
    }

    protected void startServices() {
        ump.startMessicMusicService(MessicPlayerNotification.class);
    }

    public ApplicationSmarttvComponent getSmarttvComponent() {
        return this.smarttvComponent;
    }


}
