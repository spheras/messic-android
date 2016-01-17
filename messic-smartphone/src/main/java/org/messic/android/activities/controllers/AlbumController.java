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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.messic.android.R;
import org.messic.android.activities.AlbumInfoActivity;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.dao.DAOAlbum;
import org.messic.android.messiccore.util.UtilRestJSONClient;

public class AlbumController
{
    public static void getAlbumInfoOffline( final Context originActivity, MDMAlbum album )
    {
        if ( album.getSongs() == null || album.getSongs().size() == 0 )
        {
            DAOAlbum daoalbum = new DAOAlbum( originActivity );
            album = daoalbum.getByAlbumLSid( album.getLsid(), true );
        }

        if ( originActivity instanceof AlbumInfoActivity )
        {
            AlbumInfoActivity aia = (AlbumInfoActivity) originActivity;
            aia.eventAlbumInfoLoaded( album );
        }
        else
        {
            Intent ssa = new Intent( originActivity, AlbumInfoActivity.class );
            ssa.putExtra( AlbumInfoActivity.EXTRA_ALBUM_SID, album );
            originActivity.startActivity( ssa );
        }
    }

    public static void getAlbumInfoOnline( final Activity originActivity, long sid )
    {
        final ProgressDialog dialog =
            ProgressDialog.show( originActivity, originActivity.getResources().getString( R.string.albuminfo_loading ),
                                 originActivity.getResources().getString( R.string.albuminfo_wait ), true );
        dialog.show();

        final String baseURL =
            Configuration.getBaseUrl(originActivity) + "/services/albums/" + sid
                + "?songsInfo=true&authorInfo=true&messic_token=" + Configuration.getLastToken(originActivity);
        UtilRestJSONClient.get( originActivity, baseURL, MDMAlbum.class,
                                new UtilRestJSONClient.RestListener<MDMAlbum>()
                                {
                                    public void response( MDMAlbum response )
                                    {
                                        if ( originActivity instanceof AlbumInfoActivity )
                                        {
                                            AlbumInfoActivity aia = (AlbumInfoActivity) originActivity;
                                            aia.eventAlbumInfoLoaded( response );
                                        }
                                        else
                                        {
                                            Intent ssa = new Intent( originActivity, AlbumInfoActivity.class );
                                            ssa.putExtra( AlbumInfoActivity.EXTRA_ALBUM_SID, response );
                                            originActivity.startActivity( ssa );
                                        }
                                        dialog.dismiss();
                                    }

                                    public void fail( final Exception e )
                                    {
                                        dialog.dismiss();

                                        Log.e( "Random", e.getMessage(), e );
                                        originActivity.runOnUiThread( new Runnable()
                                        {

                                            public void run()
                                            {
                                                Toast.makeText( originActivity, "Server Error", Toast.LENGTH_SHORT ).show();

                                            }
                                        } );
                                    }

                                } );

    }

}
