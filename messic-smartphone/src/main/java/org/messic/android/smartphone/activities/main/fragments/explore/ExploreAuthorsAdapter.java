package org.messic.android.smartphone.activities.main.fragments.explore;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.util.UtilRestJSONClient;
import org.messic.android.smartphone.views.fastscroller.BubbleTextGetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

public class ExploreAuthorsAdapter extends RecyclerView.Adapter<ExploreAuthorViewHolders> implements BubbleTextGetter, ExploreAuthorViewHolders.IMyViewHolderClicks {

    @Inject
    Configuration config;
    @Inject
    UtilRestJSONClient urjc;

    private HashMap<Long, MDMAuthor> authorsMap = new HashMap<>();
    private List<MDMAuthor> authorsList = new ArrayList<>();
    private Activity context;
    private EventListener mAlbumListener;

    public ExploreAuthorsAdapter() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setListener(EventListener listener) {
        this.mAlbumListener = listener;
    }

    public ExploreAuthorViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_explore_item, null);
        ExploreAuthorViewHolders rcv = new ExploreAuthorViewHolders(layoutView, this);
        return rcv;
    }

    public void onBindViewHolder(final ExploreAuthorViewHolders holder, int position) {
        if (authorsList != null) {
            final MDMAuthor author = authorsList.get(position);
            holder.authorName.setText(author.getName());
            holder.author = author;
            holder.rl.setBackgroundResource(R.color.fragment_explore_card_background);
            holder.authorName.setBackgroundResource(R.drawable.messic_button);
            holder.authorName.setTextColor(Color.BLACK);


            //Do we have albums info?
            if (author.flagFullInfoServer) {
                createAlbumViews(holder);
            } else {
                holder.albumList.removeAllViews();
                final String baseURL =
                        config.getBaseUrl() + "/services/authors/" + author.getSid() + "/cover?preferredWidth=100&preferredHeight=100&messic_token=" + config.getLastToken();

                holder.cover.setVisibility(View.VISIBLE);
                holder.rl.setBackgroundResource(R.color.white);
                Picasso.with(context).load(baseURL).into(holder.cover);
            }

        }
    }

    @Override
    public void onCardTouch(View caller, ExploreAuthorViewHolders clicked) {
        //an element clicked
        if (!clicked.author.flagFullInfoServer) {
            //we must load the info+
            loadAlbums(clicked.author, clicked);
        }
    }

    /**
     * Function to create the album views inside the holder
     *
     * @param holder ExploreAuthorViewHolders
     */
    private void createAlbumViews(final ExploreAuthorViewHolders holder) {

        int position = holder.getAdapterPosition();
        MDMAuthor author = authorsList.get(position);

        if (author.getAlbums() != null) {
            for (int i = 0; i < author.getAlbums().size(); i++) {
                final MDMAlbum album = author.getAlbums().get(i);
                final View albumView = LayoutInflater.from(context).inflate(R.layout.fragment_explore_item_albums, null);
                TextView tvName = (TextView) albumView.findViewById(R.id.fragment_explore_item_album_talbum);
                final ImageView ivCover = (ImageView) albumView.findViewById(R.id.fragment_explore_item_album_icover);
                final ImageView ivPlay = (ImageView) albumView.findViewById(R.id.fragment_explore_item_album_play);
                final ImageView ivMore = (ImageView) albumView.findViewById(R.id.fragment_explore_item_album_ivmore);

                ivPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlbumListener.coverTouch(album);
                    }
                });

                ivPlay.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mAlbumListener.coverLongTouch(album);
                        return true;
                    }
                });
                tvName.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mAlbumListener.textTouch(album, ivCover);
                    }
                });

                ivMore.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mAlbumListener.moreTouch(album, ivMore, 0);
                    }
                });

                final String baseURL =
                        config.getBaseUrl() + "/services/albums/" + album.getSid()
                                + "/cover?preferredWidth=100&preferredHeight=100&messic_token="
                                + config.getLastToken();
                tvName.setText(album.getName());

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.rl.setBackgroundResource(R.color.fragment_explore_card_background_selected);
                        holder.authorName.setBackgroundResource(R.color.fragment_explore_card_selected);
                        holder.authorName.setTextColor(Color.WHITE);
                        holder.cover.setVisibility(View.GONE);
                        Picasso.with(context).load(baseURL).into(ivCover);
                        holder.albumList.addView(albumView);
                    }
                });
            }
        }
    }

    private void loadAlbums(final MDMAuthor author, final ExploreAuthorViewHolders holder) {
        holder.author.flagFullInfoServer = true;

        final String baseURL =
                config.getBaseUrl() + "/services/authors/" + author.getSid() + "?albumsInfo=true&songsInfo=true&messic_token="
                        + config.getLastToken();

        urjc.get(baseURL, MDMAuthor.class,
                new UtilRestJSONClient.RestListener<MDMAuthor>() {
                    public void response(MDMAuthor response) {
                        replaceAuthor(response);

                        for (int i = 0; i < response.getAlbums().size(); i++) {
                            MDMAlbum album = response.getAlbums().get(i);
                            album.setAuthor(response);
                            for (int j = 0; j < album.getSongs().size(); j++) {
                                MDMSong song = album.getSongs().get(j);
                                song.setAlbum(album);
                            }
                        }

                        if (author.getSid() == holder.author.getSid()) {
                            //still viewing
                            createAlbumViews(holder);
                        }
                    }

                    public void fail(final Exception e) {
                        Log.e("Error", e.getMessage(), e);
                        /*
                        activity.runOnUiThread(new Runnable() {

                            public void run() {
                                Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                        */
                    }

                });
    }

    /**
     * Replace the current author info with the new one
     *
     * @param author
     */
    private void replaceAuthor(MDMAuthor author) {
        authorsMap.put(author.getSid(), author);
        for (int i = 0; i < authorsList.size(); i++) {
            MDMAuthor a1 = authorsList.get(i);
            if (a1.getSid() == author.getSid()) {
                authorsList.remove(i);
                authorsList.add(i, author);
                break;
            }
        }
    }

    public int getItemCount() {

        return this.authorsList.size();
    }

    public void clear() {
        authorsMap = new HashMap<Long, MDMAuthor>();
        authorsList = new ArrayList<>();
    }

    public void addAuthor(MDMAuthor author) {
        if (author != null) {
            authorsMap.put(author.getSid(), author);
            authorsList.add(author);
        }
    }

    public void addAuthors(List<MDMAuthor> authors) {
        if (authors != null) {
            for (int i = 0; i < authors.size(); i++) {
                MDMAuthor author = authors.get(i);
                authorsMap.put(author.getSid(), author);
                authorsList.add(author);
            }
        }
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (pos > 0 && pos < authorsList.size()) {
            return authorsList.get(pos).getName().substring(0, 1);
        } else {
            return "";
        }
    }

    public interface EventListener {
        void coverTouch(MDMAlbum album);

        void coverLongTouch(MDMAlbum album);

        void textTouch(MDMAlbum album, ImageView cover);

        void moreTouch(MDMAlbum album, View anchor, int index);
    }


}