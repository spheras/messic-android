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
package org.messic.android.activities.main.fragments.downloaded;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.activities.main.fragments.random.RandomEvents;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class DownloadedPresenterImpl implements DownloadedPresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;
    @Inject
    UtilMusicPlayer ump;
    @Inject
    RandomEvents events;
    @Inject
    UtilDownloadService uds;

    public DownloadedPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Random Presenter");
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
    public void initialize() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public Observable<MDMAlbum> getDownloadedAlbums() {
        return Observable.create(new Observable.OnSubscribe<MDMAlbum>() {
            @Override
            public void call(final Subscriber<? super MDMAlbum> subscriber) {

                DAOAlbum.AlbumPublisher p = new DAOAlbum.AlbumPublisher() {

                    public void publish(MDMAlbum album) {
                        subscriber.onNext(album);
                    }
                };

                DAOAlbum da = new DAOAlbum();
                List<MDMAlbum> albums = da.getAllByAuthor(p);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public void playAction(MDMAlbum album) {
        ump.addAlbum(album);
    }

    @Override
    public void longPlayAction(MDMAlbum album) {
        List<MDMSong> songs = album.getSongs();
        List<MDMSong> fsongs = new ArrayList<MDMSong>();
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).isDownloaded(config)) {
                MDMSong song = songs.get(i);
                song.setAlbum(album);
                fsongs.add(songs.get(i));
            }
        }

        ump.addSongsAndPlay(fsongs);
    }

    @Override
    public void authorAction(MDMAlbum album) {

    }

    @Override
    public Observable<MDMSong> removeAlbum(final MDMAlbum album) {
        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {
                uds.removeAlbum(album);
                List<MDMSong> songs = album.getSongs();
                for (int i = 0; i < songs.size(); i++) {
                    MDMSong song = songs.get(i);
                    song.setLfileName(null);
                }
                subscriber.onCompleted();

            }
        });
    }
}
