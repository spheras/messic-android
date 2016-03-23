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
package org.messic.android.smartphone.activities.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.smartphone.activities.main.fragments.explore.ExploreFragment;
import org.messic.android.smartphone.activities.main.fragments.downloaded.DownloadedFragment;
import org.messic.android.smartphone.activities.main.fragments.playlist.PlaylistFragment;
import org.messic.android.smartphone.activities.main.fragments.queue.PlayQueueFragment;
import org.messic.android.smartphone.activities.main.fragments.random.RandomFragment;
import org.messic.android.smartphone.activities.main.fragments.search.SearchFragment;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
 */
public class MainFragmentAdapter
        extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList();
    private final List<String> fragmentTitles = new ArrayList();
    private final List<Integer> fragmentIcons = new ArrayList();

    @Inject
    Configuration config;

    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);

        createBasicFragments();
    }

    public int getFragmentIcon(int pos) {
        return fragmentIcons.get(pos);
    }

    private void createBasicFragments() {
        Locale l = Locale.getDefault();

        if (config.isOffline()) {
            fragmentList.add(new RandomFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_random));
            fragmentIcons.add(R.drawable.ic_shuffle_white_24dp);

            fragmentList.add(new DownloadedFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_downloaded));
            fragmentIcons.add(R.drawable.ic_cloud_download_white_24dp);

            fragmentList.add(new PlayQueueFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_queue));
            fragmentIcons.add(R.drawable.ic_library_music_white_24dp);
        } else {
            fragmentList.add(new RandomFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_random));
            fragmentIcons.add(R.drawable.ic_shuffle_white_24dp);

            fragmentList.add(new ExploreFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_explore));
            fragmentIcons.add(R.drawable.ic_view_module_white_24dp);

            fragmentList.add(new PlaylistFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_playlist));
            fragmentIcons.add(R.drawable.ic_playlist_add_check_white_24dp);

            fragmentList.add(new DownloadedFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_downloaded));
            fragmentIcons.add(R.drawable.ic_cloud_download_white_24dp);

            fragmentList.add(new PlayQueueFragment());
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_queue));
            fragmentIcons.add(R.drawable.ic_library_music_white_24dp);
        }

    }

    public SearchFragment addSearchTab(String searchContent) {
        int stindex = getSearchTabIndex();
        if (stindex >= 0) {
            SearchFragment sf = (SearchFragment) fragmentList.get(stindex);
            return sf;
        } else {
            SearchFragment sf = new SearchFragment();
//            Bundle args = new Bundle();
//            args.putString(SearchFragment.SEARCH_CONTENT_KEY, searchContent);
//            sf.setArguments(args);
            fragmentList.add(sf);
            fragmentTitles.add(MessicCoreApp.getInstance().getString(R.string.title_section_search));
            fragmentIcons.add(R.drawable.ic_search_white_24dp);
            notifyDataSetChanged();
            return sf;
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public String getDescription(int position) {
        return fragmentTitles.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
        //this worked with the last appcompat version 23.1.1, now (23.2.0) we need to setIcon instead :(
//        Drawable image = ContextCompat.getDrawable(MessicCoreApp.getInstance(), fragmentIcons.get(position));
//        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
//        SpannableString sb = new SpannableString(" ");
//        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
//        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return sb;
    }


    public int getSearchTabIndex() {
        for (int i = 0; i < fragmentList.size(); i++) {
            Fragment f = fragmentList.get(i);
            if (f instanceof SearchFragment) {
                return i;
            }
        }
        return -1;
    }

    public void removeSearch(int i) {
        super.destroyItem(null, i, fragmentList.get(i));
        fragmentList.remove(i);
        fragmentTitles.remove(i);
        fragmentIcons.remove(i);
        super.finishUpdate(null);
        notifyDataSetChanged();
    }
}