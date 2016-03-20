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
package org.messic.android.messiccore.download;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.datamodel.dao.DAOAuthor;
import org.messic.android.messiccore.datamodel.dao.DAOSong;
import org.messic.android.messiccore.util.AlbumCoverCache;
import org.messic.android.messiccore.util.UtilDownloadService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class DownloadManagerService
        extends Service {
    private final IBinder downloadManagerBind = new DownloadManagerBinder();

    @Inject
    AlbumCoverCache acc;
    @Inject
    Configuration config;

    @Inject
    UtilDownloadService uds;

    public void onCreate() {
        super.onCreate();
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    private void saveDatabase(MDMSong song) {

        DAOSong daosong = new DAOSong();
        daosong.open();
        Cursor csong = daosong._getByServerSid(song.getSid());
        if (csong.moveToFirst()) {
            // the song exist, so, we only need to update the info
            MDMSong rsong = new MDMSong(csong, true);
            rsong.setLfileName(song.getLfileName());
            daosong.save(rsong);
        } else {
            // the song doesn't exist... maybe the album exist?
            // song.getAlbum();
            DAOAlbum daoalbum = new DAOAlbum();
            daoalbum.open();
            Cursor cAlbum = daoalbum._getByServerSid(song.getAlbum().getSid());
            if (cAlbum.moveToFirst()) {
                // the album exist previously, lets add the song?
                MDMAlbum album = new MDMAlbum(cAlbum, true, false);
                MDMSong nsong = new MDMSong(song);
                nsong.setAlbum(album);
                daosong.save(nsong);
            } else {
                // the album doesn't exist...maybe the author exist?
                DAOAuthor daoauthor = new DAOAuthor();
                daoauthor.open();
                Cursor cAuthor = daoauthor._getByServerSid(song.getAlbum().getAuthor().getSid());
                if (cAuthor.moveToFirst()) {
                    // the author exist previously, lets add the album?
                    MDMAuthor author = new MDMAuthor(cAuthor);
                    MDMSong nsong = new MDMSong(song);
                    MDMAlbum album = nsong.getAlbum();
                    MDMAlbum nalbum = new MDMAlbum(album);
                    nalbum.setAuthor(author);
                    nsong.setAlbum(nalbum);
                    daoalbum.save(nsong.getAlbum(), true);
                } else {
                    // the author doesn't exist previously... lets save everything!
                    MDMAuthor author = song.getAlbum().getAuthor();
                    MDMAuthor nauthor = new MDMAuthor(author);
                    nauthor.setAlbums(new ArrayList<MDMAlbum>());
                    nauthor.addAlbum(song.getAlbum());
                    daoauthor.save(nauthor, true);
                }
                cAuthor.close();
                daoauthor.close();
            }
            cAlbum.close();
            daoalbum.close();
        }
        csong.close();
        daosong.close();
    }

    private void saveCover(MDMSong song) {
        String folder = song.getAlbum().calculateExternalStorageFolder();
        String scover = folder + "/" + AlbumCoverCache.COVER_OFFLINE_FILENAME;
        final File fcover = new File(scover);
        if (!fcover.exists()) {
            Bitmap cover = acc.getCover(song.getAlbum(), new AlbumCoverCache.CoverListener() {

                public void setCover(Bitmap bitmap) {
                    saveCover(bitmap, fcover);
                }

                public void failed(Exception e) {
                    Log.e("DownloadManagerService", "cover error", e);
                }
            }, 500, 500);
            if (cover != null) {
                saveCover(cover, fcover);
            }
        }
    }

    private void saveCover(Bitmap cover, File fcover) {
        try {
            FileOutputStream baos = new FileOutputStream(fcover);
            cover.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            baos.close();
        } catch (FileNotFoundException e1) {
            Timber.e("DownloadManagerService", "cover error", e1);
        } catch (IOException e1) {
            Timber.e("DownloadManagerService", "cover error", e1);
        }
    }

    public void addDownload(MDMAlbum album, DownloadListener olistener) {
        List<MDMSong> songs = album.getSongs();
        for (MDMSong mdmSong : songs) {
            addDownload(mdmSong, olistener);
        }
    }

    public void addDownload(MDMSong song, final DownloadListener olistener) {
        uds.notification.setService(this);
        uds.notification.downloadAdded(song);

        String path = song.calculateExternalStorageFolder();
        File fpath = new File(path);
        fpath.mkdirs();
        try {
            DownloadQueue.DownloadQueueListener listener = new DownloadQueue.DownloadQueueListener() {
                public void received(MDMSong song, File fdownloaded) {
                    uds.notification.downloadFinished(song, fdownloaded);
                    song.setLfileName(fdownloaded.getAbsolutePath());
                    song.getAlbum().setLfileName(song.getAlbum().calculateExternalStorageFolder());
                    song.getAlbum().getAuthor().setLfileName(song.getAlbum().getAuthor().calculateExternalStorageFolder());
                    saveCover(song);
                    saveDatabase(song);
                    if (olistener != null) {
                        olistener.downloadFinished(song, fdownloaded);
                    }
                }

                public void started(MDMSong song) {
                    uds.notification.downloadStarted(song);
                }
            };

            DownloadQueue.addDownload(new URL(song.getURL(config)), song, listener);

        } catch (MalformedURLException e) {
            Log.e("DownloadManagerService", "error downloading!");
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return downloadManagerBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public class DownloadManagerBinder
            extends Binder {
        public DownloadManagerService getService() {
            return DownloadManagerService.this;
        }
    }
}
