package org.messic.android.activities.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMAuthor;

public class ExploreAuthorViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public MDMAuthor author;
    public TextView authorName;
    //public ImageView cover;
    public RelativeLayout rl;
    public LinearLayout albumList;
    private IMyViewHolderClicks mListener;


    public ExploreAuthorViewHolders(View itemView, IMyViewHolderClicks listener) {
        super(itemView);
        mListener = listener;
        itemView.setOnClickListener(this);
        authorName = (TextView) itemView.findViewById(R.id.author_albums_tauthor);
        //cover = (ImageView) itemView.findViewById(R.id.author_albums_ivcover);
        rl = (RelativeLayout) itemView.findViewById(R.id.author_albums_rl);
        albumList = (LinearLayout) itemView.findViewById(R.id.author_albums_list);
    }

    @Override
    public void onClick(View view) {
        //Toast.makeText(view.getContext(), "Clicked Position = " + getPosition(), Toast.LENGTH_SHORT).show();
        mListener.onCardTouch(view, this);
    }

    public static interface IMyViewHolderClicks {
        public void onCardTouch(View caller, ExploreAuthorViewHolders clicked);
    }
}