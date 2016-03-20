package org.messic.android.activities.main.fragments.downloaded;

import org.messic.android.activities.Presenter;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;

import rx.Observable;

public interface DownloadedPresenter extends Presenter {

    Observable<MDMAlbum> getDownloadedAlbums();


    void playAction(MDMAlbum album);

    void longPlayAction(MDMAlbum album);

    void authorAction(MDMAlbum album);

    Observable<MDMSong> removeAlbum(final MDMAlbum album);
}
