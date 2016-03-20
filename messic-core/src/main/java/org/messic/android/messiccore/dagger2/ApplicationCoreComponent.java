package org.messic.android.messiccore.dagger2;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAO;
import org.messic.android.messiccore.datamodel.dao.DAOSong;
import org.messic.android.messiccore.datamodel.dao.MySQLiteHelper;
import org.messic.android.messiccore.download.DownloadManagerService;
import org.messic.android.messiccore.player.MessicPlayerQueue;
import org.messic.android.messiccore.player.MessicPlayerService;
import org.messic.android.messiccore.util.AlbumCoverCache;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AndroidCoreModule.class)
public interface ApplicationCoreComponent {

    void inject(MessicPreferences preferences);

    void inject(Configuration configuration);

    void inject(UtilNetwork network);

    void inject(MySQLiteHelper sqlh);

    void inject(MDMSong song);

    void inject(UtilMusicPlayer ump);

    void inject(MessicPlayerService mps);

    void inject(MessicPlayerQueue queue);

    void inject(UtilDownloadService uds);

    void inject(DownloadManagerService dms);

    void inject(AlbumCoverCache acc);

    void inject(UtilRestJSONClient urj);

    void inject(DAOSong dao);

    void inject(DAO dao);
}
