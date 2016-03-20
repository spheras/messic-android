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
package org.messic.android.activities.searchmessicservice;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.activities.MessicBaseActivity;
import org.messic.android.activities.login.LoginActivity;
import org.messic.android.activities.main.MainActivity;
import org.messic.android.activities.searchmanualmessicservice.SearchManualMessicServiceActivity;
import org.messic.android.databinding.ActivitySearchmessicserviceBinding;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;

public class SearchMessicServiceActivity
        extends MessicBaseActivity implements SearchMessicServiceItemViewHolder.IViewHolderClicks {

    @Inject
    Configuration config;
    @Inject
    SearchMessicServicePresenter presenter;

    private RecyclerView rvList;
    private Button buttonOffline;
    private Button buttonSearch;
    private ActivitySearchmessicserviceBinding binding;
    private Subscription subscription;
    private SearchMessicServiceAdapter rvListAdapter;
    private View lempty;

    @Override
    protected void onStart() {
        super.onStart();

        //we fill with saved data
        List<MDMMessicServerInstance> servers = this.presenter.getSavedSessions();
        if (servers.size() > 0) {
            lempty.setVisibility(View.GONE);
            for (MDMMessicServerInstance instance : servers) {
                this.rvListAdapter.addInstance(instance);
            }
        }

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

    private void bindData(Bundle savedInstanceState) {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_searchmessicservice);
        this.binding.setEvents(this);

        //nothing binded to get from savedInstance

        this.rvList = (RecyclerView) findViewById(R.id.searchmessicservice_lvresults);
        this.buttonOffline = (Button) findViewById(R.id.searchmessicservice_offline);
        this.buttonSearch = ((Button) findViewById(R.id.searchmessicservice_bsearch));

        this.rvListAdapter = new SearchMessicServiceAdapter(this);
        this.rvList.setLayoutManager(new LinearLayoutManager(this));
        this.rvList.setAdapter(this.rvListAdapter);
        this.lempty = (View) findViewById(R.id.searchmessicservice_lempty);

        //we allow the swipe event animation element
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                MDMMessicServerInstance msi = (MDMMessicServerInstance) rvListAdapter.getItem(viewHolder.getAdapterPosition());
                return presenter.isSwipeable(msi);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int pos = viewHolder.getAdapterPosition();
                MDMMessicServerInstance msi = (MDMMessicServerInstance) rvListAdapter.getItem(pos);
                presenter.swipe(msi);
                rvListAdapter.removeItem(pos);
                rvListAdapter.updateAdapter();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(this.rvList);
    }

    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
            public void call(RxAction event) {
                if (event.isType(SearchMessicServiceEvents.EVENT_SHOW_SCREEN)) {
                    String screen = (String) event.getSimpleData();

                    if (screen.equals(SearchMessicServiceEvents.SCREEN_MANUAL_SEARCH)) {
                        Intent ssa = new Intent(SearchMessicServiceActivity.this, SearchManualMessicServiceActivity.class);
                        SearchMessicServiceActivity.this.startActivity(ssa);
                    } else if (screen.equals(SearchMessicServiceEvents.SCREEN_LOGINOFFLINE)) {
                        Intent ssa = new Intent(SearchMessicServiceActivity.this, MainActivity.class);
                        ssa.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        SearchMessicServiceActivity.this.startActivity(ssa);
                    }
                }
            }
        });
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
            //getWindow().setEnterTransition(transition);
        }
    }

    private void setupLayout() {
        SearchMessicServicePresenter.ShowControl sc = this.presenter.selectLayout();
        if (!sc.showLoginOffline) {
            buttonOffline.setVisibility(View.GONE);
        } else {
            buttonOffline.setVisibility(View.VISIBLE);
        }
    }

    public void onSearchServiceManualClick(View view) {
        this.presenter.manualSearchAction();
    }

    public void onPlayOfflineClick(View view) {
        this.presenter.loginOfflineAction();
    }

    public void onSearchServiceClick(View view) {
        this.buttonSearch.setEnabled(false);

        final CountDownTimer cdt = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                buttonSearch.setText(getString(R.string.searchMessicService_countdown_searching) + " ("
                        + (millisUntilFinished / 1000)
                        + getString(R.string.searchMessicService_countdown_seconds) + ")");
            }

            @Override
            public void onFinish() {
                buttonSearch.post(new Runnable() {
                    public void run() {
                        buttonSearch.setEnabled(true);
                        buttonSearch.setText(R.string.searchMessicService_searchaction);
                    }
                });
                presenter.cancelSearch();
            }
        };
        cdt.start();

        presenter.searchMessicServices(new MessicDiscovering.SearchListener() {
            public boolean messicServiceFound(final MDMMessicServerInstance md) {
                // let's see if the instance was found already
                if (!rvListAdapter.existInstance(md)) {
                    SearchMessicServiceActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            lempty.setVisibility(View.GONE);
                            if (rvListAdapter.addInstance(md)) {
                                rvListAdapter.notifyDataSetChanged();
                            }
                        }
                    });

                    cdt.cancel();
                    cdt.onFinish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onItemTouch(View caller, SearchMessicServiceItemViewHolder holder) {
        MDMMessicServerInstance instance = this.rvListAdapter.getItem(holder.getAdapterPosition());
        SearchMessicServicePresenterImpl.InstanceClickActionCommand command = this.presenter.instanceClickAction(instance);
        if (command instanceof SearchMessicServicePresenterImpl.ShowLoginActivity) {

            Intent ssa = new Intent(SearchMessicServiceActivity.this, LoginActivity.class);

            // Pass data object in the bundle and populate details activity.
            //intent.putExtra(DetailsActivity.EXTRA_CONTACT, contact);
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, holder.vstatus, "status");
            startActivity(ssa, options.toBundle());
            //SearchMessicServiceActivity.this.startActivity(ssa);
        } else {
            Toast.makeText(SearchMessicServiceActivity.this,
                    getString(R.string.searchMessicService_notavailable), Toast.LENGTH_LONG).show();
        }

    }
}
