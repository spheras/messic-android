package org.messic.android.messiccore.util;

import org.messic.android.messiccore.datamodel.dao.DAOSong;

public class UtilDatabase {

    private static UtilDatabase instance;

    private UtilDatabase() {

    }

    public static UtilDatabase get() {
        if (instance == null) {
            instance = new UtilDatabase();
        }
        return instance;
    }

    /**
     * Check if the database is empty
     *
     * @return boolean true->isempty
     */
    public boolean checkEmptyDatabase() {
        DAOSong ds = new DAOSong();
        boolean empty = (ds.countDownloaded() <= 0);
        if (empty) {
            // if the database is empty we should remove the whole offline messic folder
            UtilFile.emptyMessicFolder();
        }
        return empty;
    }

}
