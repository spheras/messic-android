package org.messic.android.messiccore.download;

import java.io.File;

import org.messic.android.messiccore.datamodel.MDMSong;

public interface DownloadListener
{
    void downloadAdded( MDMSong song );

    void downloadUpdated( MDMSong song, float percent );

    void downloadFinished( MDMSong song, File fdownloaded );

    void downloadStarted( MDMSong song );

    void connected();

    void disconnected();
}
