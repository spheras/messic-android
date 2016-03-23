package org.messic.android.smartphone.views.player;

import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMSong;

import rx.Observable;

public interface PlayerPresenter {

    Observable<MDMAlbum> getAlbum(final MDMSong song);

}
