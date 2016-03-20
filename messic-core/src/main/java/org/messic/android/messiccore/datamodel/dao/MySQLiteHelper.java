package org.messic.android.messiccore.datamodel.dao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMGenre;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilFile;

import java.io.File;

import javax.inject.Inject;

public class MySQLiteHelper
        extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "messic.db";

    private static final int DATABASE_VERSION = 2;

    private static final boolean DEVELOPER_MODE = true;

    private static MySQLiteHelper instance = null;

    @Inject
    public Configuration config;

    private MySQLiteHelper() {
        super(MessicCoreApp.getInstance(), DATABASE_NAME, null, DATABASE_VERSION);

        MessicCoreApp.getInstance().getComponent().inject(this);//.component().inject(this);

        /*
        try {
            StrictModeWrapper.checkAvailable();
            StrictModeWrapper.setStrictMode();
        } catch (Throwable throwable) {
            //not available, .. it is supposed to not to be in DEBUG
            Log.d("MySQLiteHelper", "StrictMode Not Available!");
        }
*/
    }

    public static synchronized MySQLiteHelper get() {
        if (instance == null) {
            instance = new MySQLiteHelper();
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        config.setFirstTime(true);

        File fbase = new File(UtilFile.getMessicOfflineFolderAbsolutePath());
        if (fbase.exists()) {
            UtilFile.deleteDirectory(fbase);
        }

        database.execSQL(MDMMessicServerInstance.TABLE_CREATE);
        database.execSQL(MDMGenre.TABLE_CREATE);
        database.execSQL(MDMAuthor.TABLE_CREATE);
        database.execSQL(MDMAlbum.TABLE_CREATE);
        database.execSQL(MDMSong.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        Log.w( MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
            + ", which will destroy all old data" );
        db.execSQL( "DROP TABLE IF EXISTS " + MDMMessicServerInstance.TABLE_NAME );
        db.execSQL( "DROP TABLE IF EXISTS " + MDMGenre.TABLE_NAME );
        db.execSQL( "DROP TABLE IF EXISTS " + MDMAuthor.TABLE_NAME );
        db.execSQL( "DROP TABLE IF EXISTS " + MDMAlbum.TABLE_NAME );
        db.execSQL( "DROP TABLE IF EXISTS " + MDMSong.TABLE_NAME );
        onCreate( db );
        */
        if (oldVersion < 2) {
            //we need to create the field volume con MDMSong
            db.execSQL("ALTER TABLE " + MDMSong.TABLE_NAME + " ADD COLUMN " + MDMSong.COLUMN_VOLUME + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + MDMAlbum.TABLE_NAME + " ADD COLUMN " + MDMAlbum.COLUMN_VOLUMES + " INTEGER DEFAULT 0");
        }
    }

}