package org.messic.android.messiccore.datamodel.dao;

import android.content.ContentValues;
import android.database.Cursor;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DAOSong
        extends DAO {

    @Inject
    Configuration config;

    public DAOSong() {
        super(MDMSong.TABLE_NAME, MDMSong.getColumns());
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public void create() {
        getDatabase().execSQL(MDMSong.TABLE_CREATE);
    }

    public List<MDMSong> getRandomSongs(int limit, SongPublisher sp) {

        List<MDMSong> result = new ArrayList<MDMSong>();

        open();

        String query =
                "SELECT * FROM " + MDMSong.TABLE_NAME + " WHERE " + MDMSong.COLUMN_LOCAL_FILENAME
                        + " IS NOT NULL ORDER BY RANDOM() LIMIT " + limit;

        Cursor c = getDatabase().rawQuery(query, null);

        try {
            if (c != null && c.moveToFirst()) {
                do {

                    MDMSong song = new MDMSong(c, true);
                    if (song.isDownloaded(config)) {
                        if (sp != null) {
                            sp.publish(song);
                        }
                        result.add(song);
                    }
                }
                while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error er) {
            er.printStackTrace();
        }

        c.close();
        close();

        return result;
    }

    public int countDownloaded() {
        open();

        int result = _count();
        close();

        return result;
    }

    /**
     * Check if a certain song has been downloaded
     *
     * @param songServerSid
     * @return boolean
     */
    public boolean isDownloaded(int songServerSid) {
        boolean result = false;

        open();

        Cursor c = _getByServerSid(songServerSid);

        if (!c.isAfterLast()) {
            result = true;
        }

        c.close();
        close();

        return result;
    }

    public List<MDMSong> searchByAuthor(String text, SongPublisher sp) {
        List<MDMSong> result = new ArrayList<MDMSong>();
        open();

        DAOAuthor daoAuthor = new DAOAuthor();
        daoAuthor.open();
        Cursor c =
                daoAuthor._getAll(MDMAuthor.COLUMN_NAME + " LIKE '%" + text + "%' COLLATE NOCASE", MDMAuthor.COLUMN_NAME);
        if (c.moveToFirst()) {
            do {
                int lsid = c.getInt(MDMAuthor.COLUMN_LOCAL_SID_INDEX);
                result.addAll(getSongsByAuthor(lsid, true, sp));
            }
            while (c.moveToNext());
        }
        c.close();
        daoAuthor.close();

        close();

        return result;
    }

    public List<MDMSong> searchByName(String text, SongPublisher sp) {
        List<MDMSong> result = new ArrayList<MDMSong>();
        open();

        String where =
                MDMSong.COLUMN_LOCAL_FILENAME + " IS NOT NULL AND " + MDMSong.COLUMN_NAME + " LIKE '%" + text
                        + "%' COLLATE NOCASE";

        Cursor c = _getAll(where, MDMSong.COLUMN_NAME);
        if (c.moveToFirst()) {
            do {
                MDMSong song = new MDMSong(c, true);
                if (song.isDownloaded(config)) {
                    if (sp != null) {
                        sp.publish(song);
                    }
                    result.add(song);
                }
            }
            while (c.moveToNext());
        }
        c.close();
        close();

        return result;
    }

    public List<MDMSong> searchByAlbum(String text, SongPublisher sp) {
        List<MDMSong> result = new ArrayList<MDMSong>();
        open();

        DAOAlbum daoAlbum = new DAOAlbum();
        daoAlbum.open();
        Cursor c =
                daoAlbum._getAll(MDMAlbum.COLUMN_NAME + " LIKE '%" + text + "%' COLLATE NOCASE", MDMAlbum.COLUMN_NAME);
        if (c.moveToFirst()) {
            do {
                int lsid = c.getInt(MDMAlbum.COLUMN_LOCAL_SID_INDEX);
                result.addAll(getSongsByAlbum(lsid, true, sp));
            }
            while (c.moveToNext());
        }
        c.close();
        daoAlbum.close();

        close();

        return result;
    }

    /**
     * Get all the songs of a certain author (of all the albums found)
     *
     * @param authorLSid
     * @return
     */
    public List<MDMSong> getSongsByAuthor(int authorLSid, boolean downloadedOnly, SongPublisher publisher) {
        List<MDMSong> result = new ArrayList<MDMSong>();

        open();

        String query =
                "SELECT s.* FROM " + MDMSong.TABLE_NAME + " as s," + MDMAlbum.TABLE_NAME + " as a WHERE s."
                        + MDMSong.COLUMN_FK_ALBUM + "=a." + MDMAlbum.COLUMN_LOCAL_SID + " AND a." + MDMAlbum.COLUMN_FK_AUTHOR
                        + "=" + authorLSid;

        if (downloadedOnly) {
            query = query + " AND s." + MDMSong.COLUMN_LOCAL_FILENAME + " IS NOT NULL";
        }

        // query = query + " ORDER BY S." + MDMSong.COLUMN_NAME;

        Cursor c = getDatabase().rawQuery(query, null);

        if (c != null && c.moveToFirst()) {
            do {
                MDMSong song = new MDMSong(c, true);
                if (downloadedOnly) {
                    if (song.isDownloaded(config)) {
                        if (publisher != null) {
                            publisher.publish(song);
                        }
                        result.add(song);
                    }
                } else {
                    if (publisher != null) {
                        publisher.publish(song);
                    }
                    result.add(song);
                }
            }
            while (c.moveToNext());
        }

        c.close();
        close();
        return result;
    }

    public List<MDMSong> getSongsByAlbum(int albumSid, boolean downloadedOnly, SongPublisher sp) {
        List<MDMSong> result = new ArrayList<MDMSong>();

        open();

        String query = "SELECT * FROM " + getTableName() + " WHERE fk_album=" + albumSid;
        if (downloadedOnly) {
            query = query + " AND " + MDMSong.COLUMN_LOCAL_FILENAME + " IS NOT NULL";
        }
        Cursor c = getDatabase().rawQuery(query, null);

        if (c != null && c.moveToFirst()) {
            do {
                MDMSong song = new MDMSong(c, true);
                if (downloadedOnly) {
                    if (song.isDownloaded(config)) {
                        if (sp != null) {
                            sp.publish(song);
                        }
                        result.add(song);
                    }
                } else {
                    if (sp != null) {
                        sp.publish(song);
                    }
                    result.add(song);
                }
            }
            while (c.moveToNext());
        }

        c.close();
        close();
        return result;
    }

    public MDMSong save(MDMSong song) {
        open();

        ContentValues cv = new ContentValues();
        cv.put(MDMSong.COLUMN_TRACK, song.getTrack());
        cv.put(MDMSong.COLUMN_SERVER_FILENAME, song.getFileName());
        cv.put(MDMSong.COLUMN_LOCAL_FILENAME, song.getLfileName());
        cv.put(MDMSong.COLUMN_VOLUME, song.getVolume());
        cv.put(MDMSong.COLUMN_NAME, song.getName());
        cv.put(MDMSong.COLUMN_RATE, song.getRate());
        cv.put(MDMSong.COLUMN_SERVER_SID, song.getSid());

        if (song.getAlbum() == null) {
            // there is no album to link! :(
            cv.put(MDMSong.COLUMN_FK_ALBUM, 0);
        } else {
            if (song.getAlbum().getLsid() > 0) {
                // it has a local sid, so it is an existen song from the database, linking!
                cv.put(MDMSong.COLUMN_FK_ALBUM, song.getAlbum().getLsid());
            } else {
                // It is not an entity created from database, maybe it exist at database or maybe not, let's see
                DAOAlbum daoalbum = new DAOAlbum();
                daoalbum.open();
                Cursor cAlbum = daoalbum._getByServerSid(song.getAlbum().getSid());
                if (cAlbum.getCount() > 0) {
                    // it exist at the database!, so, just linking to it
                    MDMAlbum album = new MDMAlbum(cAlbum, false, true);
                    cv.put(MDMSong.COLUMN_FK_ALBUM, album.getLsid());
                } else {
                    // it doesn't exists at the database, we need to create it previously
                    MDMAlbum album = daoalbum.save(song.getAlbum(), false);
                    cv.put(MDMSong.COLUMN_FK_ALBUM, album.getLsid());
                }
                cAlbum.close();
                daoalbum.close();
            }
        }

        Cursor c = null;
        if (song.getLsid() > 0) {
            cv.put(MDMSong.COLUMN_LOCAL_SID, song.getLsid());
            c = super._update(cv, song.getLsid());
        } else {
            c = super._save(cv);
        }

        c.moveToFirst();
        MDMSong msi = new MDMSong(c, true);
        c.close();
        close();
        return msi;
    }

    public interface SongPublisher {
        void publish(MDMSong song);
    }
}
