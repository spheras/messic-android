package org.messic.android.smartphone.activities.albuminfo;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.download.DownloadListener;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import timber.log.Timber;

public class AlbumInfoPresenterImpl implements AlbumInfoPresenter {

    @Inject
    UtilMusicPlayer ump;
    @Inject
    UtilDownloadService uds;

    public AlbumInfoPresenterImpl() {

        // Perform injection so that when this call returns all dependencies will be available for use.
        MessicSmartphoneApp app = MessicSmartphoneApp.getSmartphoneApp();
        if (app != null)
            app.getSmartphoneComponent().inject(this);

        Timber.d("Creating a AlbumInfoPresenter Presenter");
    }

    @Override
    public Observable<MDMSong> getAlbumSongs(MDMAlbum album) {
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
    public void initialize() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void playAction(MDMSong song) {
        ump.addSong(song);
    }

    @Override
    public void playAction(MDMAlbum album) {
        ump.addAlbum(album);
    }

    @Override
    public void longPlayAction(MDMAlbum album) {
        ump.addSongsAndPlay(album.getSongs());
    }

    @Override
    public void longPlayAction(MDMSong song) {
        ump.addSongAndPlay(song);
    }

    @Override
    public Observable<Boolean> removeAction(MDMSong song) {
        return Observable.just(song).map(new Func1<MDMSong, Boolean>() {
            @Override
            public Boolean call(MDMSong song) {
                boolean result = uds.removeSong(song);
                return result;
            }
        });
    }

    @Override
    public Observable<MDMSong> downloadAction(final MDMAlbum album) {
        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            private int downloads = 0;

            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {
                uds.addDownload(album, new DownloadListener() {
                    public void downloadUpdated(MDMSong song, float percent) {
                    }

                    public void downloadStarted(MDMSong song) {

                    }

                    public void downloadFinished(MDMSong song, File fdownloaded) {
                        subscriber.onNext(song);
                        downloads++;

                        if (downloads == album.getSongs().size()) {
                            subscriber.onCompleted();
                        }
                    }

                    public void downloadAdded(MDMSong song) {
                    }

                    public void disconnected() {
                    }

                    public void connected() {
                    }
                });
            }
        });
    }


    @Override
    public Observable<Float> downloadAction(final MDMSong song) {

        return Observable.create(new Observable.OnSubscribe<Float>() {
            @Override
            public void call(final Subscriber<? super Float> subscriber) {
                uds.addDownload(song, new DownloadListener() {
                    public void downloadUpdated(MDMSong song, float percent) {
                        subscriber.onNext(percent);
                    }

                    public void downloadStarted(MDMSong song) {

                    }

                    public void downloadFinished(MDMSong song, File fdownloaded) {
                        subscriber.onCompleted();
                    }

                    public void downloadAdded(MDMSong song) {
                    }

                    public void disconnected() {
                    }

                    public void connected() {
                    }
                });
            }
        });

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
