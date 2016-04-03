package org.messic.android.smarttv.activities.main.fragments;

import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.smarttv.activities.Presenter;

import rx.Observable;

public interface MainFragmentPresenter extends Presenter {


    Observable<MDMRandomList> loadRandomPlaylists();

    Observable<MDMAuthor> loadAuthors();
}
