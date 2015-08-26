package org.messic.android.messic_tv.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.messic_tv.activities.fragments.MainFragment;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.util.UtilRestJSONClient;

/**
 * Created by spheras on 22/07/15.
 */
public class SongController {

    public void loadSongs(final MainFragment ctx, final MDMAlbum album) {
        final String baseURL =
                Configuration.getBaseUrl(ctx.getActivity())
                        + "/services/albums/" + album.getSid() + "?songsInfo=true&messic_token="
                        + Configuration.getLastToken();
        UtilRestJSONClient.get(ctx.getActivity(), baseURL, MDMAlbum[].class,
                new UtilRestJSONClient.RestListener<MDMAlbum[]>() {
                    public void response(MDMAlbum[] response) {
                        System.out.println();
                        //ctx.loadRows(response);
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

}
