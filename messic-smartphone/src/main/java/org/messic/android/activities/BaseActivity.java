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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.activities.fragments.DownloadedFragment;
import org.messic.android.activities.fragments.ExploreFragment;
import org.messic.android.activities.fragments.PlayQueueFragment;
import org.messic.android.activities.fragments.PlaylistFragment;
import org.messic.android.activities.fragments.RandomFragment;
import org.messic.android.activities.fragments.SearchFragment;
import org.messic.android.activities.fragments.TitleFragment;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.activities.controllers.LoginController;
import org.messic.android.activities.controllers.RandomController;
import org.messic.android.messiccore.util.UtilDownloadService;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseActivity
        extends Activity
        implements ActionBar.TabListener, OnQueryTextListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory. If this becomes too
     * memory intensive, it may be best to switch to a {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private List<Fragment> fragments = new ArrayList<Fragment>();

    private SearchView mSearchView;

    private static final String STATE_SEARCHFRAGMENTS = "search_fragments";

    private static final String STATE_SELECTEDTAB = "selected_tab";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        int index = getActionBar().getSelectedNavigationIndex();
        savedInstanceState.putInt(STATE_SELECTEDTAB, index);

        ArrayList<String> searchFragments = new ArrayList<String>();
        for (int i = 0; i < fragments.size(); i++) {
            Fragment f = fragments.get(i);
            if (f instanceof SearchFragment) {
                SearchFragment sf = (SearchFragment) f;
                searchFragments.add(sf.getSearchContent());
            }
        }

        if (searchFragments != null && searchFragments.size() > 0) {
            // Save the user's current frame state
            savedInstanceState.putStringArrayList(STATE_SEARCHFRAGMENTS, searchFragments);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }

        if (savedInstanceState != null) {
            ArrayList<String> searchFragments = savedInstanceState.getStringArrayList(STATE_SEARCHFRAGMENTS);
            if (searchFragments != null && searchFragments.size() > 0) {
                for (String string : searchFragments) {
                    createSearchFragment(getActionBar(), fragments.size(), string);
                }
            }

            int index = savedInstanceState.getInt(STATE_SELECTEDTAB);
            Tab tabindex = getActionBar().getTabAt(index);
            getActionBar().selectTab(tabindex);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.base_activity_actions, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.action_search_hint));
        mSearchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                // openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void openSettings() {
        View anchor = findViewById(R.id.action_settings);

        // Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(this, anchor);

        // Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.menu_base, popup.getMenu());

        // registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_base_item_removedatabase:
                        emptyDatabase();
                        break;
                    case R.id.menu_base_item_logout:
                        logout();
                        break;
                    case R.id.menu_base_item_clearplaylist:
                        UtilMusicPlayer.clearQueue(BaseActivity.this);
                        break;
                    case R.id.menu_base_item_playrandom:
                        RandomController.addRandomSongsToPlaylist(BaseActivity.this);
                        break;
                    case R.id.menu_base_item_showlicense:
                        Intent browserIntent =
                                new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.gnu.org/licenses/gpl-3.0-standalone.html"));
                        startActivity(browserIntent);

                        break;
                }
                return true;
            }
        });

        popup.show();// showing popup menu
    }

    private void emptyDatabase() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DialogInterface.OnClickListener reallySure = new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:

                                        final ProgressDialog pd =
                                                ProgressDialog.show(BaseActivity.this, BaseActivity.this.getString(R.string.action_emptying_localdatabase_title),
                                                        BaseActivity.this.getString(R.string.action_emptying_localdatabase_content), true);

                                        UtilDownloadService.emptyLocal(BaseActivity.this, pd);

                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        // No button clicked
                                        break;
                                }
                            }
                        };

                        builder.setMessage(getString(R.string.action_empty_localdatabase_reallysure));
                        builder.setPositiveButton(getString(R.string.yes), reallySure);
                        builder.setNegativeButton(getString(R.string.no), reallySure);
                        builder.show();

                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        break;
                }
            }
        };
        builder.setMessage(getString(R.string.action_empty_localdatabase));
        builder.setPositiveButton(getString(R.string.yes), dialogClickListener);
        builder.setNegativeButton(getString(R.string.no), dialogClickListener);
        builder.show();

    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter
            extends FragmentPagerAdapter {

        private void createBasicFragments() {
            Locale l = Locale.getDefault();
            if (Configuration.isOffline(BaseActivity.this)) {
                fragments.add(new RandomFragment(getString(R.string.title_section_random).toUpperCase(l)));
                fragments.add(new DownloadedFragment(getString(R.string.title_section_downloaded).toUpperCase(l)));
                fragments.add(new PlayQueueFragment(getString(R.string.title_section_queue).toUpperCase(l)));
            } else {
                fragments.add(new RandomFragment(getString(R.string.title_section_random).toUpperCase(l)));
                fragments.add(new ExploreFragment(getString(R.string.title_section_explore).toUpperCase(l)));
                fragments.add(new PlaylistFragment(getString(R.string.title_section_playlist).toUpperCase(l)));
                fragments.add(new DownloadedFragment(getString(R.string.title_section_downloaded).toUpperCase(l)));
                fragments.add(new PlayQueueFragment(getString(R.string.title_section_queue).toUpperCase(l)));

            }

        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            createBasicFragments();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
            // // getItem is called to instantiate the fragment for the given page.
            // // Return a PlaceholderFragment (defined as a static inner class below).
            // return PlaceholderFragment.newInstance( position + 1 );
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ((TitleFragment) fragments.get(position)).getTitle();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment
            extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_base, container, false);
            return rootView;
        }
    }

    public boolean onQueryTextChange(String newText) {
        // Toast.makeText( this, newText, Toast.LENGTH_SHORT ).show();
        return false;
    }

    public boolean onQueryTextSubmit(String text) {

        Toast.makeText(this, getString(R.string.action_searchmusic_toast) + " " + text, Toast.LENGTH_LONG).show();

        ActionBar actionBar = getActionBar();
        int i = fragments.size();
        createSearchFragment(actionBar, i, text);
        actionBar.setSelectedNavigationItem(i);

        mSearchView.clearFocus();
        return true;
    }

    private void createSearchFragment(ActionBar actionBar, int i, String text) {
        fragments.add(new SearchFragment(getString(R.string.title_section_search), text, i));
        actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));

        mSectionsPagerAdapter.notifyDataSetChanged();

    }

    public void removeSearchTab(int index, SearchFragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(fragment);
        trans.commit();

        getActionBar().removeTabAt(index);
        fragments.remove(index);
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(null);
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mSectionsPagerAdapter.notifyDataSetChanged();

        if (index > 0) {
            getActionBar().selectTab(getActionBar().getTabAt(index - 1));
        }
    }

    private void logout() {
        LoginController lc = new LoginController();
        lc.logout(this);
    }
}
