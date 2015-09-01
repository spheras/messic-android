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
package org.messic.android.messic_tv.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.messic_tv.R;
import org.messic.android.messic_tv.util.PicassoMessicUtil;
import org.messic.android.messic_tv.util.UtilMessic;
import org.messic.android.messic_tv.util.Utils;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.MessicPlayerService;
import org.messic.android.messiccore.player.PlayerEventListener;
import org.messic.android.messiccore.util.UtilMusicPlayer;

public class MessicPlayerTVService
        extends MessicPlayerService {

    private static final String SRV_LOG_NAME = "MESSIC-PLAYER-SRV";

    private MediaSession mSession;

    private MediaSession.Token sessionToken;

    @Override
    public void onCreate() {
        Log.d(SRV_LOG_NAME, "onCreate");
        // Toast.makeText( this, "onCreate", Toast.LENGTH_SHORT ).show();
        super.onCreate();

        mSession = new MediaSession(this, "Messic TV Service");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        sessionToken = mSession.getSessionToken();
    }

    @Override
    public void onDestroy() {
        releaseSession();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        getPlayer().addListener(new PlayerListener());
        return result;
    }


    private class MediaSessionCallback extends MediaSession.Callback {
        @Override
        public void onPlay() {
            getPlayer().playSong();
        }

        @Override
        public void onPause() {
            getPlayer().pauseSong();
        }

        @Override
        public void onSkipToNext() {
            getPlayer().nextSong();
        }

        @Override
        public void onSkipToPrevious() {
            getPlayer().prevSong();
        }
    }

    private class FocusAudioListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                Log.e(SRV_LOG_NAME, "audio focus lost");
                getPlayer().stop();
                UtilMusicPlayer.stopMessicMusicService(getApplicationContext());
            }
        }
    }

    private class PlayerListener implements PlayerEventListener {

        @Override
        public void paused(MDMSong song, int index) {
            if (mSession.isActive()) {
                updateMetadataInfo(song, getApplicationContext());
            }
        }

        @Override
        public void playing(MDMSong song, boolean resumed, int index) {
            Context mContext = getApplicationContext();
            if (!mSession.isActive()) {

                AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                // Request audio focus for playback
                int result = am.requestAudioFocus(new FocusAudioListener(),
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                    mSession.setActive(true);
                }
            }

            if (mSession.isActive()) {
                updateMetadataInfo(song, mContext);
            }
        }

        @Override
        public void completed(int index) {
            Log.d(SRV_LOG_NAME, "messic list played");
            releaseSession();
        }

        @Override
        public void added(MDMSong song) {

        }

        @Override
        public void added(MDMAlbum album) {

        }

        @Override
        public void added(MDMPlaylist playlist) {

        }

        @Override
        public void removed(MDMSong song) {

        }

        @Override
        public void empty() {
            Log.d(SRV_LOG_NAME, "empty list, releasing session");
            releaseSession();
        }

        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {
            Log.d(SRV_LOG_NAME, "messic service disconnected");
            releaseSession();
        }
    }

    private void releaseSession() {
        if (mSession.isActive()) {
            mSession.setActive(false);
            mSession.release();
        }
    }

    private void updateMetadataInfo(MDMSong song, Context mContext) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE,
                song.getAlbum().getName());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
                song.getName());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, song.getName());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, song.getAlbum().getName());
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, song.getAlbum().getAuthor().getName());

        try {
            String coverOnlineURL = PicassoMessicUtil.getCoverURL(mContext, song);
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, coverOnlineURL);
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, coverOnlineURL);

            Target metadataTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Context mContext = getApplicationContext();
                    Bitmap bm_ic_pause = BitmapFactory.decodeResource(mContext.getResources(), getPlayer().isPlaying() ? R.drawable.ic_play_arrow_white_48dp : R.drawable.ic_pause_white_48dp);

                    Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
                    Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas c = new Canvas(mutableBitmap);
                    int l = bitmap.getWidth() / 2 - bitmap.getWidth() / 4;
                    int t = bitmap.getHeight() / 2 - bitmap.getHeight() / 4;

                    Rect src = new Rect(0, 0, bm_ic_pause.getWidth(), bm_ic_pause.getHeight());
                    Rect dst = new Rect(l, t, l + bitmap.getWidth() / 2, t + bitmap.getHeight() / 2);
                    Paint aPaint = new Paint();
                    aPaint.setAlpha(60);
                    c.drawBitmap(bm_ic_pause, src, dst, aPaint);

                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON, mutableBitmap);
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, mutableBitmap);
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, mutableBitmap);
                    mSession.setMetadata(metadataBuilder.build());
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.e(SRV_LOG_NAME, "no image loaded");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            PicassoMessicUtil.loadCover(mContext, song, metadataTarget);
        } catch (Exception e) {
            // no image
            Log.e(SRV_LOG_NAME, "no image loaded", e);
        }

        //    metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
        //           song.getAlbum().getCover().get);
        mSession.setMetadata(metadataBuilder.build());
    }
}
