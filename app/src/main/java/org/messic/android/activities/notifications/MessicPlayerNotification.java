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
package org.messic.android.activities.notifications;

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

import org.messic.android.R;
import org.messic.android.activities.AlbumInfoActivity;
import org.messic.android.activities.LoginActivity;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.IMessicPlayerNotification;
import org.messic.android.messiccore.player.MessicPlayerQueue;
import org.messic.android.messiccore.util.AlbumCoverCache;
import org.messic.android.messiccore.util.UtilImage;
import org.messic.android.messiccore.util.UtilMusicPlayer;

public class MessicPlayerNotification
        implements IMessicPlayerNotification {
    private static final int ONGOING_NOTIFICATION_ID = 7553;

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

    }

    public MessicPlayerNotification(Service service, MessicPlayerQueue player) {
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

                    UtilMusicPlayer.clearAndStopAll(context);

                    Intent ssa = new Intent(MessicPlayerNotification.this.service, LoginActivity.class);
                    ssa.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ssa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MessicPlayerNotification.this.service.getApplication().startActivity(ssa);
                } else if (intent.getAction().equals(ACTION_ALBUM)) {
                    Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                    context.sendBroadcast(it);

                    Intent ssa = new Intent(MessicPlayerNotification.this.service, AlbumInfoActivity.class);
                    ssa.putExtra(AlbumInfoActivity.EXTRA_ALBUM_SID, player.getCurrentSong().getAlbum());
                    ssa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ssa.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    MessicPlayerNotification.this.service.getApplication().startActivity(ssa);

                }
            }
        };

        this.service.registerReceiver(receiver, filter);
    }

    private void createNotification() {
        if (this.notification != null) {
            return;
        }

        mNotificationManager = (NotificationManager) this.service.getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews contentView =
                new RemoteViews(this.service.getPackageName(), R.layout.bignotification_player_layout);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.service);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("title");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setContent(contentView);

        Notification n = mBuilder.build();
        n.bigContentView = contentView;

        Intent intentClose = new Intent(ACTION_CLOSE);
        PendingIntent pintentClose = PendingIntent.getBroadcast(this.service, 0, intentClose, 0);
        contentView.setOnClickPendingIntent(R.id.bignotification_ivclose, pintentClose);
        Intent intentBack = new Intent(ACTION_BACK);
        PendingIntent pintentBack = PendingIntent.getBroadcast(this.service, 0, intentBack, 0);
        contentView.setOnClickPendingIntent(R.id.bignotification_ivback, pintentBack);
        Intent intentPlay = new Intent(ACTION_PLAY);
        PendingIntent pintentPlay = PendingIntent.getBroadcast(this.service, 0, intentPlay, 0);
        contentView.setOnClickPendingIntent(R.id.bignotification_ivplay, pintentPlay);
        Intent intentPause = new Intent(ACTION_PAUSE);
        PendingIntent pintentPause = PendingIntent.getBroadcast(this.service, 0, intentPause, 0);
        contentView.setOnClickPendingIntent(R.id.bignotification_ivpause, pintentPause);
        Intent intentNext = new Intent(ACTION_NEXT);
        PendingIntent pintentNext = PendingIntent.getBroadcast(this.service, 0, intentNext, 0);
        contentView.setOnClickPendingIntent(R.id.bignotification_ivnext, pintentNext);
        Intent intentAlbum = new Intent(ACTION_ALBUM);
        PendingIntent pintentAlbum =
                PendingIntent.getBroadcast(this.service, 0, intentAlbum, PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.bignotification_ivcurrent_cover, pintentAlbum);
        contentView.setOnClickPendingIntent(R.id.bignotification_tvcurrent_author, pintentAlbum);
        contentView.setOnClickPendingIntent(R.id.bignotification_tvcurrent_song, pintentAlbum);

        this.notification = n;
        this.service.startForeground(ONGOING_NOTIFICATION_ID, notification);
        this.registerBroadcastActions();
    }

    private void refreshContentData(MDMSong song) {
        createNotification();
        this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                song.getAlbum().getAuthor().getName());
        this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_song, song.getName());
        constructNotificationCover(song);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, this.notification);
    }

    @Override
    public void paused(MDMSong song, int index) {
        createNotification();
        this.notification.bigContentView.setViewVisibility(R.id.bignotification_ivplay, View.VISIBLE);
        this.notification.bigContentView.setViewVisibility(R.id.bignotification_ivpause, View.INVISIBLE);
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
                    AlbumCoverCache.getCover(this.service, playSong.getAlbum(), new AlbumCoverCache.CoverListener() {

                        public void setCover(Bitmap bitmap) {
                            // we need to recreate the remote view due to memory buffer problems
                            // with remote views and the image
                            // we send
                            RemoteViews contentView =
                                    new RemoteViews(service.getPackageName(), R.layout.bignotification_player_layout);
                            notification.bigContentView = contentView;
                            notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                                    playSong.getAlbum().getAuthor().getName());
                            notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_song,
                                    playSong.getName());

                            Bitmap cover =
                                    UtilImage.resizeToNotificationImageSize(service.getApplicationContext(), bitmap, R.dimen.bignotification_cover_width, R.dimen.bignotification_cover_height);
                            notification.bigContentView.setImageViewBitmap(R.id.bignotification_ivcurrent_cover, cover);
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
                RemoteViews contentView =
                        new RemoteViews(this.service.getPackageName(), R.layout.bignotification_player_layout);
                notification.bigContentView = contentView;
                this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_author,
                        playSong.getAlbum().getAuthor().getName());
                this.notification.bigContentView.setTextViewText(R.id.bignotification_tvcurrent_song,
                        playSong.getName());

                cover = UtilImage.resizeToNotificationImageSize(this.service.getApplicationContext(), cover, R.dimen.bignotification_cover_width, R.dimen.bignotification_cover_height);
                notification.bigContentView.setImageViewBitmap(R.id.bignotification_ivcurrent_cover, cover);
                this.currentNotificationCover = cover;
            } else {
                notification.bigContentView.setImageViewResource(R.id.bignotification_ivcurrent_cover,
                        R.drawable.ic_launcher);
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
