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
package org.messic.android.activities.searchmanualmessicservice;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Toast;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.activities.MessicBaseActivity;
import org.messic.android.databinding.ActivitySearchmessicserviceManualBinding;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;
import org.parceler.Parcels;

import javax.inject.Inject;

import rx.Subscription;

public class SearchManualMessicServiceActivity
        extends MessicBaseActivity {


    @Inject
    SearchManualMessicServicePresenter presenter;
    private Subscription subscription;
    private ActivitySearchmessicserviceManualBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.presenter.resume();

        if (this.subscription != null)
            RxDispatcher.get().unsubscribe(this.subscription);

        this.subscription = subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.presenter.pause();
        RxDispatcher.get().unsubscribe(this.subscription);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) getApplication()).getSmartphoneComponent().inject(this);


        startServices();
        bindData(savedInstanceState);
        setupLayout();
        setupToolbar(true);
        this.presenter.initialize();
        setupWindowAnimations();
    }

    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
            public void call(RxAction event) {
                if (event.isType(SearchManualMessicServiceEvents.EVENT_FINISH_ACTIVITY)) {
                    SearchManualMessicServiceActivity.this.finish();
                } else if (event.isType(SearchManualMessicServiceEvents.EVENT_MANDATORY_FIELDS)) {
                    Toast.makeText(SearchManualMessicServiceActivity.this, getResources().getString(R.string.searchMessicServiceManual_mandatory), Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    /**
     * Binding information form the layout to objects
     */
    private void bindData(Bundle savedInstanceState) {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_searchmessicservice_manual);

        SearchManualMessicServiceActivityBindingImpl instance;
        if (savedInstanceState == null) {
            instance = new SearchManualMessicServiceActivityBindingImpl("", "", false, 82, "");
        } else {
            instance = Parcels.unwrap(savedInstanceState.getParcelable(BINDING_PARCEL));
        }

        this.binding.setInstance(instance);
        this.binding.setEvents(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BINDING_PARCEL, Parcels.wrap(this.binding.getInstance()));
    }

    /**
     * setting up the layout of the activity.
     * Here you must put elements, remove elements, manage events, ...
     */
    private void setupLayout() {

    }

    /**
     * Setting up the animations for this activty, mainly enter and exit transitions
     */

    private void setupWindowAnimations() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
            getWindow().setEnterTransition(transition);
        }
    }

    public void onSaveClick(View v) {
        this.presenter.saveAction(this.binding.getInstance());
    }

    public void onCancelClick(View v) {
        finish(); //forgive me for don't using the presenter for this.
    }
}
