package org.messic.android.activities.main.fragments.explore;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.messic.android.R;
import org.messic.android.messiccore.datamodel.MDMAuthor;

public class ExploreAuthorViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public MDMAuthor author;
    public Button authorName;
    public ImageButton cover;
    public RelativeLayout rl;
    public LinearLayout albumList;
    private IMyViewHolderClicks mListener;


    public ExploreAuthorViewHolders(View itemView, IMyViewHolderClicks listener) {
        super(itemView);
        mListener = listener;
        authorName = (Button) itemView.findViewById(R.id.fragment_explore_item_tvauthor);
        authorName.setOnClickListener(this);
        cover = (ImageButton) itemView.findViewById(R.id.fragment_explore_item_cover);
        cover.setOnClickListener(this);
        rl = (RelativeLayout) itemView.findViewById(R.id.fragment_explore_item_layout);
        albumList = (LinearLayout) itemView.findViewById(R.id.fragment_explore_item_list);
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