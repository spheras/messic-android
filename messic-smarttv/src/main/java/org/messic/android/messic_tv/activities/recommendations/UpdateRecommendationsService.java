package org.messic.android.messic_tv.activities.recommendations;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import org.messic.android.messic_tv.R;
import org.messic.android.messic_tv.activities.MainActivity;
import org.messic.android.messic_tv.controllers.RandomListsController;
import org.messic.android.messic_tv.util.PicassoMessicUtil;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;

/**
 * Created by Fco Javier Coira on 25/08/2015.
 */
public class UpdateRecommendationsService extends IntentService {
    private static final String TAG = "MessicRecommendationSrv";
    private static final int MAX_RECOMMENDATIONS = 3;

    private NotificationManager mNotificationManager;

    public UpdateRecommendationsService() {
        super("MESSIC RECOMMENDATIONS SERVICE");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Updating recommendation cards");
        new RandomListsController().loadRecommendations(this);
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
                    .setSmallIcon(R.drawable.ic_launcher);

            for (MDMSong aSong : response[0].getSongs()) {
                Log.d(TAG, "Recommendation - " + aSong.getName());

                Bitmap cover = PicassoMessicUtil.getCover(aContext, aSong);

                int notificationId = count + 1;
                Notification notification = builder.setBackground(PicassoMessicUtil.getCoverURL(aContext, aSong))
                        .setId(aSong.getSid())
                        .setPriority(MAX_RECOMMENDATIONS - count)
                        .setTitle(aSong.getName())
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
}
