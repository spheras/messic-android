package org.messic.android.activities.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.messic.android.R;
import org.messic.android.activities.holders.ExploreAuthorViewHolders;
import org.messic.android.fastscroller.BubbleTextGetter;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMAuthor;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExploreAuthorRecyclerViewAdapter extends RecyclerView.Adapter<ExploreAuthorViewHolders> implements BubbleTextGetter, ExploreAuthorViewHolders.IMyViewHolderClicks {

    private HashMap<Long, MDMAuthor> authorsMap = new HashMap<>();
    private List<MDMAuthor> authorsList = new ArrayList<>();
    private Activity context;
    private AlbumAdapter.EventListener mAlbumListener;

    public ExploreAuthorRecyclerViewAdapter(Activity context, AlbumAdapter.EventListener albumListener) {
        this.context = context;
        this.mAlbumListener = albumListener;
    }

    public ExploreAuthorViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.author, null);
        ExploreAuthorViewHolders rcv = new ExploreAuthorViewHolders(layoutView, this);
        return rcv;
    }

    public void onBindViewHolder(final ExploreAuthorViewHolders holder, int position) {
        if (authorsList != null) {
            MDMAuthor author = authorsList.get(position);
            holder.authorName.setText(author.getName());
            holder.author = author;


            //Do we have albums info?
            if (author.flagFullInfoServer) {
                createAlbumViews(holder);
            } else {
                holder.albumList.removeAllViews();
                final String baseURL =
                        Configuration.getBaseUrl(this.context) + "/services/authors/" + author.getSid() + "/cover?preferredWidth=100&preferredHeight=100&messic_token=" + Configuration.getLastToken(this.context);

                Picasso.with(context).load(baseURL).into(

                        new Target() {

                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                //mainLayout.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                                holder.rl.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                            }

                            @Override
                            public void onBitmapFailed(final Drawable errorDrawable) {
                                Log.d("TAG", "FAILED");
                            }

                            @Override
                            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                Log.d("TAG", "Prepare Load");
                            }
                        });

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
                final View albumView = LayoutInflater.from(context).inflate(R.layout.author_albums, null);
                TextView tvName = (TextView) albumView.findViewById(R.id.author_albums_album_talbum);
                final ImageView ivCover = (ImageView) albumView.findViewById(R.id.author_albums_album_icover);
                final ImageView ivPlay = (ImageView) albumView.findViewById(R.id.author_albums_album_play);
                final ImageView ivMore = (ImageView) albumView.findViewById(R.id.author_albums_album_ivmore);

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
                        mAlbumListener.textTouch(album);
                    }
                });

                ivMore.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mAlbumListener.moreTouch(album, ivMore, 0);
                    }
                });

                final String baseURL =
                        Configuration.getBaseUrl(context) + "/services/albums/" + album.getSid()
                                + "/cover?preferredWidth=100&preferredHeight=100&messic_token="
                                + Configuration.getLastToken(context);
                tvName.setText(album.getName());

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.rl.setBackgroundColor(Color.parseColor("#88006600"));
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
                Configuration.getBaseUrl(this.context) + "/services/authors/" + author.getSid() + "?albumsInfo=true&songsInfo=true&messic_token="
                        + Configuration.getLastToken(this.context);

        UtilRestJSONClient.get(this.context, baseURL, MDMAuthor.class,
                new UtilRestJSONClient.RestListener<MDMAuthor>() {
                    public void response(MDMAuthor response) {
                        replaceAuthor(response);
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


}