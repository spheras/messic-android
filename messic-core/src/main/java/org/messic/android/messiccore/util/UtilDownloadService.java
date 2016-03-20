package org.messic.android.messiccore.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.datamodel.dao.DAOAuthor;
import org.messic.android.messiccore.datamodel.dao.DAOGenre;
import org.messic.android.messiccore.datamodel.dao.DAOSong;
import org.messic.android.messiccore.download.DownloadListener;
import org.messic.android.messiccore.download.DownloadManagerService;
import org.messic.android.messiccore.download.DownloadManagerService.DownloadManagerBinder;
import org.messic.android.messiccore.download.IDownloadNotification;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

public class UtilDownloadService {


    private static UtilDownloadService instance;
    public IDownloadNotification notification;

    @Inject
    Configuration config;
    private DownloadManagerService downloadService = null;
    private boolean downloadBound = false;
    // connect to the service
    private ServiceConnection downloadConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadManagerBinder binder = (DownloadManagerBinder) service;
            // get service
            downloadService = binder.getService();
            // pass list
            downloadBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            downloadService = null;
            downloadBound = false;
        }

    };

    private UtilDownloadService() {
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public static UtilDownloadService get() {
        if (instance == null) {
            instance = new UtilDownloadService();
        }
        return instance;
    }

    public void startDownloadService(IDownloadNotification notification) {
        if (!downloadBound) {
            this.notification = notification;
            Context appctx = MessicCoreApp.getInstance();
            Intent downloadIntent = new Intent(appctx, DownloadManagerService.class);
            appctx.startService(downloadIntent);

            appctx.bindService(downloadIntent, downloadConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void stopDownloadService() {
        if (downloadBound) {
            Context appctx = MessicCoreApp.getInstance();
            Intent downloadIntent = new Intent(appctx, DownloadManagerService.class);
            appctx.unbindService(downloadConnection);
            appctx.stopService(downloadIntent);
        }
    }

    public DownloadManagerService getDownloadService() {
        if (downloadBound) {
            return downloadService;
        } else {
            startDownloadService(null);
            return null;
        }
    }

    public boolean addDownload(MDMSong song, DownloadListener listener) {
        DownloadManagerService mps = getDownloadService();
        if (mps != null) {


            mps.addDownload(song, listener);
            return true;
        }
        return false;
    }

    public boolean addDownload(MDMAlbum album, DownloadListener listener) {
        DownloadManagerService mps = getDownloadService();
        if (mps != null) {
            mps.addDownload(album, listener);

            return true;
        }
        return false;
    }

    /**
     * Empty all the local database and remove all the local files!
     *
     * @return
     */
    public void emptyLocal() {
        DAOAuthor daoauthor = new DAOAuthor();
        DAOAlbum daoalbum = new DAOAlbum();
        DAOSong daosong = new DAOSong();
        DAOGenre daogenre = new DAOGenre();

        daoauthor._recreate();
        daoalbum._recreate();
        daosong._recreate();
        daogenre._recreate();

        File fbase = new File(UtilFile.getMessicOfflineFolderAbsolutePath());
        if (fbase.exists()) {
            UtilFile.deleteDirectory(fbase);
        }


        if (config.isOffline()) {
            //@TODO review this
            if (config.getLoginActivityClass() != null) {
                Intent ssa = new Intent(MessicCoreApp.getInstance(), config.getLoginActivityClass());
                MessicCoreApp.getInstance().startActivity(ssa);
            }
        }
    }

    public boolean removeAlbum(MDMAlbum album) {

        DAOAlbum daoalbum = new DAOAlbum();
        daoalbum.open();

        Cursor cursor = daoalbum._getByServerSid((int) album.getSid());
        if (cursor.moveToFirst()) {
            MDMAlbum lalbum = new MDMAlbum(cursor, false, false);
            daoalbum.removeAlbum(lalbum.getLsid());
        }
        cursor.close();

        daoalbum.close();
        String sAlbumFolder = album.calculateExternalStorageFolder();
        File fAlbumFolder = new File(sAlbumFolder);
        UtilFile.deleteDirectory(fAlbumFolder);
        // the last thing is to check if the author has other albums downloaded.. or just we need to remove the
        // author also
        MDMAuthor author = album.getAuthor();
        List<MDMAlbum> palbums = daoalbum.getAllByAuthorLSid(author.getLsid(), false);
        if (palbums.size() == 0) {
            // the author can be removed too!
            DAOAuthor daoauthor = new DAOAuthor();
            daoauthor.open();
            daoauthor._delete(author.getLsid());
            daoauthor.close();
            String sAuthorFolder = author.calculateExternalStorageFolder();
            File fAuthorFolder = new File(sAuthorFolder);
            UtilFile.deleteDirectory(fAuthorFolder);
        }


        return true;
    }

    public boolean removeSong(MDMSong song) {
        String lfilename = song.getLfileName();
        File f = new File(lfilename);
        if (f.exists()) {
            f.delete();
        }
        song.setLfileName("");
        DAOSong daosong = new DAOSong();
        daosong.save(song);

        // now we need to check if the album is empty now or not.. to remove the album folder also
        DAOAlbum daoalbum = new DAOAlbum();
        daoalbum.open();
        Cursor c = daoalbum._get(song.getAlbum().getLsid());
        if (c.moveToFirst()) {
            MDMAlbum album = new MDMAlbum(c, true, false);
            boolean flagEmptyAlbum = true;
            List<MDMSong> albumSongs = album.getSongs();
            for (int i = 0; i < albumSongs.size(); i++) {
                MDMSong asong = albumSongs.get(i);
                if (asong.isDownloaded(config)) {
                    flagEmptyAlbum = false;
                    break;
                }
            }
            if (flagEmptyAlbum) {
                // the ablum is empty (no more songs downloaded).. we need to remove the album folder also
                daoalbum.removeAlbum(album.getLsid());
                String sAlbumFolder = album.calculateExternalStorageFolder();
                File fAlbumFolder = new File(sAlbumFolder);
                UtilFile.deleteDirectory(fAlbumFolder);

                // the last thing is to check if the author has other albums downloaded.. or just we need to remove the
                // author also
                MDMAuthor author = album.getAuthor();
                List<MDMAlbum> palbums = daoalbum.getAllByAuthorLSid(author.getLsid(), false);
                if (palbums.size() == 0) {
                    // the author can be removed too!
                    DAOAuthor daoauthor = new DAOAuthor();
                    daoauthor.open();
                    daoauthor._delete(author.getLsid());
                    daoauthor.close();
                    String sAuthorFolder = author.calculateExternalStorageFolder();
                    File fAuthorFolder = new File(sAuthorFolder);
                    UtilFile.deleteDirectory(fAuthorFolder);
                }

            }
        }
        c.close();
        daoalbum.close();

        return true;
    }
}
