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
package org.messic.android.smarttv.activities.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.databinding.TvActivityLoginBinding;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.MessicBaseActivity;
import org.messic.android.smarttv.activities.main.MainActivity;
import org.messic.android.smarttv.rxevents.RxAction;
import org.messic.android.smarttv.rxevents.RxDispatcher;
import org.parceler.Parcels;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends MessicBaseActivity implements SearchMessicServiceItemViewHolder.IViewHolderClicks {
    @Inject
    LoginPresenter presenter;
    @Inject
    MessicPreferences preferences;
    @Inject
    Configuration config;
    private SearchMessicServiceAdapter rvListAdapter;
    private ProgressDialog pdialog;
    private TvActivityLoginBinding binding;
    private View vStatusOnline;
    private View vStatus;
    private TextView vStatusOffline;
    private TextView tHostnameOnline;
    private TextView tHostnameIP;
    private LinearLayout lOnlineLayout;
    private CheckBox cRemember;
    private EditText tUserName;
    private EditText tPassword;
    private Button bLoginAction;
    private Subscription subscription;
    private Button bRemoveServiceAction;
    private RecyclerView lvSearchResults;
    private Button bSearch;
    private TextView tvEmpty;
    private View lLoginPanelContainer;

    @Override
    protected void onStart() {
        super.onStart();
        //we fill with saved data
        List<MDMMessicServerInstance> servers = this.presenter.getSavedSessions();
        if (servers.size() > 0) {
            tvEmpty.setVisibility(View.GONE);
            config.setMessicService(servers.get(0));

            for (MDMMessicServerInstance instance : servers) {
                this.rvListAdapter.addInstance(instance);
            }

            this.rvListAdapter.select(0);
            fillOnline(servers.get(0));

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
        ((MessicSmarttvApp) getApplication()).getSmarttvComponent().inject(this);


        startServices();
        bindData(savedInstanceState);
        setupLayout();
        this.presenter.initialize();
        setupWindowAnimations();
    }

    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
                                                public void call(RxAction event) {
                                                    if (event.isType(LoginEvents.EVENT_FINISH_ACTIVITY)) {
                                                        LoginActivity.this.finish();
                                                    } else if (event.isType(LoginEvents.EVENT_SERVER_STATUS)) {
                                                        UtilNetwork.MessicServerConnectionStatus result = (UtilNetwork.MessicServerConnectionStatus) event.getSimpleData();
                                                        showServerStatus(result.reachable, result.running);
                                                    } else if (event.isType(LoginEvents.EVENT_SHOW_SCREEN)) {
                                                        String screen = (String) event.getSimpleData();

                                                        if (screen.equals(LoginEvents.SCREEN_MAIN)) {
                                                            Intent ssa = new Intent(LoginActivity.this, MainActivity.class);
                                                            LoginActivity.this.startActivity(ssa);
                                                        }

                                                    }

                                                }
                                            }

        );
    }

    /**
     * Binding information form the layout to objects
     */

    private void bindData(Bundle savedInstanceState) {
        this.binding = DataBindingUtil.setContentView(this, R.layout.tv_activity_login);

        LoginActivityBindingImpl user;
        if (savedInstanceState == null) {
            user = new LoginActivityBindingImpl("", "", true, "ccc", "dddd");
        } else {
            user = Parcels.unwrap(savedInstanceState.getParcelable(BINDING_PARCEL));
        }
        this.binding.setUser(user);
        this.binding.setEvents(this);

        this.lvSearchResults = (RecyclerView) findViewById(R.id.login_lvresults);
        this.tvEmpty = (TextView) findViewById(R.id.login_lempty);
        this.bSearch = (Button) findViewById(R.id.login_search_button);
        this.tUserName = (EditText) findViewById(R.id.login_online_tusername);
        this.tPassword = (EditText) findViewById(R.id.login_online_tpassword);
        this.cRemember = (CheckBox) findViewById(R.id.login_online_cbremember);
        this.bLoginAction = (Button) findViewById(R.id.login_online_bloginaction);
        this.bRemoveServiceAction = (Button) findViewById(R.id.login_bremoveservice);
        this.lLoginPanelContainer = findViewById(R.id.login_loginpanel_container);


        this.vStatusOnline = findViewById(R.id.login_online_status);
        this.tHostnameOnline = (TextView) findViewById(R.id.login_online_thostname);
        this.tHostnameIP = (TextView) findViewById(R.id.login_online_thostname_ip);
        this.lOnlineLayout = (LinearLayout) findViewById(R.id.login_online_layout);

        this.rvListAdapter = new SearchMessicServiceAdapter(this);
        this.lvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        this.lvSearchResults.setAdapter(this.rvListAdapter);

        this.tUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    tPassword.requestFocus();
                    return true;
                }
                return false;
            }
        });
        tUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onLoginClick(null);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BINDING_PARCEL, Parcels.wrap(this.binding.getUser()));
    }

    /**
     * setting up the layout of the activity.
     * Here you must put elements, remove elements, manage events, ...
     */
    private void setupLayout() {
        boolean showLogin = this.presenter.fillUserPassword(this.binding.getUser());
        lLoginPanelContainer.setVisibility((showLogin ? View.VISIBLE : View.GONE));
        if (showLogin) {
            bLoginAction.requestFocus();
        } else {
            bSearch.requestFocus();
        }
        onSearchServiceClick(null);
    }

    /**
     * Setting up the animations for this activty, mainly enter and exit transitions
     */

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
//            getWindow().setEnterTransition(transition);
        }
    }

    public void showServerStatus(boolean reachable, boolean running) {
        if (reachable && running) {
            this.vStatus.setBackgroundColor(Color.GREEN);
            vStatusOffline.setText(getString(R.string.login_explanation));
        } else {
            this.vStatus.setBackgroundColor(Color.RED);
            vStatusOffline.setText(getString(R.string.login_server_offline));
        }
    }

    public void showLoginProcess() {

    }

    public void hideLoginProcess() {
        if (pdialog != null) {
            pdialog.dismiss();
        }
    }

    //EVENTS HANDLING
    public void onSearchOnlineClick(View view) {
        this.presenter.searchOnlineAction();
    }

    public void onStatusOnlineClick(View view) {
        this.presenter.statusOnlineAction();
    }

    public void onRemoveClick(View v) {
        if (rvListAdapter.getSelected() >= 0) {
            MDMMessicServerInstance instance = rvListAdapter.getInstances().get(rvListAdapter.getSelected());
            if (instance.lsid != 0) {
                DAOServerInstance dsi = new DAOServerInstance();
                dsi.open();
                dsi.remove(instance);
                dsi.close();
            }

            rvListAdapter.removeItem(rvListAdapter.getSelected());
            rvListAdapter.notifyDataSetChanged();

            if (rvListAdapter.getInstances().size() <= 0) {
                lLoginPanelContainer.setVisibility(View.GONE);
                bSearch.requestFocus();
                this.rvListAdapter.select(-1);
            } else {
                this.rvListAdapter.select(0);
                fillOnline(this.rvListAdapter.getItem(0));
            }
        }

    }

    public void onLoginClick(View view) {
        if (pdialog != null) {
            pdialog.dismiss();
        }
        pdialog = ProgressDialog.show(LoginActivity.this, getString(R.string.login_title), getString(R.string.login_message), true);

        Subscription _subscription = _getLoginOperationObservable()//
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_getLoginOperationObserver());


        LoginActivityBindingImpl user = binding.getUser();
    }

    private Observable<Boolean> _getLoginOperationObservable() {
        return Observable.just(binding.getUser()).map(new Func1<LoginActivityBindingImpl, Boolean>() {
            @Override
            public Boolean call(LoginActivityBindingImpl user) {
                boolean result = presenter.loginAction(rvListAdapter.getItem(rvListAdapter.getSelected()), user.remember.get(), user.username.get(), user.password.get());
                return result;
            }
        });
    }

    public void onSearchServiceClick(View view) {
        this.bSearch.setEnabled(false);

        final CountDownTimer cdt = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                bSearch.setText(getString(R.string.searchMessicService_countdown_searching) + " ("
                        + (millisUntilFinished / 1000)
                        + getString(R.string.searchMessicService_countdown_seconds) + ")");
            }

            @Override
            public void onFinish() {
                bSearch.post(new Runnable() {
                    public void run() {
                        bSearch.setEnabled(true);
                        bSearch.setText(R.string.searchMessicService_searchaction);
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
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            LoginActivity.this.tvEmpty.setVisibility(View.GONE);
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

    /**
     * Observer that handles the result through the 3 important actions:
     * <p/>
     * 1. onCompleted
     * 2. onError
     * 3. onNext
     */
    private Observer<Boolean> _getLoginOperationObserver() {
        return new Observer<Boolean>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error in Login");
                if (pdialog != null) {
                    pdialog.dismiss();
                }
                Toast.makeText(LoginActivity.this, "Error!!", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onNext(Boolean bool) {
                if (bool) {
                    Timber.d("Login completed");
                    if (pdialog != null) {
                        pdialog.dismiss();
                    }
                } else {
                    if (pdialog != null) {
                        pdialog.dismiss();
                    }
                    Toast.makeText(LoginActivity.this, "Login error, check your credentials and try again", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    public void onItemSelect(View caller, SearchMessicServiceItemViewHolder holder) {
        this.rvListAdapter.select(holder.getAdapterPosition());
        fillOnline(holder.instance);
    }

    /**
     * set the behaviour and content of the online fields
     */
    private void fillOnline(MDMMessicServerInstance instance) {
        findViewById(R.id.login_loginpanel).setVisibility(View.VISIBLE);

        switch (instance.getLastCheckedStatus()) {
            case MDMMessicServerInstance.STATUS_UNKNOWN:
                findViewById(R.id.login_online_status).setBackgroundColor(Color.parseColor("#FFFF00"));
                break;
            case MDMMessicServerInstance.STATUS_DOWN:
                findViewById(R.id.login_online_status).setBackgroundColor(Color.parseColor("#FF0000"));
                break;
            case MDMMessicServerInstance.STATUS_RUNNING:
                findViewById(R.id.login_online_status).setBackgroundColor(Color.parseColor("#00FF00"));
                break;
        }

        LoginActivityBinding user = this.binding.getUser();
        user.setServername(instance.name);
        user.setServerip(instance.ip);
        user.setRemember(preferences.getRemember());
        if (preferences.getRemember()) {
            String suser = instance.lastUser;//Configuration.getLastMessicUser();
            user.setUsername((suser != null ? suser : ""));
            String password = instance.lastPassword;//Configuration.getLastMessicPassword();
            user.setPassword((password != null ? password : ""));
            bLoginAction.requestFocus();
        } else {
            user.setUsername("");
            user.setPassword("");
            tUserName.requestFocus();
        }

        lLoginPanelContainer.setVisibility(View.VISIBLE);
    }
}
