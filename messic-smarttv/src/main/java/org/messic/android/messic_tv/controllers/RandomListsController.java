package org.messic.android.messic_tv.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.messic_tv.activities.fragments.MainFragment;
import org.messic.android.messic_tv.activities.recommendations.UpdateRecommendationsService;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.util.UtilRestJSONClient;

/**
 * Created by spheras on 22/07/15.
 */
public class RandomListsController {

    public void loadRandomPlaylists(final MainFragment ctx) {
        final String baseURL =
                Configuration.getBaseUrl(ctx.getActivity())
                        + "/services/randomlists?messic_token="
                        + Configuration.getLastToken();
        UtilRestJSONClient.get(ctx.getActivity(), baseURL, MDMRandomList[].class,
                new UtilRestJSONClient.RestListener<MDMRandomList[]>() {
                    public void response(MDMRandomList[] response) {
                        ctx.loadRows(response);
                    }

                    public void fail(final Exception e) {
                        Log.e("Random", e.getMessage(), e);
                        if (ctx instanceof Fragment) {
                            ((Activity) ctx.getActivity()).runOnUiThread(new Runnable() {

                                public void run() {
                                    Toast.makeText(ctx.getActivity(), "Server Error", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }

                });
    }

    public void loadRecommendations(final UpdateRecommendationsService recommendationsService){
        Context ctx = recommendationsService.getApplicationContext();
        final String baseURL =
                Configuration.getBaseUrl(recommendationsService.getApplicationContext())
                        + "/services/randomlists?messic_token="
                        + Configuration.getLastToken();
        UtilRestJSONClient.get(ctx, baseURL, MDMRandomList[].class,
                new UtilRestJSONClient.RestListener<MDMRandomList[]>() {
                    public void response(MDMRandomList[] response) {
                        recommendationsService.updateRecommendations(response);
                    }

                    public void fail(final Exception e) {
                        Log.e("Random", e.getMessage(), e);
                    }

                });
    }

}
