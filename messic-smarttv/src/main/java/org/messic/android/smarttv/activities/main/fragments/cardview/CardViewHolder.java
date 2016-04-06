package org.messic.android.smarttv.activities.main.fragments.cardview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;

import org.messic.android.R;

public class CardViewHolder extends Presenter.ViewHolder {
    private ImageCardView imageCardView;
    private Drawable defaultCardImage;
    private Context context;

    public CardViewHolder(View view) {
        super(view);
        imageCardView = (ImageCardView) view;
        this.context = view.getContext();
        defaultCardImage = context.getResources().getDrawable(R.drawable.unknowncover);
    }

    public ImageCardView getImageCardView() {
        return imageCardView;
    }

    public void setImageCardView(ImageCardView imageCardView) {
        this.imageCardView = imageCardView;
    }

    public Drawable getDefaultCardImage() {
        return defaultCardImage;
    }

    public void setDefaultCardImage(Drawable defaultCardImage) {
        this.defaultCardImage = defaultCardImage;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
