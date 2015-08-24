/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.messic.android.messic_tv.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.messic.android.messic_tv.R;
import org.messic.android.messic_tv.activities.adapters.SearchMessicServiceAdapter;
import org.messic.android.messic_tv.controllers.LoginController;
import org.messic.android.messic_tv.controllers.SearchMessicServiceController;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.MessicPreferences;

/*
 * MainActivity class that loads MainFragment
 */
public class LoginActivity extends Activity implements SearchMessicServiceAdapter.SearchMessicServiceAdapterListener {

    private SearchMessicServiceAdapter adapter;
    private SearchMessicServiceController smController;
    private LoginController loginController;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        this.smController = new SearchMessicServiceController();
        this.loginController = new LoginController();

        this.adapter = new SearchMessicServiceAdapter(this, this);
        //smController.getSavedSessions(this, adapter);
        ListView lv = (ListView) findViewById(R.id.login_lvresults);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        lv.setAdapter(adapter);


        ((Button) findViewById(R.id.login_search_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchMesicServices();
            }
        });

        ((Button) findViewById(R.id.login_bremoveservice)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                removeSelectedService();
            }
        });

        layoutScreen();

        lv.setFocusable(true);
        lv.requestFocus();
        lv.setSelection(0);

        lv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.d("focus changed", "" + b);
            }
        });


        lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("itemselected", "" + i);
                adapter.select(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d("itemselected", "nothing");
            }
        });

        if (adapter.getCount() > 0) {
            findViewById(R.id.login_lempty).setVisibility(View.GONE);
        }

//        MDMMessicServerInstance instance = new MDMMessicServerInstance();
//        instance.description = "local test messic";
//        instance.ip = "10.0.2.2"; //the emulator need to connect with the gateway (10.0.2.2) to see the host machine "127.0.0.1";
//        instance.port = 8080;
//        instance.secured = false;
//        Configuration.setMessicService(this, instance);
//        UtilMusicPlayer.startMessicMusicService(this, new MessicPlayerNotification());
//
//        LoginController lc = new LoginController();
//        try {
//            lc.login(this, true, "spheras", "messic", null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private void removeSelectedService() {
        if (adapter.getSelected() >= 0) {
            MDMMessicServerInstance instance = adapter.getInstances().get(adapter.getSelected());
            if (instance.lsid != 0) {
                DAOServerInstance dsi = new DAOServerInstance(this);
                dsi.open();
                dsi.remove(instance);
                //dsi._recreate();
                dsi.close();
            }
        }

        adapter.removeItem(adapter.getSelected());
        adapter.notifyDataSetChanged();
        adapter.select(0);
    }

    private void searchMesicServices() {
        final Button b = ((Button) findViewById(R.id.login_search_button));
        b.setEnabled(false);

        final CountDownTimer cdt = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                b.setText(getString(R.string.searchMessicService_countdown_searching) + " ("
                        + (millisUntilFinished / 1000)
                        + getString(R.string.searchMessicService_countdown_seconds) + ")");
            }

            @Override
            public void onFinish() {
                b.post(new Runnable() {
                    public void run() {
                        b.setEnabled(true);
                        b.setText(R.string.searchMessicService_searchaction);
                    }
                });
                smController.cancelSearch();
            }
        };
        cdt.start();

        smController.searchMessicServices(new MessicDiscovering.SearchListener() {

            public boolean messicServiceFound(final MDMMessicServerInstance md) {
                // let's see if the instance was found already
                if (!adapter.existInstance(md)) {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            findViewById(R.id.login_lempty).setVisibility(View.GONE);
                            if (adapter.addInstance(md)) {
                                adapter.addInstance(md);

                                MDMMessicServerInstance tmpi = new MDMMessicServerInstance();
                                tmpi.description = "temporal";
                                tmpi.ip = "127.1273.423.4";
                                tmpi.lastCheckedStatus = 0;
                                tmpi.lastUser = "raro";
                                tmpi.lastPassword = "rarito";
                                tmpi.port = 80;
                                tmpi.secured = false;
                                adapter.addInstance(tmpi);

                                tmpi = new MDMMessicServerInstance();
                                tmpi.description = "temporal2";
                                tmpi.ip = "127.1273.423.5";
                                tmpi.lastCheckedStatus = 1;
                                tmpi.lastUser = "raro333";
                                tmpi.lastPassword = "rarito44";
                                tmpi.port = 8080;
                                tmpi.secured = true;
                                adapter.addInstance(tmpi);

                                adapter.notifyDataSetChanged();
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


    private void layoutScreen() {
        findViewById(R.id.login_loginpanel).setVisibility(View.GONE);

        //0. We fill the last saved sessions
        this.smController.getSavedSessions(this, adapter);

        if (adapter.getCount() == 0) {
            //And search messic services, if any others
            searchMesicServices();
        }

    }

    /**
     * set the behaviour and content of the online fields
     */
    private void fillOnline(MDMMessicServerInstance instance) {
        findViewById(R.id.login_loginpanel).setVisibility(View.VISIBLE);

        MessicPreferences p = new MessicPreferences(this);

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


        ((TextView) findViewById(R.id.login_online_thostname)).setText(instance.name);
        ((TextView) findViewById(R.id.login_online_thostname_ip)).setText(instance.ip);
        ((CheckBox) findViewById(R.id.login_online_cbremember)).setChecked(p.getRemember());
        TextView tlogin = ((TextView) findViewById(R.id.login_online_tusername));
        final TextView tpass = ((TextView) findViewById(R.id.login_online_tpassword));
        if (p.getRemember()) {
            String user = instance.lastUser;//Configuration.getLastMessicUser();
            tlogin.setText((user != null ? user : ""));
            String password = instance.lastPassword;//Configuration.getLastMessicPassword();
            tpass.setText((password != null ? password : ""));
        }

        tlogin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    tpass.requestFocus();
                    return true;
                }
                return false;
            }
        });
        tpass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    login();
                    return true;
                }
                return false;
            }
        });

        Button loginaction = (Button) findViewById(R.id.login_online_bloginaction);
        loginaction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });
        //loginaction.requestFocus();
    }

    /**
     * perform login process
     */
    private void login() {
        String username = ((TextView) findViewById(R.id.login_online_tusername)).getText().toString();
        String password = ((TextView) findViewById(R.id.login_online_tpassword)).getText().toString();
        boolean remember = ((CheckBox) findViewById(R.id.login_online_cbremember)).isChecked();
        try {
            ProgressDialog dialog =
                    ProgressDialog.show(LoginActivity.this, getString(R.string.login_title),
                            getString(R.string.login_message), true);
            Configuration.setMessicService(this, adapter.getInstances().get(adapter.getSelected()));
            loginController.login(LoginActivity.this, remember, username, password, dialog);
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Error while trying to login", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void selected(MDMMessicServerInstance instance) {
        fillOnline(instance);
    }
}
