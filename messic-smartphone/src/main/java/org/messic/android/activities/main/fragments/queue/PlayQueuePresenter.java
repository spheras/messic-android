package org.messic.android.activities.main.fragments.queue;

import org.messic.android.activities.Presenter;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;

import rx.Observable;

public interface PlayQueuePresenter extends Presenter {

    Observable<MDMSong> getQueueSongs();

    void playAction(int index);

    void authorAction(MDMSong song);

    void albumAction(MDMSong song);

    void songAction(MDMSong song);

    void removeAction(int index);

    Observable<MDMAlbum> getAlbum(final MDMAlbum album);
}
