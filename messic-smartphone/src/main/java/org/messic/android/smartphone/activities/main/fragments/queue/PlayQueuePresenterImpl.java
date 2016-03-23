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
package org.messic.android.smartphone.activities.main.fragments.queue;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.smartphone.activities.main.fragments.random.RandomEvents;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class PlayQueuePresenterImpl implements PlayQueuePresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    RandomEvents events;

    public PlayQueuePresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a PlayQueue Presenter");
    }

    @Override
    public Observable<MDMSong> getQueueSongs() {
        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {
                List<MDMSong> queue = ump.getQueue();
                for (MDMSong song : queue) {
                    subscriber.onNext(song);
                }
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public void playAction(int index) {
        ump.setSong(index);
        ump.playSong();
    }

    @Override
    public void removeAction(int index) {
        ump.removeSong(index);
    }

    @Override
    public void authorAction(MDMSong song) {
//@TODO
    }

    @Override
    public void albumAction(MDMSong song) {
//@TODO

    }

    @Override
    public void songAction(MDMSong song) {
//@TODO
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

    public Observable<MDMAlbum> getAlbum(final MDMAlbum album) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMAlbum>() {
            @Override
            public void call(final Subscriber<? super MDMAlbum> subscriber) {
                if (album.getSongs().size() > 0) {
                    subscriber.onNext(album);
                    subscriber.onCompleted();
                } else if (album.isFlagFromLocalDatabase()) {
                    DAOAlbum dao = new DAOAlbum();
                    dao.open();
                    MDMAlbum result = dao.getByAlbumLSid(album.getLsid(), true);
                    dao.close();
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } else {

                    final String baseURL =
                            config.getBaseUrl() + "/services/albums/" + album.getSid()
                                    + "?songsInfo=true&authorInfo=true&messic_token=" + config.getLastToken();

                    jsonClient.get(baseURL, MDMAlbum.class,
                            new UtilRestJSONClient.RestListener<MDMAlbum>() {
                                public void response(MDMAlbum response) {
                                    for (int i = 0; i < response.getSongs().size(); i++) {
                                        MDMSong song = response.getSongs().get(i);
                                        song.setAlbum(response);
                                    }
                                    subscriber.onNext(response);
                                    subscriber.onCompleted();
                                }

                                public void fail(final Exception e) {
                                    subscriber.onError(e);
                                }
                            });
                }
            }
        });

    }
}
