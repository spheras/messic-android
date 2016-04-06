package org.messic.android.smarttv.activities.main.fragments;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.messic.android.smarttv.MessicSmarttvApp;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class MainFragmentPresenterImpl implements MainFragmentPresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;

    public MainFragmentPresenterImpl() {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
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
    public Observable<MDMRandomList> loadRandomPlaylists() {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMRandomList>() {
            @Override
            public void call(final Subscriber<? super MDMRandomList> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/randomlists?messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMRandomList[].class,
                        new UtilRestJSONClient.RestListener<MDMRandomList[]>() {
                            public void response(MDMRandomList[] response) {
                                for (int i = 0; i < response.length; i++) {
                                    subscriber.onNext(response[i]);
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
    public Observable<MDMAuthor> loadAuthors() {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMAuthor>() {
            @Override
            public void call(final Subscriber<? super MDMAuthor> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/authors?albumsInfo=true&songsInfo=false&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMAuthor[].class,
                        new UtilRestJSONClient.RestListener<MDMAuthor[]>() {
                            public void response(MDMAuthor[] response) {
                                for (int i = 0; i < response.length; i++) {
                                    subscriber.onNext(response[i]);
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
    public Observable<MDMAlbum> loadAuthorAlbums(final MDMAuthor author) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMAlbum>() {
            @Override
            public void call(final Subscriber<? super MDMAlbum> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/authors/" + author.getSid() + "?albumsInfo=true&songsInfo=true&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMAuthor.class,
                        new UtilRestJSONClient.RestListener<MDMAuthor>() {
                            public void response(MDMAuthor response) {
                                for (int i = 0; i < response.getAlbums().size(); i++) {
                                    MDMAlbum album = response.getAlbums().get(i);
                                    for (int j = 0; j < album.getSongs().size(); j++) {
                                        MDMSong song = album.getSongs().get(j);
                                        album.setAuthor(author);
                                        song.setAlbum(album);
                                    }
                                    subscriber.onNext(album);
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
    public Observable<MDMSong> loadAlbum(final MDMAlbum album) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/albums/" + album.getSid() + "?songsInfo=true&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMAlbum.class,
                        new UtilRestJSONClient.RestListener<MDMAlbum>() {
                            public void response(MDMAlbum response) {
                                for (int j = 0; j < response.getSongs().size(); j++) {
                                    MDMSong song = response.getSongs().get(j);
                                    song.setAlbum(response);
                                    subscriber.onNext(song);
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
