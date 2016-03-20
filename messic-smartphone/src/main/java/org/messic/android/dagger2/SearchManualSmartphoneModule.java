/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.messic.android.dagger2;

import android.content.Context;

import org.messic.android.activities.searchmanualmessicservice.SearchManualMessicServiceEvents;
import org.messic.android.activities.searchmanualmessicservice.SearchManualMessicServicePresenter;
import org.messic.android.activities.searchmanualmessicservice.SearchManualMessicServicePresenterImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a {@link Context} or
 * {@link android.app.Application} to create.
 */
@Module
public class SearchManualSmartphoneModule {
    @Provides
    @Singleton
    SearchManualMessicServicePresenter provideSearchManualMessicServicePresenter() {
        return new SearchManualMessicServicePresenterImpl();
    }

    @Provides
    @Singleton
    SearchManualMessicServiceEvents provideSearchManualMessicServiceEvents() {
        return SearchManualMessicServiceEvents.get();
    }
}