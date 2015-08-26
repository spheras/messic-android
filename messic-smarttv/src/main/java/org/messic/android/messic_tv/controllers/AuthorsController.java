package org.messic.android.messic_tv.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.messic_tv.activities.fragments.MainFragment;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.List;

/**
 * Created by spheras on 22/07/15.
 */
public class AuthorsController {

    public void loadAuthors(final MainFragment ctx) {
        final String baseURL =
                Configuration.getBaseUrl(ctx.getActivity())
                        + "/services/authors?albumsInfo=true&songsInfo=false&messic_token="
                        + Configuration.getLastToken();
        UtilRestJSONClient.get(ctx.getActivity(), baseURL, MDMAuthor[].class,
                new UtilRestJSONClient.RestListener<MDMAuthor[]>() {
                    public void response(MDMAuthor[] response) {
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

    public void loadAuthor(final MainFragment ctx, final MDMAuthor author, final ArrayObjectAdapter aoa) {
        final String baseURL =
                Configuration.getBaseUrl(ctx.getActivity())
                        + "/services/authors/" + author.getSid() + "?albumsInfo=true&songsInfo=true&messic_token="
                        + Configuration.getLastToken();
        UtilRestJSONClient.get(ctx.getActivity(), baseURL, MDMAuthor.class,
                new UtilRestJSONClient.RestListener<MDMAuthor>() {
                    public void response(final MDMAuthor response) {
                        ctx.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aoa.clear();
                                List<MDMAlbum> albums = response.getAlbums();
                                for (int i = 0; i < albums.size(); i++) {
                                    MDMAlbum album = albums.get(i);
                                    for (int j = 0; j < album.getSongs().size(); j++) {
                                        MDMSong song = album.getSongs().get(j);
                                        album.setAuthor(author);
                                        song.setAlbum(album);
                                        aoa.add(song);
                                    }
                                }

                                aoa.notifyArrayItemRangeChanged(0, aoa.size());
                            }
                        });
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
