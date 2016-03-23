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
package org.messic.android.smartphone.activities.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.smartphone.activities.MessicBaseActivity;
import org.messic.android.smartphone.activities.main.MainActivity;
import org.messic.android.smartphone.activities.searchmessicservice.SearchMessicServiceActivity;
import org.messic.android.smartphone.activities.welcome.WelcomeActivity;
import org.messic.android.smartphone.rxevents.RxAction;
import org.messic.android.smartphone.rxevents.RxDispatcher;
import org.messic.android.databinding.ActivityLoginBinding;
import org.messic.android.messiccore.util.UtilNetwork;
import org.parceler.Parcels;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LoginActivity extends MessicBaseActivity {
    @Inject
    LoginPresenter presenter;

    private ProgressDialog pdialog;
    private ActivityLoginBinding binding;
    private View vStatusOnline;
    private View vStatus;
    private TextView vStatusOffline;
    private Button bSearchOnline;
    private Button bSearchOffline;
    private TextView tHostnameOnline;
    private TextView tHostnameIP;
    private LinearLayout lOnlineLayout;
    private CheckBox cRemember;
    private EditText tUserName;
    private EditText tPassword;
    private Button bLoginAction;
    private Subscription subscription;

    @Override
    protected void onStart() {
        super.onStart();
        // put the layout considering the situation
        setupLayout();
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
        //setupLayout(); this time we will do it at onStart
        setupToolbar(false);
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

                    if (screen.equals(LoginEvents.SCREEN_SEARCH_MESSSIC_SERVICE)) {
                        Intent ssa = new Intent(LoginActivity.this, SearchMessicServiceActivity.class);
                        LoginActivity.this.startActivity(ssa);
                        finish();

                    } else if (screen.equals(LoginEvents.SCREEN_MAIN)) {
                        Intent ssa = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(ssa);
                    }

                }
            }
        });
    }

    /**
     * Binding information form the layout to objects
     */
    private void bindData(Bundle savedInstanceState) {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        LoginActivityBindingImpl user;
        if (savedInstanceState == null) {
            user = new LoginActivityBindingImpl("", "", true, "ccc", "dddd");
        } else {
            user = Parcels.unwrap(savedInstanceState.getParcelable(BINDING_PARCEL));
        }

        this.binding.setUser(user);
        this.binding.setEvents(this);

        this.vStatusOnline = findViewById(R.id.login_online_status);
        this.vStatus = findViewById(R.id.searchmessicservice_item_vstatus);
        this.vStatusOffline = (TextView) findViewById(R.id.login_offline_description);
        this.bSearchOnline = (Button) findViewById(R.id.login_searchonline_button);
        this.bSearchOffline = (Button) findViewById(R.id.login_boffline);

        this.tHostnameOnline = (TextView) findViewById(R.id.login_online_thostname);
        this.tHostnameIP = (TextView) findViewById(R.id.login_online_thostname_ip);
        this.lOnlineLayout = (LinearLayout) findViewById(R.id.login_online_layout);
        this.cRemember = (CheckBox) findViewById(R.id.login_online_cbremember);

        this.tUserName = (EditText) findViewById(R.id.login_online_tusername);
        this.tPassword = (EditText) findViewById(R.id.login_online_tpassword);
        this.bLoginAction = (Button) findViewById(R.id.login_online_bloginaction);

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
        LoginPresenterImpl.ShowControl result = this.presenter.selectLayout(this.binding.getUser());
        if (result != null) {
            if (result.showWelcomeActivity) {
                Intent ssa = new Intent(this, WelcomeActivity.class);
                this.startActivity(ssa);
            } else if (result.showSearchActivity) {
                RxDispatcher.get().unsubscribe(subscription);
                // then the user should first go to search a valid messic service
                Intent ssa = new Intent(this, SearchMessicServiceActivity.class);
                this.startActivity(ssa);
                finish();
            } else {
                showLoginOffline(result.showLoginOffline);
                showLoginOnline(result.showLoginOnline);
                showSearchOnline(result.showSearchOnline);
            }
        }
    }

    /**
     * Setting up the animations for this activty, mainly enter and exit transitions
     */

    private void setupWindowAnimations() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
//            getWindow().setEnterTransition(transition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_login_action_scan) {
            Intent ssa = new Intent(LoginActivity.this, SearchMessicServiceActivity.class);
            LoginActivity.this.startActivity(ssa);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showServerStatus(boolean reachable, boolean running) {
        if (reachable && running) {
            this.vStatusOnline.setBackgroundResource(R.drawable.ic_check_circle_green_24dp);
            this.vStatus.setBackgroundColor(Color.GREEN);
            vStatusOffline.setText(getString(R.string.login_explanation));
        } else {
            this.vStatusOnline.setBackgroundResource(R.drawable.ic_highlight_off_red_24dp);
            this.vStatus.setBackgroundColor(Color.RED);
            vStatusOffline.setText(getString(R.string.login_server_offline));
        }
    }


    private void showSearchOnline(boolean show) {
        this.bSearchOnline.setVisibility((show ? View.VISIBLE : View.GONE));
    }

    private void showLoginOffline(boolean show) {
        this.bSearchOffline.setVisibility((show ? View.VISIBLE : View.GONE));
    }

    private void showLoginOnline(boolean show) {
        this.vStatusOnline.setVisibility((show ? View.VISIBLE : View.GONE));
        this.tHostnameOnline.setVisibility((show ? View.VISIBLE : View.GONE));
        this.tHostnameIP.setVisibility((show ? View.VISIBLE : View.GONE));
        this.lOnlineLayout.setVisibility((show ? View.VISIBLE : View.GONE));
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

    public void onLoginOfflineClick(View view) {
        this.presenter.loginOfflineAction();
    }

    public void onStatusOnlineClick(View view) {
        this.presenter.statusOnlineAction();
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
                boolean result = presenter.loginAction(user.remember.get(), user.username.get(), user.password.get());
                return result;
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
}
