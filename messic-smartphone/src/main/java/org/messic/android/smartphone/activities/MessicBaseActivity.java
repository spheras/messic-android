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
package org.messic.android.smartphone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smartphone.notifications.DownloadNotification;
import org.messic.android.smartphone.notifications.MessicPlayerNotification;

import javax.inject.Inject;


public abstract class MessicBaseActivity extends AppCompatActivity {

    public final static String BINDING_PARCEL = "BINDING";

    @Inject
    UtilMusicPlayer ump;
    @Inject
    UtilDownloadService uds;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        ((MessicSmartphoneApp) MessicSmartphoneApp.getInstance()).getSmartphoneComponent().inject(this);
    }

    @SuppressWarnings("unchecked")
    protected void transitionTo(Intent i) {
//        final Pair<View, String>[] pairs = TransitionHelper.createSafeTransitionParticipants(this, true);
//        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pairs);
//        startActivity(i, transitionActivityOptions.toBundle());
        startActivity(i);
    }

    protected void setupToolbar(boolean home) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(home);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startServices() {
        ump.startMessicMusicService(MessicPlayerNotification.class);
        uds.startDownloadService(new DownloadNotification());
    }

}
