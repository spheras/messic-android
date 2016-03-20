package org.messic.android.messiccore.dagger2;

import android.content.Context;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.datamodel.dao.MySQLiteHelper;
import org.messic.android.messiccore.util.AlbumCoverCache;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilDatabase;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module
public class AndroidCoreModule {
    @Provides
    @Singleton
    Configuration provideConfiguration() {
        return Configuration.get();
    }

    @Provides
    @Singleton
    UtilNetwork provideUtilNetwork() {
        return UtilNetwork.get();
    }

    @Provides
    @Singleton
    MessicPreferences providePreferences() {
        return MessicPreferences.get();
    }

    @Provides
    @Singleton
    UtilMusicPlayer provideUtilMusicPlayer() {
        return UtilMusicPlayer.get();
    }

    @Provides
    @Singleton
    UtilDownloadService provideUtilDownloadService() {
        return UtilDownloadService.get();
    }

    @Provides
    @Singleton
    AlbumCoverCache provideAlbumCoverCache() {
        return AlbumCoverCache.get();
    }

    @Provides
    @Singleton
    UtilRestJSONClient provideUtilRestJSONClient() {
        return UtilRestJSONClient.get();
    }

    @Provides
    @Singleton
    UtilDatabase provideUtilDatabase() {
        return UtilDatabase.get();
    }

    @Provides
    @Singleton
    DAOServerInstance provideDaoServerInstance() {
        return new DAOServerInstance();
    }

    @Provides
    @Singleton
    MySQLiteHelper provideMySQLiteHelper() {
        return MySQLiteHelper.get();
    }
}
