package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;

public interface CardViewItem {
    int DEFAULT_CARD_WIDTH = 176;
    int DEFAULT_CARD_HEIGHT = 176;

    Object getItem();

    void onBindViewHolder(Configuration config, Context context, CardViewHolder viewHolder);

    void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row, UtilMusicPlayer ump, Configuration config, MainFragmentPresenter presenter, Context context);
}
