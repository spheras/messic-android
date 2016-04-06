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

package org.messic.android.smarttv.activities.main.fragments;

import android.content.Context;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import org.messic.android.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.main.fragments.cardview.CardViewHolder;
import org.messic.android.smarttv.activities.main.fragments.cardview.CardViewItem;

import javax.inject.Inject;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class RowCardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";
    protected static Context mContext;
    @Inject
    Configuration config;

    public RowCardPresenter() {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        //Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();

        ImageCardView imageCardView = new ImageCardView(mContext);
        imageCardView.setFocusable(true);
        imageCardView.setFocusableInTouchMode(true);
        imageCardView.setBackgroundColor(mContext.getResources().getColor(R.color.fastlane_background));
        return new CardViewHolder(imageCardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        CardViewHolder scviewHolder = ((CardViewHolder) viewHolder);
        CardViewItem cardViewItem = (CardViewItem) item;
        cardViewItem.onBindViewHolder(config, mContext, scviewHolder);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        //Log.d(TAG, "onUnbindViewHolder");
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }


}
