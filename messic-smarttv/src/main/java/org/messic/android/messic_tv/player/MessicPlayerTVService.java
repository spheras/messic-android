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
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.messic_tv.R;
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
        Log.d("MessicPlayerService", "onCreate");
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
        mSession.setActive(false);
        mSession.release();
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
                UtilMusicPlayer.stopMessicMusicService(getApplicationContext());
            }
        }
    }

    private class PlayerListener implements PlayerEventListener {

        @Override
        public void paused(MDMSong song, int index) {

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
                final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE,
                        song.getAlbum().getName());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
                        song.getName());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, song.getName());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, song.getAlbum().getName());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, song.getAlbum().getAuthor().getName());

                try {
                    String coverOnlineURL =
                            Configuration.getBaseUrl(mContext) + "/services/albums/" + song.getAlbum().getSid()
                                    + "/cover?preferredWidth=" + Utils.convertDpToPixel(mContext, 256) + "&preferredHeight=" + Utils.convertDpToPixel(mContext, 256) + "&messic_token="
                                    + Configuration.getLastToken();
                    metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, coverOnlineURL);
                    metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, coverOnlineURL);

                    Target metadataTarget = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON, bitmap);
                            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap);
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
                    Picasso.with(mContext).load(coverOnlineURL).error(mContext.getResources().getDrawable(R.drawable.unknowncover, null)).into(metadataTarget);
                } catch (Exception e) {
                    // no image
                    Log.e(SRV_LOG_NAME, "no image loaded", e);
                }

                //    metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
                //           song.getAlbum().getCover().get);
                mSession.setMetadata(metadataBuilder.build());
            }
        }

        @Override
        public void completed(int index) {
            Log.i(SRV_LOG_NAME, "list played");
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

        }

        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {

        }
    }
}
