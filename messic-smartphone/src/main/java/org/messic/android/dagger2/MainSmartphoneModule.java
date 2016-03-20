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
package org.messic.android.dagger2;

import android.content.Context;

import org.messic.android.activities.main.MainPresenter;
import org.messic.android.activities.main.MainPresenterImpl;
import org.messic.android.activities.main.fragments.downloaded.DownloadedAlbumAdapter;
import org.messic.android.activities.main.fragments.downloaded.DownloadedPresenter;
import org.messic.android.activities.main.fragments.downloaded.DownloadedPresenterImpl;
import org.messic.android.activities.main.fragments.explore.ExploreAuthorsAdapter;
import org.messic.android.activities.main.fragments.explore.ExplorePresenter;
import org.messic.android.activities.main.fragments.explore.ExplorePresenterImpl;
import org.messic.android.activities.main.fragments.playlist.PlaylistAdapter;
import org.messic.android.activities.main.fragments.playlist.PlaylistPresenter;
import org.messic.android.activities.main.fragments.playlist.PlaylistPresenterImpl;
import org.messic.android.activities.main.fragments.queue.PlayQueuePresenter;
import org.messic.android.activities.main.fragments.queue.PlayQueuePresenterImpl;
import org.messic.android.activities.main.fragments.queue.PlayQueueSongAdapter;
import org.messic.android.activities.main.fragments.random.RandomEvents;
import org.messic.android.activities.main.fragments.random.RandomPresenter;
import org.messic.android.activities.main.fragments.random.RandomPresenterImpl;
import org.messic.android.activities.main.fragments.random.RandomSongAdapter;
import org.messic.android.activities.main.fragments.search.SearchPresenter;
import org.messic.android.activities.main.fragments.search.SearchPresenterImpl;
import org.messic.android.activities.main.fragments.search.SearchSongAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module
public class MainSmartphoneModule {

    @Provides
    @Singleton
    MainPresenter provideMainPresenter() {
        return new MainPresenterImpl();
    }

    @Provides
    @Singleton
    RandomPresenter provideRandomPresenter() {
        return new RandomPresenterImpl();
    }

    @Provides
    @Singleton
    RandomEvents provideRandomEvents() {
        return RandomEvents.get();
    }

    @Provides
    @Singleton
    ExplorePresenter provideExplorePresenter() {
        return new ExplorePresenterImpl();
    }


    @Provides
    @Singleton
    RandomSongAdapter provideRandomSongAdapter() {
        return new RandomSongAdapter();
    }

    @Provides
    @Singleton
    ExploreAuthorsAdapter provideExploreAuthorsAdapter() {
        return new ExploreAuthorsAdapter();
    }

    @Provides
    @Singleton
    PlaylistPresenter providePlaylistPresenter() {
        return new PlaylistPresenterImpl();
    }

    @Provides
    @Singleton
    PlaylistAdapter providePlaylistAdapter() {
        return new PlaylistAdapter();
    }

    @Provides
    @Singleton
    PlayQueuePresenter providePlayQueuePresenter() {
        return new PlayQueuePresenterImpl();
    }

    @Provides
    @Singleton
    PlayQueueSongAdapter providePlayQueueSongAdapter() {
        return new PlayQueueSongAdapter();
    }

    @Provides
    @Singleton
    DownloadedPresenter provideDownloadedPresenter() {
        return new DownloadedPresenterImpl();
    }

    @Provides
    @Singleton
    DownloadedAlbumAdapter provideDownloadedAlbumAdapter() {
        return new DownloadedAlbumAdapter();
    }

    @Provides
    @Singleton
    SearchPresenter provideSearchPresenter() {
        return new SearchPresenterImpl();
    }

    @Provides
    @Singleton
    SearchSongAdapter provideSearchSongAdapter() {
        return new SearchSongAdapter();
    }

}