package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import com.squareup.picasso.Picasso;

import org.messic.android.R;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;

import javax.inject.Inject;

public class ActionEmptyCardViewItem extends ActionCardViewItem {

    @Inject
    UtilMusicPlayer ump;

    public ActionEmptyCardViewItem() {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
    }

    @Override
    public void onBindViewHolder(Configuration config, Context context, CardViewHolder viewHolder) {
        viewHolder.getImageCardView().setTitleText(context.getString(R.string.action_clearQueueTitle));
        viewHolder.getImageCardView().setContentText(context.getString(R.string.action_clearQueueDescription));
        viewHolder.getImageCardView().setMainImageDimensions(DEFAULT_CARD_WIDTH, DEFAULT_CARD_HEIGHT);
        Picasso.with(context).load(R.drawable.ic_delete_white_48dp).into(viewHolder.getImageCardView().getMainImageView());
        viewHolder.getImageCardView().setBadgeImage(context.getResources().getDrawable(R.drawable.ic_delete_white_48dp));
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row, UtilMusicPlayer ump, Configuration config, MainFragmentPresenter presenter, Context context) {
        ump.clearAndStopAll();
    }

}
