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
package org.messic.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.activities.adapters.SearchMessicServiceAdapter;
import org.messic.android.activities.controllers.LoginController;
import org.messic.android.activities.controllers.SearchMessicServiceController;
import org.messic.android.activities.notifications.DownloadNotification;
import org.messic.android.activities.notifications.MessicPlayerNotification;
import org.messic.android.activities.swipedismiss.SwipeDismissListViewTouchListener;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.controllers.messicdiscovering.MessicDiscovering;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;

public class SearchManualMessicServiceActivity
        extends Activity {

    private SearchMessicServiceController controller = new SearchMessicServiceController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_messic_service_manual);


        findViewById(R.id.searchmessicservice_manual_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchManualMessicServiceActivity.this.finish();
            }
        });

        findViewById(R.id.searchmessicservice_manual_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConsistency()) {
                    //lets save
                    //@TODO
                    DAOServerInstance dao = new DAOServerInstance(SearchManualMessicServiceActivity.this);
                    dao.open();

                    MDMMessicServerInstance msi = new MDMMessicServerInstance();
                    msi.name = ((EditText) findViewById(R.id.searchmessicservice_manual_name_edit)).getText().toString();
                    msi.ip = ((EditText) findViewById(R.id.searchmessicservice_manual_ip_edit)).getText().toString();
                    msi.secured = ((CheckBox) findViewById(R.id.searchmessicservice_manual_secured_edit)).isChecked();
                    msi.description = ((EditText) findViewById(R.id.searchmessicservice_manual_description_edit)).getText().toString();
                    String sport = ((EditText) findViewById(R.id.searchmessicservice_manual_port_edit)).getText().toString();
                    msi.port = Integer.valueOf(sport);

                    dao.save(msi);

                    dao.close();

                    finish();
                } else {
                    Toast.makeText(SearchManualMessicServiceActivity.this, "You must fill all the fields", Toast.LENGTH_LONG);
                }
            }
        });
    }

    private boolean checkConsistency() {

        EditText et = ((EditText) findViewById(R.id.searchmessicservice_manual_name_edit));
        boolean result = true;
        result = result && checkConsistencyField(et);

        et = ((EditText) findViewById(R.id.searchmessicservice_manual_ip_edit));
        result = result && checkConsistencyField(et);

        et = ((EditText) findViewById(R.id.searchmessicservice_manual_port_edit));
        result = result && checkConsistencyField(et);

        return result;
    }

    private boolean checkConsistencyField(EditText et) {
        if (et.getText().toString().trim().length() <= 0) {
            et.setError("You must fill this field");
            return false;
        } else {
            return true;
        }
    }
}
