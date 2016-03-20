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
package org.messic.android.activities.main.fragments.explore;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class ExplorePresenterImpl implements ExplorePresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    UtilDownloadService uds;

    public ExplorePresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Explore Presenter");
    }

    public Observable<MDMAuthor> getAuthors() {
        if (!config.isOffline()) {
            return getExploreOnlineAuthors();
        } else {
            return null;
        }
    }

    public Observable<MDMAuthor> getExploreOnlineAuthors() {

        return Observable.create(new Observable.OnSubscribe<MDMAuthor>() {
            @Override
            public void call(final Subscriber<? super MDMAuthor> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/authors?albumsInfo=false&songsInfo=false&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMAuthor[].class,
                        new UtilRestJSONClient.RestListener<MDMAuthor[]>() {
                            public void response(MDMAuthor[] response) {
                                for (int i = 0; i < response.length; i++) {
                                    if (response[i] != null) {
                                        response[i].flagFullInfoServer = false;
                                        subscriber.onNext(response[i]);
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


    @Override
    public void playAction(MDMAlbum album) {
        ump.addAlbum(album);
    }

    @Override
    public void longPlayAction(MDMAlbum album) {
        List<MDMSong> songs = album.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            MDMSong song = songs.get(i);
            song.setAlbum(album);
        }
        ump.addSongsAndPlay(album.getSongs());
    }


}
