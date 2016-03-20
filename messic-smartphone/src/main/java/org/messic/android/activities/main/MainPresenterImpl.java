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
package org.messic.android.activities.main;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.activities.main.fragments.random.RandomSongAdapter;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class MainPresenterImpl implements MainPresenter {

    @Inject
    Configuration config;
    @Inject
    UtilDownloadService uds;
    @Inject
    RandomSongAdapter randomAdapter;
    @Inject
    UtilMusicPlayer ump;

    public MainPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Random Presenter");

    }

    public void addRandomSongsToPlaylist() {
        List<MDMSong> songs = randomAdapter.getSongs();
        ump.addSongsAndPlay(songs);
    }

    public void clearQueue() {
        ump.clearQueue();
    }

    public void logout() {
        config.setToken(null);
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
    public Observable<Void> emptyDatabase() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                uds.emptyLocal();
                subscriber.onCompleted();
            }
        });

    }
}
