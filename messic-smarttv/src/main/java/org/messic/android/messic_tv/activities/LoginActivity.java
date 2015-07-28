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
import android.os.Bundle;

import org.messic.android.messic_tv.controllers.LoginController;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMMessicServerInstance;

/*
 * MainActivity class that loads MainFragment
 */
public class LoginActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MDMMessicServerInstance instance=new MDMMessicServerInstance();
        instance.description="local test messic";
        instance.ip="10.0.2.2";//the emulator need to connect with the gateway to see the host machine "127.0.0.1";
        instance.port=8080;
        instance.secured=false;
        Configuration.setMessicService(this,instance);

        LoginController lc = new LoginController();
        try {
            lc.login(this, true, "spheras", "messic", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
