package org.messic.android.smarttv.activities.search;

import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.smarttv.activities.Presenter;

import rx.Observable;

public interface SearchFragmentPresenter extends Presenter {


    Observable<MDMSong> search(String content);

}
