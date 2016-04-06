package org.messic.android.smarttv.activities.recommendations;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import org.messic.android.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.main.MainActivity;
import org.messic.android.smarttv.utils.PicassoMessicUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Fco Javier Coira on 25/08/2015.
 */
public class UpdateRecommendationsService extends IntentService {
    private static final String TAG = "MessicRecommendationSrv";
    private static final int MAX_RECOMMENDATIONS = 3;

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;

    private NotificationManager mNotificationManager;

    public UpdateRecommendationsService() {
        super("MESSIC RECOMMENDATIONS SERVICE");
        MessicSmarttvApp.getSmarttvApp().getSmarttvComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Updating recommendation cards");
        Observable<MDMRandomList> observable = loadRandomPlaylists();
        final List<MDMRandomList> rlist = new ArrayList<>();
        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMRandomList>() {
            @Override
            public void call(MDMRandomList randomList) {
                //on next
                rlist.add(randomList);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                //on error
            }
        }, new Action0() {
            @Override
            public void call() {
                //on complete
                updateRecommendations(rlist.toArray(new MDMRandomList[rlist.size()]));
            }
        });

    }

    public void updateRecommendations(MDMRandomList[] response) {

        if (response == null || response.length == 0) {
            return;
        }

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }

        int count = 0;
        try {
            Context aContext = getApplicationContext();
            RecommendationBuilder builder = new RecommendationBuilder()
                    .setContext(aContext)
                    .setSmallIcon(R.mipmap.ic_launcher);

            for (MDMSong aSong : response[0].getSongs()) {
                Log.d(TAG, "Recommendation - " + aSong.getName());

                Bitmap cover = PicassoMessicUtil.getCover(config, aContext, aSong);

                int notificationId = count + 1;
                Notification notification = builder.setBackground(PicassoMessicUtil.getCoverURL(config, aContext, aSong))
                        .setId(aSong.getSid())
                        .setPriority(MAX_RECOMMENDATIONS - count)
                        .setTitle(aSong.getName())
                        .setDescription(aSong.getAlbum().getName())
                        .setBitmap(cover)
                        .setIntent(buildPendingIntent(aSong, notificationId))
                        .build();

                mNotificationManager.notify(notificationId, notification);

                if (++count >= MAX_RECOMMENDATIONS) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to update recommendation", e);
        }
    }

    private PendingIntent buildPendingIntent(MDMSong aSong, int id) {
        Intent detailsIntent = new Intent(this, MainActivity.class);
        detailsIntent.putExtra(MainActivity.Song, aSong);
        detailsIntent.putExtra(MainActivity.NOTIFICATION_ID, id);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        //stackBuilder.addParentStack(MainFragment.class);
        stackBuilder.addNextIntent(detailsIntent);
        // Ensure a unique PendingIntents, otherwise all
        // recommendations end up with the same PendingIntent
        detailsIntent.setAction(Long.toString(aSong.getSid()));

        PendingIntent intent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }

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
}
