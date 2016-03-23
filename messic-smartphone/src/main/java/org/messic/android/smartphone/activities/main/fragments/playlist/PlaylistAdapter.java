package org.messic.android.smartphone.activities.main.fragments.playlist;

import android.app.Activity;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.messic.android.smartphone.MessicSmartphoneApp;
import org.messic.android.R;
import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PlaylistAdapter
        extends BaseExpandableListAdapter {
    @Inject
    Configuration config;
    private List<MDMPlaylist> playlists = new ArrayList<MDMPlaylist>();
    private LayoutInflater inflater = null;
    private Activity activity = null;
    private EventListener listener = null;

    private Animation anim = null;

    public PlaylistAdapter() {
        // Perform injection so that when this call returns all dependencies will be available for use.
        ((MessicSmartphoneApp) MessicCoreApp.getInstance()).getSmartphoneComponent().inject(this);
    }

    public void setActivity(Activity activity) {
        this.inflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.anim = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    public void clear() {
        playlists = new ArrayList<MDMPlaylist>();
    }

    public void addPlaylist(MDMPlaylist playlist) {
        if (this.playlists == null) {
            this.playlists = new ArrayList<MDMPlaylist>();
        }
        this.playlists.add(playlist);
    }

    public Object getChild(int groupPosition, int childPosition) {
        return this.playlists.get(groupPosition).getSongs().get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return this.playlists.get(groupPosition).getSongs().get(childPosition).getSid();
    }

    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.fragment_playlist_item, null);
        }

        final MDMSong song = this.playlists.get(groupPosition).getSongs().get(childPosition);
        final ImageView icover = (ImageView) convertView.findViewById(R.id.fragment_playlist_item_icover);
        TextView tauthor = (TextView) convertView.findViewById(R.id.fragment_playlist_item_tauthor);
        TextView tsongname = (TextView) convertView.findViewById(R.id.fragment_playlist_item_tsong);
        TextView talbum = (TextView) convertView.findViewById(R.id.fragment_playlist_item_talbum);
        ImageButton ibBackground = (ImageButton) convertView.findViewById(R.id.fragment_playlist_item_ib_background);
        final ImageView ficover = icover;
        ImageButton ibPlay = (ImageButton) convertView.findViewById(R.id.fragment_playlist_item_play);

        tauthor.setText(song.getAlbum().getAuthor().getName());
        talbum.setText(song.getAlbum().getName());
        talbum.setPaintFlags(talbum.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tsongname.setText(song.getName());
        icover.setImageResource(android.R.color.white);
        convertView.setTag(childPosition);
        final int fposition = childPosition;
        final View fCounterView = convertView;

        ibBackground.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                listener.textTouch(song, childPosition, icover);
            }
        });


//        tauthor.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                listener.textTouch(song, childPosition, icover);
//            }
//        });
//        talbum.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                listener.textTouch(song, childPosition, icover);
//            }
//        });
//        tsongname.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                listener.textTouch(song, childPosition, icover);
//            }
//        });
/*
        icover.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                v.startAnimation(anim);
                listener.coverTouch(song, childPosition);
            }
        });
        icover.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                v.startAnimation(anim);
                listener.coverLongTouch(song, childPosition);
                return false;
            }
        });
*/
        ibPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                listener.coverTouch(song, childPosition);
            }
        });
        ibPlay.setOnLongClickListener(new View.OnLongClickListener() {

            public boolean onLongClick(View v) {
                v.startAnimation(anim);
                listener.coverLongTouch(song, childPosition);
                return false;
            }
        });


        String baseURL =
                config.getBaseUrl() + "/services/albums/" + song.getAlbum().getSid()
                        + "/cover?preferredWidth=100&preferredHeight=100&messic_token="
                        + config.getLastToken();
        Picasso.with(activity).load(baseURL).into(ficover);

        return convertView;

    }

    public int getChildrenCount(int groupPosition) {
        return this.playlists.get(groupPosition).getSongs().size();
    }

    public Object getGroup(int groupPosition) {
        return this.playlists.get(groupPosition);
    }

    public int getGroupCount() {
        return this.playlists.size();
    }

    public long getGroupId(int groupPosition) {
        return this.playlists.get(groupPosition).getSid();
    }

    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.fragment_playlist_parent, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.fragment_playlist_parent_tname);
        if (isExpanded) {
            textView.setBackgroundResource(R.color.fragment_playlist_selected);
        } else {
            textView.setBackgroundResource(R.color.fragment_playlist);
        }
        textView.setText(this.playlists.get(groupPosition).getName());

        ImageView ivplay = (ImageView) convertView.findViewById(R.id.fragment_playlist_parent_ivplay);
        ivplay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                listener.playlistTouch(playlists.get(groupPosition), groupPosition);
            }
        });
        return convertView;
    }

    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }

    public interface EventListener {
        void coverTouch(MDMSong song, int index);

        void coverLongTouch(MDMSong song, int index);

        void textTouch(MDMSong song, int index, ImageView cover);

        void playlistTouch(MDMPlaylist playlist, int index);
    }

}
