/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.smartphone.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.smartphone.activities.login.LoginActivity;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.player.IMessicPlayerNotification;
import org.messic.android.messiccore.player.MessicPlayerQueue;
import org.messic.android.messiccore.util.AlbumCoverCache;
import org.messic.android.messiccore.util.UtilImage;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MessicPlayerNotification
        implements IMessicPlayerNotification {


    private static final int ONGOING_NOTIFICATION_ID = 7553;

    @Inject
    UtilMusicPlayer ump;

    @Inject
    AlbumCoverCache acc;


    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient jsonClient;

    // linked service
    private Service service;

    // to update the notification later on.
    private NotificationManager mNotificationManager;

    /**
     * current cover used at notification bar
     */
    private Bitmap currentNotificationCover = null;

    private Notification notification = null;

    private MessicPlayerQueue player;

    public MessicPlayerNotification() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicSmartphoneApp.getInstance()).getSmartphoneComponent().inject(this);
    }

    public MessicPlayerNotification(Service service, MessicPlayerQueue player) {
        this();
        this.player = player;
        this.service = service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setPlayer(MessicPlayerQueue player) {
        this.player = player;
    }

    @Override
    public void cancel() {
        if (this.notification != null) {
            this.mNotificationManager.cancelAll();
            this.mNotificationManager.cancel(ONGOING_NOTIFICATION_ID);
            this.service.stopForeground(true);
            this.notification = null;
            this.mNotificationManager = null;
        }
    }

    private void registerBroadcastActions() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BACK);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_CLOSE);
        filter.addAction(ACTION_ALBUM);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ACTION_BACK)) {
                    player.prevSong();
                } else if (intent.getAction().equals(ACTION_PLAY)) {
                    player.resumeSong();
                } else if (intent.getAction().equals(ACTION_PAUSE)) {
                    player.pauseSong();
                } else if (intent.getAction().equals(ACTION_NEXT)) {
                    player.nextSong();
                } else if (intent.getAction().equals(ACTION_CLOSE)) {
                    service.stopForeground(true);
                    service.unregisterReceiver(this);
                    mNotificationManager.cancel(ONGOING_NOTIFICATION_ID);

                    ump.clearAndStopAll();

                    Intent ssa = new Intent(MessicPlayerNotification.this.service, LoginActivity.class);
                    ssa.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ssa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MessicPlayerNotification.this.service.getApplication().startActivity(ssa);

                } else if (intent.getAction().equals(ACTION_ALBUM)) {

                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    context.sendBroadcast(it);

                    Observable<MDMAlbum> observable = getAlbum(player.getCurrentSong().getAlbum());

                    observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<MDMAlbum>() {
                        @Override
                        public void call(MDMAlbum mdmAlbum) {
                            Intent ssa = new Intent(MessicPlayerNotification.this.service, AlbumInfoActivity.class);
                            ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, mdmAlbum);
                            ssa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ssa.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            MessicPlayerNotification.this.service.getApplication().startActivity(ssa);
                        }
                    });


                }
            }
        };

        this.service.registerReceiver(receiver, filter);
    }


    public Observable<MDMAlbum> getAlbum(final MDMAlbum album) {
        //we need to request the full album info and load all the songs to show the info
        return Observable.create(new Observable.OnSubscribe<MDMAlbum>() {
            @Override
            public void call(final Subscriber<? super MDMAlbum> subscriber) {
                MDMAlbum result = album;
                if (result.getSongs().size() > 0) {
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } else if (album.isFlagFromLocalDatabase() && album.getSongs().size() == 0) {
                    DAOAlbum dao = new DAOAlbum();
                    dao.open();
                    result = dao.getByAlbumLSid(album.getLsid(), true);
                    dao.close();
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } else {

                    final String baseURL =
                            config.getBaseUrl() + "/services/albums/" + album.getSid()
                                    + "?songsInfo=true&authorInfo=true&messic_token=" + config.getLastToken();

                    jsonClient.get(baseURL, MDMAlbum.class,
                            new UtilRestJSONClient.RestListener<MDMAlbum>() {
                                public void response(MDMAlbum response) {
                                    for (int i = 0; i < response.getSongs().size(); i++) {
                                        MDMSong song = response.getSongs().get(i);
                                        song.setAlbum(response);
                                    }
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

    private void createNotification() {
        if (this.notification != null) {
            return;
        }

        mNotificationManager = (NotificationManager) this.service.getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews smallContentView =
                new RemoteViews(this.service.getPackageName(), R.layout.notification_small);
        RemoteViews bigContentView =
                new RemoteViews(this.service.getPackageName(), R.layout.notification_big);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.service);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle("title");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setContent(bigContentView);

        Notification n = mBuilder.build();

        n.contentView = smallContentView;
        n.bigContentView = bigContentView;

        Intent intentClose = new Intent(ACTION_CLOSE);
        PendingIntent pintentClose = PendingIntent.getBroadcast(this.service, 0, intentClose, 0);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_ivclose, pintentClose);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_ivclose, pintentClose);

        Intent intentBack = new Intent(ACTION_BACK);
        PendingIntent pintentBack = PendingIntent.getBroadcast(this.service, 0, intentBack, 0);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_ivback, pintentBack);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_ivback, pintentBack);

        Intent intentPlay = new Intent(ACTION_PLAY);
        PendingIntent pintentPlay = PendingIntent.getBroadcast(this.service, 0, intentPlay, 0);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_ivplay, pintentPlay);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_ivplay, pintentPlay);

        Intent intentPause = new Intent(ACTION_PAUSE);
        PendingIntent pintentPause = PendingIntent.getBroadcast(this.service, 0, intentPause, 0);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_ivpause, pintentPause);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_ivpause, pintentPause);

        Intent intentNext = new Intent(ACTION_NEXT);
        PendingIntent pintentNext = PendingIntent.getBroadcast(this.service, 0, intentNext, 0);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_ivnext, pintentNext);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_ivnext, pintentNext);

        Intent intentAlbum = new Intent(ACTION_ALBUM);
        PendingIntent pintentAlbum =
                PendingIntent.getBroadcast(this.service, 0, intentAlbum, PendingIntent.FLAG_CANCEL_CURRENT);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_ivcurrent_cover, pintentAlbum);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_tvcurrent_author, pintentAlbum);
        bigContentView.setOnClickPendingIntent(R.id.bignotification_tvcurrent_song, pintentAlbum);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_ivcurrent_cover, pintentAlbum);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_tvcurrent_author, pintentAlbum);
        smallContentView.setOnClickPendingIntent(R.id.bignotification_tvcurrent_song, pintentAlbum);

        this.notification = n;
        this.service.startForeground(ONGOING_NOTIFICATION_ID, notification);
        this.registerBroadcastActions();
    }

    private void refreshContentData(MDMSong song) {
        createNotification();
        this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                song.getAlbum().getAuthor().getName());
        this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_song, song.getName());

        this.notification.contentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                song.getAlbum().getAuthor().getName());
        this.notification.contentView.setTextViewText(R.id.bignotification_tvcurrent_song, song.getName());

        constructNotificationCover(song);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, this.notification);
    }

    @Override
    public void paused(MDMSong song, int index) {
        createNotification();
        this.notification.bigContentView.setViewVisibility(R.id.bignotification_ivplay, View.VISIBLE);
        this.notification.bigContentView.setViewVisibility(R.id.bignotification_ivpause, View.INVISIBLE);
        this.notification.contentView.setViewVisibility(R.id.bignotification_ivplay, View.VISIBLE);
        this.notification.contentView.setViewVisibility(R.id.bignotification_ivpause, View.INVISIBLE);
        refreshContentData(song);
    }

    @Override
    public void playing(MDMSong song, boolean resumed, int index) {
        createNotification();
        if (!resumed) {
            this.currentNotificationCover = null;
        }
        this.notification.bigContentView.setViewVisibility(R.id.bignotification_ivplay, View.INVISIBLE);
        this.notification.bigContentView.setViewVisibility(R.id.bignotification_ivpause, View.VISIBLE);
        this.notification.contentView.setViewVisibility(R.id.bignotification_ivplay, View.INVISIBLE);
        this.notification.contentView.setViewVisibility(R.id.bignotification_ivpause, View.VISIBLE);
        refreshContentData(song);
    }

    @Override
    public void completed(int index) {
        // TODO Auto-generated method stub

    }

    /**
     * Method to construct the notification cover
     *
     * @param playSong
     */
    private void constructNotificationCover(final MDMSong playSong) {
        createNotification();
        if (currentNotificationCover == null) {
            Bitmap cover =
                    acc.getCover(playSong.getAlbum(), new AlbumCoverCache.CoverListener() {

                        public void setCover(Bitmap bitmap) {
                            // we need to recreate the remote view due to memory buffer problems
                            // with remote views and the image
                            // we send
                            RemoteViews bigContentView =
                                    new RemoteViews(service.getPackageName(), R.layout.notification_big);
                            RemoteViews smallContentView =
                                    new RemoteViews(service.getPackageName(), R.layout.notification_small);

                            notification.bigContentView = bigContentView;
                            notification.contentView = smallContentView;

                            notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                                    playSong.getAlbum().getAuthor().getName());
                            notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_song,
                                    playSong.getName());
                            notification.contentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                                    playSong.getAlbum().getAuthor().getName());
                            notification.contentView.setTextViewText(R.id.bignotification_tvcurrent_song,
                                    playSong.getName());

                            Bitmap cover =
                                    UtilImage.resizeToNotificationImageSize(service.getApplicationContext(), bitmap, R.dimen.bignotification_cover_width, R.dimen.bignotification_cover_height);
                            notification.bigContentView.setImageViewBitmap(R.id.bignotification_ivcurrent_cover, cover);
                            notification.contentView.setImageViewBitmap(R.id.bignotification_ivcurrent_cover, cover);

                            currentNotificationCover = cover;
                            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
                        }

                        public void failed(Exception e) {
                            // TODO Auto-generated method stub

                        }
                    });
            if (cover != null) {
                // we need to recreate the remote view due to memory buffer problems with remote views and the image we
                // send
                RemoteViews bigContentView =
                        new RemoteViews(this.service.getPackageName(), R.layout.notification_big);
                RemoteViews smallContentView =
                        new RemoteViews(this.service.getPackageName(), R.layout.notification_small);
                notification.bigContentView = bigContentView;
                notification.contentView = smallContentView;
                this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                        playSong.getAlbum().getAuthor().getName());
                this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_song,
                        playSong.getName());
                this.notification.contentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                        playSong.getAlbum().getAuthor().getName());
                this.notification.contentView.setTextViewText(R.id.bignotification_tvcurrent_song,
                        playSong.getName());

                cover = UtilImage.resizeToNotificationImageSize(this.service.getApplicationContext(), cover, R.dimen.bignotification_cover_width, R.dimen.bignotification_cover_height);
                notification.bigContentView.setImageViewBitmap(R.id.bignotification_ivcurrent_cover, cover);
                notification.contentView.setImageViewBitmap(R.id.bignotification_ivcurrent_cover, cover);
                this.currentNotificationCover = cover;
            } else {
                notification.bigContentView.setImageViewResource(R.id.bignotification_ivcurrent_cover,
                        R.mipmap.ic_launcher);
                notification.contentView.setImageViewResource(R.id.bignotification_ivcurrent_cover,
                        R.mipmap.ic_launcher);
            }
        }
    }

    @Override
    public void added(MDMSong song) {
        // nothing to do
    }

    @Override
    public void added(MDMAlbum album) {
        // nothing to do
    }

    @Override
    public void added(MDMPlaylist playlist) {
        // nothing to do
    }

    @Override
    public void disconnected() {
        // nothing to do
    }

    @Override
    public void connected() {
        // nothing to do
    }

    @Override
    public void removed(MDMSong song) {
        if (this.player.getQueue().size() == 0) {
            cancel();
        }
    }

    @Override
    public void empty() {
        this.cancel();
    }
}
