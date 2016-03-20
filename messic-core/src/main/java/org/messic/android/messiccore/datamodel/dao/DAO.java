package org.messic.android.messiccore.datamodel.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.messic.android.messiccore.MessicCoreApp;

import javax.inject.Inject;

public abstract class DAO {
    public static final int BOOLEAN_TRUE = 0;

    public static final int BOOLEAN_FALSE = -1;

    @Inject
    MySQLiteHelper dbHelper;

    private String tableName;

    private String[] columns;

    public DAO(String tableName, String[] columns) {
        MessicCoreApp.getInstance().getComponent().inject(this);

        this.columns = columns;
        this.tableName = tableName;
    }

    public static int getBoolean(boolean b) {
        if (b) {
            return BOOLEAN_TRUE;
        } else {
            return BOOLEAN_FALSE;
        }
    }

    public static boolean getBoolean(int i) {
        if (i == BOOLEAN_TRUE) {
            return true;
        } else {
            return false;
        }
    }

    public void open()
            throws SQLException {
        getDatabase().acquireReference();
        //getDatabase().beginTransaction();
    }

    public synchronized void close() {
//        Best Practices:
//        Open and close the database and resultset with each operation
        //getDatabase().endTransaction();
        getDatabase().close();
        //dbHelper.close();
    }

    /**
     * @return the database
     */
    public synchronized SQLiteDatabase getDatabase() {
        return dbHelper.getWritableDatabase();
    }


    public abstract void create();

    public void _recreate() {
        open();
        _drop();
        create();
        close();
    }

    public void _drop() {
        getDatabase().execSQL("DROP TABLE IF EXISTS " + getTableName());
    }

    public Cursor _getAll() {
        return _getAll(null);
    }

    public Cursor _getAll(String orderBy) {
        return _getAll(null, orderBy);
    }

    public Cursor _getAll(String where, String orderBy) {
        Cursor cursor = getDatabase().query(this.tableName, this.columns, where, null, null, null, orderBy, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor _get(int sid) {
        Cursor cursor =
                getDatabase().query(this.tableName, this.columns, "lsid=?", new String[]{"" + sid}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public int _count() {
        Cursor mCount = getDatabase().rawQuery("SELECT count(*) FROM " + this.tableName, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    public Cursor _getByServerSid(long serverSid) {
        Cursor cursor =
                getDatabase().query(this.tableName, this.columns, "sid=?", new String[]{"" + serverSid}, null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public boolean _delete(long lsid) {
        return getDatabase().delete(this.tableName, "lsid=?", new String[]{"" + lsid}) > 0;
    }

    public Cursor _save(ContentValues values) {
        long lsid = getDatabase().insert(this.tableName, null, values);
        return _get((int) lsid);
    }

    public Cursor _update(ContentValues values, int lsid) {
        getDatabase().update(this.tableName, values, "lsid=?", new String[]{"" + lsid});
        return _get(lsid);
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
