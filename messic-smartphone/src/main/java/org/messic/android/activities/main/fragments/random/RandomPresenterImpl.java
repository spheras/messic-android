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
package org.messic.android.activities.main.fragments.random;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.datamodel.dao.DAOSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class RandomPresenterImpl implements RandomPresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    RandomEvents events;

    public RandomPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Random Presenter");
    }

    public Observable<MDMSong> getRandomSongs() {
        if (config.isOffline()) {
            return getRandomOfflineSongs();
        } else {
            return getRandomOnlineSongs();
        }
    }

    @Override
    public void playAction(MDMSong song) {
        ump.addSong(song);
        events.sendSongAdded(song);
    }

    @Override
    public void longPlayAction(MDMSong song) {
        ump.addSongAndPlay(song);
        events.sendSongAdded(song);
    }

    @Override
    public Observable<MDMAlbum> authorAction(MDMSong song) {
        return getAlbum(song);
    }

    @Override
    public void albumAction(MDMSong song) {
//@TODO

    }

    @Override
    public void songAction(MDMSong song) {
//@TODO
    }

    public Observable<MDMSong> getRandomOfflineSongs() {
        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {

                DAOSong.SongPublisher p = new DAOSong.SongPublisher() {

                    public void publish(MDMSong song) {
                        subscriber.onNext(song);
                    }
                };

                DAOSong ds = new DAOSong();
                ds.getRandomSongs(45, p);
                subscriber.onCompleted();

            }
        });

    }

    public Observable<MDMAlbum> getAlbum(final MDMSong song) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMAlbum>() {
            @Override
            public void call(final Subscriber<? super MDMAlbum> subscriber) {
                MDMAlbum album = song.getAlbum();
                if (album.isFlagFromLocalDatabase()) {
                    DAOAlbum dao = new DAOAlbum();
                    dao.open();
                    song.setAlbum(dao.getByAlbumLSid(album.getLsid(), true));
                    dao.close();
                    subscriber.onNext(song.getAlbum());
                    subscriber.onCompleted();
                } else {

                    final String baseURL =
                            config.getBaseUrl() + "/services/albums/" + song.getAlbum().getSid()
                                    + "?songsInfo=true&authorInfo=true&messic_token=" + config.getLastToken();

                    jsonClient.get(baseURL, MDMAlbum.class,
                            new UtilRestJSONClient.RestListener<MDMAlbum>() {
                                public void response(MDMAlbum response) {
                                    for (int i = 0; i < response.getSongs().size(); i++) {
                                        MDMSong song = response.getSongs().get(i);
                                        song.setAlbum(response);
                                    }
                                    song.setAlbum(response);
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

    public Observable<MDMSong> getRandomOnlineSongs() {

        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/randomlists?filterRandomListName=RandomListName-Random&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMRandomList[].class,
                        new UtilRestJSONClient.RestListener<MDMRandomList[]>() {
                            public void response(MDMRandomList[] response) {
                                for (int i = 0; i < response.length; i++) {
                                    if (response[i] != null) {
                                        List<MDMSong> songs = response[i].getSongs();
                                        for (MDMSong song : songs) {
                                            subscriber.onNext(song);
                                        }
                                    }
                                }
                                subscriber.onCompleted();
                            }

                            public void fail(final Exception e) {
                                subscriber.onError(e);
                            }
                        });
            }
        });


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
