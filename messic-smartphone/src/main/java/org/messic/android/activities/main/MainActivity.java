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
package org.messic.android.activities.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.messic.android.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.activities.MessicBaseActivity;
import org.messic.android.activities.login.LoginActivity;
import org.messic.android.activities.main.fragments.downloaded.DownloadedAlbumAdapter;
import org.messic.android.activities.main.fragments.explore.ExploreAuthorsAdapter;
import org.messic.android.activities.main.fragments.playlist.PlaylistAdapter;
import org.messic.android.activities.main.fragments.queue.PlayQueueSongAdapter;
import org.messic.android.activities.main.fragments.random.RandomSongAdapter;
import org.messic.android.activities.main.fragments.search.SearchFragment;
import org.messic.android.rxevents.RxAction;
import org.messic.android.rxevents.RxDispatcher;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity
        extends MessicBaseActivity implements ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener {

    @Inject
    MainPresenter presenter;
    @Inject
    RandomSongAdapter adapterRandomSongAdapter;
    @Inject
    ExploreAuthorsAdapter adapterExploreAuthorsAdapter;
    @Inject
    PlaylistAdapter adapterPlaylistAdapter;
    @Inject
    DownloadedAlbumAdapter adapterDownloadedAlbumAdapter;
    @Inject
    PlayQueueSongAdapter adapterPlayQueueSongAdapter;


    private Subscription subscription;
    private TabLayout mTabs;
    private ViewPager tabsviewPager;
    private TextView tabTitle;
    private MainFragmentAdapter mTabsAdapter;
    private SearchView mSearchView;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.presenter.resume();

        if (this.subscription != null)
            RxDispatcher.get().unsubscribe(this.subscription);

        this.subscription = subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.presenter.pause();
        RxDispatcher.get().unsubscribe(this.subscription);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) getApplication()).getSmartphoneComponent().inject(this);

        startServices();
        bindData(savedInstanceState);
        setupLayout();
        setupToolbar(false);
        this.presenter.initialize();
        setupWindowAnimations();
    }

    private Subscription subscribe() {
        return RxDispatcher.get().subscribe(new RxDispatcher.RxSubscriber() {
            public void call(RxAction event) {
//                if (event.isType(LoginEvents.EVENT_FINISH_ACTIVITY)) {
//                    LoginActivity.this.finish();
//                }
            }
        });
    }

    /**
     * Binding information form the layout to objects
     */
    private void bindData(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        tabsviewPager = (ViewPager) findViewById(R.id.main_tabspager);
        mTabsAdapter = new MainFragmentAdapter(getSupportFragmentManager());


        this.tabTitle = (TextView) findViewById(R.id.main_tab_title);

        //setup viewpager to give swipe effect
        tabsviewPager.setAdapter(mTabsAdapter);

        tabsviewPager.addOnPageChangeListener(this);

        mTabs = (TabLayout) findViewById(R.id.main_tabs);
        mTabs.setupWithViewPager(tabsviewPager);
        updateTabIcons();

//        LoginActivityBindingImpl user;
//        if (savedInstanceState == null) {
//            user = new LoginActivityBindingImpl("", "", true, "ccc", "dddd");
//        } else {
//            user = Parcels.unwrap(savedInstanceState.getParcelable(BINDING_PARCEL));
//        }

        adapterRandomSongAdapter.clear();
        adapterExploreAuthorsAdapter.clear();
        adapterPlaylistAdapter.clear();
        adapterDownloadedAlbumAdapter.clear();
        adapterPlayQueueSongAdapter.clear();
    }

    private void updateTabIcons() {
        for (int i = 0; i < mTabsAdapter.getCount(); i++) {
            mTabs.getTabAt(i).setIcon(mTabsAdapter.getFragmentIcon(i));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelable(BINDING_PARCEL, Parcels.wrap(this.binding.getUser()));
    }

    /**
     * setting up the layout of the activity.
     * Here you must put elements, remove elements, manage events, ...
     */
    private void setupLayout() {
        String title = mTabsAdapter.getDescription(0);
        this.tabTitle.setText(title);
    }

    /**
     * Setting up the animations for this activty, mainly enter and exit transitions
     */

    private void setupWindowAnimations() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.activity_fade);
//            getWindow().setEnterTransition(transition);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu2, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (mSearchView != null) {
            mSearchView.setQueryHint(getString(R.string.action_search_hint));
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setMaxWidth(Integer.MAX_VALUE);
        }

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
        popup.getMenuInflater().inflate(R.menu.activity_main_menu, popup.getMenu());

        // registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_base_item_removedatabase:
                        emptyDatabaseAction();
                        break;
                    case R.id.menu_base_item_logout:
                        presenter.logout();

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // call this to finish the current activity

                        break;
                    case R.id.menu_base_item_clearplaylist:
                        presenter.clearQueue();
                        break;
                    case R.id.menu_base_item_playrandom:
                        presenter.addRandomSongsToPlaylist();
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

    private void emptyDatabaseAction() {
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
                                                ProgressDialog.show(MainActivity.this, MainActivity.this.getString(R.string.action_emptying_localdatabase_title),
                                                        MainActivity.this.getString(R.string.action_emptying_localdatabase_content), true);

                                        Observable<Void> observable = presenter.emptyDatabase();
                                        observable.subscribeOn(Schedulers.io()).onBackpressureBuffer()
                                                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
                                            @Override
                                            public void call(Void aVoid) {

                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                throwable.printStackTrace();
                                                Timber.e(throwable.getMessage(), throwable);
                                                Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_LONG).show();
                                                pd.dismiss();
                                            }
                                        }, new Action0() {
                                            @Override
                                            public void call() {
                                                pd.dismiss();
                                            }
                                        });


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


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        String title = mTabsAdapter.getDescription(position);
        this.tabTitle.setText(title);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, getString(R.string.action_searchmusic_toast) + " " + query, Toast.LENGTH_LONG).show();

        SearchFragment sf = createSearchFragment(query);
        mTabs.getTabAt(mTabsAdapter.getCount() - 1).select();
        mSearchView.clearFocus();
        sf.updateSearch(query);

        mSearchView.setQuery("", false);
        mSearchView.setIconified(true);
        return true;
    }

    private SearchFragment createSearchFragment(String text) {
        SearchFragment sf = mTabsAdapter.addSearchTab(text);
        updateTabIcons();
        return sf;
    }


    public void closeSearchTab() {
        int tabIndex = mTabsAdapter.getSearchTabIndex();
        if (tabIndex >= 0) {
            mTabs.removeTabAt(tabIndex);
            mTabsAdapter.removeSearch(tabIndex);
            updateTabIcons();
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

}
