package org.messic.android.messic_tv.controllers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.messic_tv.activities.MainActivity;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMLogin;
import org.messic.android.messiccore.util.MessicPreferences;
import org.messic.android.messiccore.util.UtilNetwork;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Created by spheras on 22/07/15.
 */
public class LoginController {

    public void login(final Activity context, final boolean remember, final String username, final String password,
                      final ProgressDialog pd)
            throws Exception {
        UtilNetwork.nukeNetwork();

        final String baseURL = Configuration.getBaseUrl(context) + "/messiclogin";
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.add("j_username", username);
        formData.add("j_password", password);
        try {
            UtilRestJSONClient.post(context, baseURL, formData, MDMLogin.class,
                    new UtilRestJSONClient.RestListener<MDMLogin>() {
                        public void response(MDMLogin response) {
                            MessicPreferences mp = new MessicPreferences(context);
                            mp.setRemember(remember, username, password);
                            Configuration.setToken(response.getMessic_token());

                            if (pd != null) {
                                pd.dismiss();
                            }
                            Configuration.setOffline(false);
                            Intent ssa = new Intent(context, MainActivity.class);
                            context.startActivity(ssa);
                        }

                        public void fail(Exception e) {
                            Log.e("Login", e.getMessage(), e);
                            context.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (pd != null) {
                                        pd.dismiss();
                                    }
                                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });
        } catch (Exception e) {
            if (pd != null) {
                pd.dismiss();
            }
            Log.e("login", e.getMessage(), e);
            throw e;
        }
    }
}
