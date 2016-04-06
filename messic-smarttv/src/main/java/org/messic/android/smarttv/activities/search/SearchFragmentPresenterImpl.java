package org.messic.android.smarttv.activities.search;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.messic.android.smarttv.MessicSmarttvApp;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

public class SearchFragmentPresenterImpl implements SearchFragmentPresenter {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;

    public SearchFragmentPresenterImpl() {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
    }

    @Override
    public Observable<MDMSong> search(final String content) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMSong>() {
            @Override
            public void call(final Subscriber<? super MDMSong> subscriber) {

                final String baseURL =
                        config.getBaseUrl()
                                + "/services/search?content=" + content + "&messic_token="
                                + config.getLastToken();

                jsonClient.get(baseURL, MDMRandomList.class,
                        new UtilRestJSONClient.RestListener<MDMRandomList>() {
                            public void response(MDMRandomList response) {
                                List<MDMSong> songs = response.getSongs();
                                for (int i = 0; i < songs.size(); i++) {
                                    MDMSong song = songs.get(i);
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

    @Override
    public void initialize() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }
}
