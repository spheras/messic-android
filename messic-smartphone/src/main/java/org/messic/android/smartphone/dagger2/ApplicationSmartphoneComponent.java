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
package org.messic.android.smartphone.dagger2;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.smartphone.activities.login.LoginEvents;
import org.messic.android.smartphone.activities.login.LoginPresenterImpl;
import org.messic.android.smartphone.activities.main.MainActivity;
import org.messic.android.smartphone.activities.main.fragments.explore.ExploreFragment;
import org.messic.android.smartphone.activities.main.fragments.playlist.PlaylistAdapter;
import org.messic.android.smartphone.activities.main.fragments.random.RandomEvents;
import org.messic.android.smartphone.activities.main.fragments.random.RandomPresenterImpl;
import org.messic.android.smartphone.activities.main.fragments.search.SearchSongAdapter;
import org.messic.android.smartphone.activities.searchmessicservice.SearchMessicServiceActivity;
import org.messic.android.smartphone.activities.searchmessicservice.SearchMessicServiceEvents;
import org.messic.android.smartphone.activities.searchmessicservice.SearchMessicServicePresenterImpl;
import org.messic.android.smartphone.notifications.MessicPlayerNotification;
import org.messic.android.smartphone.activities.MessicBaseActivity;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoActivity;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoPresenterImpl;
import org.messic.android.smartphone.activities.albuminfo.AlbumInfoSongAdapter;
import org.messic.android.smartphone.activities.login.LoginActivity;
import org.messic.android.smartphone.activities.main.MainFragmentAdapter;
import org.messic.android.smartphone.activities.main.MainPresenterImpl;
import org.messic.android.smartphone.activities.main.fragments.downloaded.DownloadedAlbumAdapter;
import org.messic.android.smartphone.activities.main.fragments.downloaded.DownloadedFragment;
import org.messic.android.smartphone.activities.main.fragments.downloaded.DownloadedPresenterImpl;
import org.messic.android.smartphone.activities.main.fragments.explore.ExploreAuthorsAdapter;
import org.messic.android.smartphone.activities.main.fragments.explore.ExplorePresenterImpl;
import org.messic.android.smartphone.activities.main.fragments.playlist.PlaylistFragment;
import org.messic.android.smartphone.activities.main.fragments.playlist.PlaylistPresenterImpl;
import org.messic.android.smartphone.activities.main.fragments.queue.PlayQueueFragment;
import org.messic.android.smartphone.activities.main.fragments.queue.PlayQueuePresenterImpl;
import org.messic.android.smartphone.activities.main.fragments.queue.PlayQueueSongAdapter;
import org.messic.android.smartphone.activities.main.fragments.random.RandomFragment;
import org.messic.android.smartphone.activities.main.fragments.random.RandomSongAdapter;
import org.messic.android.smartphone.activities.main.fragments.search.SearchFragment;
import org.messic.android.smartphone.activities.main.fragments.search.SearchPresenterImpl;
import org.messic.android.smartphone.activities.searchmanualmessicservice.SearchManualMessicServiceActivity;
import org.messic.android.smartphone.activities.searchmanualmessicservice.SearchManualMessicServiceEvents;
import org.messic.android.smartphone.activities.searchmanualmessicservice.SearchManualMessicServicePresenterImpl;
import org.messic.android.messiccore.dagger2.AndroidCoreModule;
import org.messic.android.messiccore.dagger2.ApplicationCoreComponent;
import org.messic.android.smartphone.views.player.PlayerPresenterImpl;
import org.messic.android.smartphone.views.player.PlayerView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {SmartphoneModule.class, MainSmartphoneModule.class, LoginSmartphoneModule.class, AlbuminfoSmartphoneModule.class, SearchSmartphoneModule.class, PlayerSmartphoneModule.class, SearchManualSmartphoneModule.class, AndroidCoreModule.class})
public interface ApplicationSmartphoneComponent extends ApplicationCoreComponent {

    void inject(MessicSmartphoneApp application);

    void inject(LoginPresenterImpl presenter);

    void inject(LoginActivity activity);

    void inject(SearchMessicServiceActivity ssa);

    void inject(LoginEvents le);

    void inject(SearchMessicServicePresenterImpl presenter);

    void inject(SearchMessicServiceEvents events);

    void inject(SearchManualMessicServiceActivity activity);

    void inject(SearchManualMessicServiceEvents smmse);

    void inject(SearchManualMessicServicePresenterImpl presenter);

    void inject(MainActivity activity);

    void inject(MainFragmentAdapter adapter);

    void inject(RandomFragment fragment);

    void inject(RandomPresenterImpl presenter);

    void inject(RandomSongAdapter adapter);

    void inject(RandomEvents events);

    void inject(MessicPlayerNotification notification);

    void inject(MessicBaseActivity activity);

    void inject(PlayerView view);

    void inject(ExploreFragment fragment);

    void inject(ExploreAuthorsAdapter adapter);

    void inject(ExplorePresenterImpl presenter);

    void inject(PlaylistFragment fragment);

    void inject(PlaylistAdapter adapter);

    void inject(PlaylistPresenterImpl presenter);

    void inject(PlayQueueFragment fragment);

    void inject(PlayQueueSongAdapter adapter);

    void inject(PlayQueuePresenterImpl presenter);

    void inject(AlbumInfoActivity activity);

    void inject(AlbumInfoPresenterImpl presenter);

    void inject(AlbumInfoSongAdapter adapter);

    void inject(DownloadedFragment fragment);

    void inject(DownloadedAlbumAdapter adapter);

    void inject(DownloadedPresenterImpl presenter);

    void inject(MainPresenterImpl presenter);

    void inject(SearchFragment fragment);

    void inject(SearchSongAdapter adapter);

    void inject(SearchPresenterImpl presenter);

    void inject(PlayerPresenterImpl presenter);
}
