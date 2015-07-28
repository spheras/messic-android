/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.android.activities.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.activities.adapters.SongAdapter;
import org.messic.android.activities.fragments.RandomFragment;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMRandomList;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.datamodel.dao.DAOSong;
import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.messiccore.util.UtilRestJSONClient;

import java.util.List;

public class RandomController
{

    private static MDMRandomList[] rlist = null;

    public static void addRandomSongsToPlaylist( final Context ctx )
    {
        if ( rlist != null )
        {
            UtilMusicPlayer.addSongsAndPlay( ctx, rlist[0].getSongs() );
        }
        else
        {
            if ( !Configuration.isOffline() )
            {
                final String baseURL =
                    Configuration.getBaseUrl( ctx )
                        + "/services/randomlists?filterRandomListName=RandomListName-Random&messic_token="
                        + Configuration.getLastToken();
                UtilRestJSONClient.get( ctx, baseURL, MDMRandomList[].class,
                                        new UtilRestJSONClient.RestListener<MDMRandomList[]>()
                                        {
                                            public void response( MDMRandomList[] response )
                                            {
                                                rlist = response;
                                                UtilMusicPlayer.addSongsAndPlay( ctx, rlist[0].getSongs() );
                                            }

                                            public void fail( final Exception e )
                                            {
                                                Log.e( "Random", e.getMessage(), e );
                                                if ( ctx instanceof Activity )
                                                {
                                                    ( (Activity) ctx ).runOnUiThread( new Runnable()
                                                    {

                                                        public void run()
                                                        {
                                                            Toast.makeText( ctx, "Server Error", Toast.LENGTH_SHORT ).show();

                                                        }
                                                    } );
                                                }
                                            }

                                        } );
            }
            else
            {

                AsyncTask<Void, MDMSong, Void> at = new AsyncTask<Void, MDMSong, Void>()
                {
                    private DAOSong.SongPublisher p = new DAOSong.SongPublisher()
                    {

                        public void publish( MDMSong song )
                        {
                            publishProgress( song );
                        }
                    };

                    @Override
                    protected void onProgressUpdate( MDMSong... values )
                    {
                        super.onProgressUpdate( values );
                        for ( MDMSong mdmSong : values )
                        {
                            UtilMusicPlayer.addSong( ctx, mdmSong );
                        }
                    }

                    @Override
                    protected Void doInBackground( Void... params )
                    {
                        DAOSong ds = new DAOSong( ctx );
                        ds.getRandomSongs( 45, p );
                        return null;
                    }

                };
                at.execute();

            }

        }
    }

    public void getRandomMusicOffline( final SongAdapter adapter, final Activity activity, final RandomFragment rf,
                                       boolean refresh, final SwipeRefreshLayout srl )
    {
        if ( rlist == null || refresh )
        {
            rlist = null;

            AsyncTask<Void, MDMSong, Void> at = new AsyncTask<Void, MDMSong, Void>()
            {
                private DAOSong.SongPublisher p = new DAOSong.SongPublisher()
                {

                    public void publish( MDMSong song )
                    {
                        publishProgress( song );
                    }
                };

                @Override
                protected void onProgressUpdate( MDMSong... values )
                {
                    super.onProgressUpdate( values );
                    for ( MDMSong mdmSong : values )
                    {
                        adapter.addSong( mdmSong );
                    }

                    rf.eventRandomInfoLoaded();
                    activity.runOnUiThread( new Runnable()
                    {
                        public void run()
                        {
                            adapter.notifyDataSetChanged();
                        }
                    } );
                }

                @Override
                protected Void doInBackground( Void... params )
                {
                    DAOSong ds = new DAOSong( activity );
                    ds.getRandomSongs( 45, p );
                    return null;
                }

            };
            at.execute();

        }
        else
        {
            if ( rlist != null )
            {
                refreshData( adapter, activity, rf, srl );
            }
        }
    }

    public void getRandomMusicOnline( final SongAdapter adapter, final Activity activity, final RandomFragment rf,
                                      boolean refresh, final SwipeRefreshLayout srl )
    {
        if ( rlist == null || refresh )
        {
            final String baseURL =
                Configuration.getBaseUrl( activity )
                    + "/services/randomlists?filterRandomListName=RandomListName-Random&messic_token="
                    + Configuration.getLastToken();
            UtilRestJSONClient.get( activity, baseURL, MDMRandomList[].class,
                                    new UtilRestJSONClient.RestListener<MDMRandomList[]>()
                                    {
                                        public void response( MDMRandomList[] response )
                                        {
                                            rlist = response;
                                            refreshData( adapter, activity, rf, srl );
                                        }

                                        public void fail( final Exception e )
                                        {
                                            Log.e( "Random", e.getMessage(), e );
                                            activity.runOnUiThread( new Runnable()
                                            {

                                                public void run()
                                                {
                                                    Toast.makeText( activity, "Server Error", Toast.LENGTH_SHORT ).show();

                                                }
                                            } );
                                        }

                                    } );
        }
        else
        {
            if ( rlist != null )
            {
                refreshData( adapter, activity, rf, srl );
            }
        }
    }

    private void refreshData( final SongAdapter adapter, final Activity activity, final RandomFragment rf,
                              final SwipeRefreshLayout srl )
    {
        adapter.clear();
        activity.runOnUiThread( new Runnable()
        {
            public void run()
            {
                adapter.notifyDataSetChanged();
                for ( int i = 0; i < rlist.length; i++ )
                {
                    List<MDMSong> songs = rlist[i].getSongs();
                    for ( int j = 0; j < songs.size(); j++ )
                    {
                        MDMSong song = songs.get( j );
                        adapter.addSong( song );
                    }
                }
                rf.eventRandomInfoLoaded();
                adapter.notifyDataSetChanged();

                if ( srl != null )
                    srl.setRefreshing( false );
            }
        } );

    }
}
