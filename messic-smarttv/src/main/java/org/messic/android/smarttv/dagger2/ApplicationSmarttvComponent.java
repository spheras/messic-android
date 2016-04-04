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
package org.messic.android.smarttv.dagger2;

import org.messic.android.messiccore.dagger2.AndroidCoreModule;
import org.messic.android.messiccore.dagger2.ApplicationCoreComponent;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.MessicBaseActivity;
import org.messic.android.smarttv.activities.login.LoginActivity;
import org.messic.android.smarttv.activities.login.LoginEvents;
import org.messic.android.smarttv.activities.login.LoginPresenterImpl;
import org.messic.android.smarttv.activities.main.fragments.MainFragment;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenterImpl;
import org.messic.android.smarttv.activities.main.fragments.PicassoImageCardViewTarget;
import org.messic.android.smarttv.activities.main.fragments.SongCardPresenter;
import org.messic.android.smarttv.utils.RemoteControlReceiver;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {SmarttvModule.class, LoginSmarttvModule.class, MainFragmentSmartTVModule.class, AndroidCoreModule.class})
public interface ApplicationSmarttvComponent extends ApplicationCoreComponent {

    void inject(MessicSmarttvApp application);

    void inject(MessicBaseActivity activity);

    void inject(LoginActivity activity);

    void inject(LoginEvents events);

    void inject(LoginPresenterImpl presenter);

    void inject(RemoteControlReceiver receiver);

    void inject(MainFragment fragment);

    void inject(SongCardPresenter presenter);

    void inject(PicassoImageCardViewTarget target);

    void inject(MainFragmentPresenterImpl presenter);

}
