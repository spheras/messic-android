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

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Presenter;

import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenterImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module
public class MainFragmentSmartTVModule {

    @Provides
    @Singleton
    MainFragmentPresenter provideMainFragmentPresenter() {
        return new MainFragmentPresenterImpl();
    }

    @Provides
    @Singleton
    Presenter providePresenter() {
        //@TODO
        return null;
    }

    @Provides
    @Singleton
    ArrayObjectAdapter provideAdapter() {
        return new ArrayObjectAdapter(new ListRowPresenter());
    }


}