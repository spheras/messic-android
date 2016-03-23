package org.messic.android.smartphone.views.player;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class PlayerPresenterImpl implements PlayerPresenter {
    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;
    @Inject
    UtilMusicPlayer ump;

    public PlayerPresenterImpl() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a Player Presenter");
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

}
