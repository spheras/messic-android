package org.messic.android.messic_tv.controllers;

import android.app.Activity;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import org.messic.android.messic_tv.R;
import org.messic.android.messic_tv.activities.presenters.SongCardPresenter;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.List;

/**
 * Created by spheras on 22/07/15.
 */
public class SearchController {

    private SparseArray<MDMSong> searchResult = null;

    private boolean flagSearching = false;


    public void searchOnline(String searchContent, final Activity activity, final ArrayObjectAdapter adapter) {
        if (flagSearching) {
            return;
        }

        adapter.clear();

        final String baseURL =
                Configuration.getBaseUrl(activity) + "/services/search?content=" + searchContent + "&messic_token="
                        + Configuration.getLastToken(activity);
        flagSearching = true;
        UtilRestJSONClient.get(activity, baseURL, MDMRandomList.class,
                new UtilRestJSONClient.RestListener<MDMRandomList>() {
                    public void response(MDMRandomList response) {
                        if (searchResult == null) {
                            searchResult = new SparseArray<MDMSong>();
                        }
                        List<MDMSong> songs = response.getSongs();
                        for (int i = 0; i < songs.size(); i++) {
                            MDMSong song = songs.get(i);
                            searchResult.put(i, song);
                        }
                        refreshData(songs, adapter, activity);
                        flagSearching = false;
                    }

                    public void fail(final Exception e) {
                        Log.e("Search", e.getMessage(), e);
                        activity.runOnUiThread(new Runnable() {

                            public void run() {
                                Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show();

                            }
                        });
                        flagSearching = false;
                    }

                });
    }

    private void refreshData(final List<MDMSong> result, final ArrayObjectAdapter adapter, final Activity activity) {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new SongCardPresenter());
                listRowAdapter.addAll(0, result);
                HeaderItem header = new HeaderItem(activity.getString(R.string.searchResults));
                ListRow lr = new ListRow(header, listRowAdapter);
                adapter.add(lr);

                adapter.notifyArrayItemRangeChanged(0, result.size());
            }
        });
    }

}
