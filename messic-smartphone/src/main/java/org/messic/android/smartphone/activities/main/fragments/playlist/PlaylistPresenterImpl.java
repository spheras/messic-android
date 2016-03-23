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
package org.messic.android.smartphone.activities.main.fragments.playlist;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class PlaylistPresenterImpl implements PlaylistPresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;
    @Inject
    UtilMusicPlayer ump;

    public PlaylistPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Playlist Presenter");
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
    public Observable<MDMPlaylist> getPlaylists() {

        if (!config.isOffline()) {
            return getPlaylistOnline();
        } else {
            return null;
        }

    }

    @Override
    public void playAction(MDMSong song) {
        ump.addSong(song);
    }

    @Override
    public void longPlayAction(MDMSong song) {
        ump.addSongAndPlay(song);

    }

    @Override
    public Observable<MDMAlbum> authorAction(MDMSong song) {
        return getAlbum(song);
    }

    @Override
    public Observable<MDMAlbum> albumAction(MDMSong song) {

        return getAlbum(song);
    }

    @Override
    public Observable<MDMAlbum> songAction(MDMSong song) {
        return getAlbum(song);

    }

    public Observable<MDMAlbum> getAlbum(final MDMSong song) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMAlbum>() {
            @Override
            public void call(final Subscriber<? super MDMAlbum> subscriber) {

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
        });

    }

    @Override
    public void playlistAction(MDMPlaylist playlist) {
        ump.addPlaylist(playlist);
    }

    public Observable<MDMPlaylist> getPlaylistOnline() {
        return Observable.create(new Observable.OnSubscribe<MDMPlaylist>() {
            @Override
            public void call(final Subscriber<? super MDMPlaylist> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/playlists?songsInfo=true&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMPlaylist[].class,
                        new UtilRestJSONClient.RestListener<MDMPlaylist[]>() {
                            public void response(MDMPlaylist[] response) {
                                for (int i = 0; i < response.length; i++) {
                                    if (response[i] != null) {
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
}
