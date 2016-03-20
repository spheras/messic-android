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
package org.messic.android.messiccore.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

import javax.inject.Inject;

public class AlbumCoverCache {
    public static String COVER_OFFLINE_FILENAME = "cover.jpg";
    private static AlbumCoverCache instance;
    @Inject
    Configuration config;
    private LruCache<String, Bitmap> mMemoryCache;

    private AlbumCoverCache() {
        MessicCoreApp.getInstance().getComponent().inject(this);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static AlbumCoverCache get() {
        if (instance == null) {
            instance = new AlbumCoverCache();
        }
        return instance;
    }

    public Bitmap getCover(final MDMAlbum album, final CoverListener listener) {
        return getCover(album, listener, 100, 100);
    }

    public Bitmap getCover(final MDMAlbum album, final CoverListener listener, final int prefferredWidth, final int prefferredHeight) {
        Bitmap result = getBitmapFromMemCache("" + album.getSid());
        if (result != null) {
            return result;
        } else {
            if (config.isOffline()) {
                String albumPath = album.getLfileName();
                File coverPath = new File(albumPath + "/" + COVER_OFFLINE_FILENAME);
                if (coverPath.exists()) {
                    Bitmap bmp;
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(coverPath);
                        bmp = BitmapFactory.decodeStream(fis);
                        addBitmapToMemoryCache("" + album.getSid(), bmp);
                        return bmp;
                    } catch (FileNotFoundException e) {
                        Log.e("AlbumCoverCache", e.getMessage(), e);
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            } else {
                AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            String baseURL =
                                    config.getBaseUrl() + "/services/albums/" + album.getSid()
                                            + "/cover?" +
                                            (prefferredWidth > 0 ? "preferredWidth=" + prefferredWidth + "&preferredHeight=" + prefferredHeight + "&" : "")
                                            + "messic_token=" + config.getLastToken();
                            System.gc();
                            Bitmap bmp =
                                    BitmapFactory.decodeStream(new URL(baseURL).openConnection().getInputStream());
                            addBitmapToMemoryCache("" + album.getSid(), bmp);
                            listener.setCover(bmp);
                        } catch (Exception e) {
                            listener.failed(e);
                        }
                        return null;
                    }

                };
                at.execute();
            }
            return null;
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public interface CoverListener {
        void setCover(Bitmap bitmap);

        void failed(Exception e);
    }
}
