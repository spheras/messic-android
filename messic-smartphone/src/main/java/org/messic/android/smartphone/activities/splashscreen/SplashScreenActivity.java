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
package org.messic.android.smartphone.activities.splashscreen;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Window;

import org.messic.android.R;
import org.messic.android.smartphone.activities.MessicBaseActivity;
import org.messic.android.smartphone.activities.login.LoginActivity;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenActivity extends MessicBaseActivity {
    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 1500;

    @Override
    protected void onStart() {
        super.onStart();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Start the next activity
                        Intent mainIntent = new Intent().setClass(SplashScreenActivity.this, LoginActivity.class);
                        transitionTo(mainIntent);
                    }
                });

                // Close the activity so the user won't able to go back this
                // activity pressing Back button
                //finish();
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set portrait orientation & Hide title bar; must be done first
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //setupWindowAnimations();
    }

    private void setupWindowAnimations() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Fade fade = (Fade) TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
            Slide slide = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);

            getWindow().setEnterTransition(fade);
            getWindow().setReenterTransition(slide);
            getWindow().setExitTransition(fade);

        }
    }
}
