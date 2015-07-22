package org.messic.android.messiccore.download;

import android.app.Service;

import org.messic.android.messiccore.datamodel.MDMSong;

import java.io.File;

/**
 * Created by spheras on 21/07/15.
 */
public interface IDownloadNotification extends DownloadListener {
    void createNotification(String title, String subtitle);

    void downloadAdded(MDMSong song);

    void downloadStarted(MDMSong song);

    void downloadUpdated(MDMSong song, float percent);

    void downloadFinished(MDMSong song, File fdownloaded);

    void connected();

    void disconnected();

    void setService(Service service);

}
