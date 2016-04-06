package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v17.leanback.app.HeadersFragment;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.activities.main.fragments.MainFragmentPresenter;

import java.util.ArrayList;
import java.util.List;

public class IndexCardViewItem implements CardViewItem {

    private static final int INDEX_CARD_WIDTH = 60;
    private static final int INDEX_CARD_HEIGHT = 60;
    private char index;
    private Paint paint;
    private HeadersFragment header;
    private int startHeaderIndex;

    public IndexCardViewItem(char index, HeadersFragment header, int startHeaderIndex) {
        this.index = index;
        this.paint = new Paint();
        this.paint.setColor(Color.WHITE);
        this.paint.setTextSize(20);
        this.header = header;
        this.startHeaderIndex = startHeaderIndex;
    }

    public static List<IndexCardViewItem> getIndexList(HeadersFragment fragment, int startHeaderIndex) {

        char[] chars = new char[]{'0', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        ArrayList<IndexCardViewItem> result = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            IndexCardViewItem cardview = new IndexCardViewItem(chars[i], fragment, startHeaderIndex);
            result.add(cardview);
        }
        return result;
    }

    @Override
    public Object getItem() {
        return index;
    }

    @Override
    public void onBindViewHolder(Configuration config, Context context, CardViewHolder viewHolder) {
        viewHolder.getImageCardView().setTitleText("" + getIndex());
        viewHolder.getImageCardView().setMainImageDimensions(INDEX_CARD_WIDTH, 0);
        viewHolder.getImageCardView().setContentText("" + getIndex());
        /*
        int width_pixels = Utils.convertDpToPixel(context, INDEX_CARD_WIDTH);
        int height_pixels = Utils.convertDpToPixel(context, INDEX_CARD_HEIGHT);
        Bitmap bm = Bitmap.createBitmap(width_pixels, height_pixels, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        paint.setColor(Color.RED);
        c.drawRect(0, 0, 40, 40, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        c.drawText("C" + getIndex(), 0, 20, paint);
        Drawable bitmapDrawable = new BitmapDrawable(context.getResources(), bm);
        viewHolder.mCardView.setMainImage(bitmapDrawable);
        */
        viewHolder.getImageCardView().setMainImage(null);
        viewHolder.getImageCardView().setBadgeImage(null);
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object oitem, RowPresenter.ViewHolder rowViewHolder, Row row, UtilMusicPlayer ump, Configuration config, MainFragmentPresenter presenter, Context context) {
        ObjectAdapter adapter = header.getAdapter();
        for (int i = startHeaderIndex; i < adapter.size(); i++) {
            String sheader = ((ListRow) adapter.get(i)).getHeaderItem().getName();
            if (sheader.startsWith("" + getIndex())) {
                header.setSelectedPosition(i);
                return;
            }
        }
    }

    public char getIndex() {
        return this.index;
    }
}
