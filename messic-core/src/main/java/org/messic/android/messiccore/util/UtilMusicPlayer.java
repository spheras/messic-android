package org.messic.android.messiccore.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.messic.android.messiccore.MessicCoreApp;
import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.datamodel.MDMAlbum;
import org.messic.android.messiccore.datamodel.MDMPlaylist;
import org.messic.android.messiccore.datamodel.MDMSong;
import org.messic.android.messiccore.player.IMessicPlayerNotification;
import org.messic.android.messiccore.player.MessicPlayerService;
import org.messic.android.messiccore.player.MessicPlayerService.MusicBinder;
import org.messic.android.messiccore.player.PlayerEventListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UtilMusicPlayer {

    private static UtilMusicPlayer instance;
    @Inject
    Configuration config;

    private MessicPlayerService musicService = null;

    //how could we inject this? (real implementation is in other project & dagger work in compiling time)
    private IMessicPlayerNotification playernotification;

    private boolean musicBound = false;
    /**
     * listener list to store there the pending listeners to notify to the music service while connecting
     */
    private List<PlayerEventListener> pendingListeners = new ArrayList<PlayerEventListener>();
    // connect to the service
    private ServiceConnection messicPlayerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            // get service
            musicService = binder.getService();

            for (int i = 0; i < pendingListeners.size(); i++) {
                pendingListeners.get(i).connected();

                musicService.getPlayer().addListener(pendingListeners.get(i));

                MDMSong song = musicService.getPlayer().getCurrentSong();
                if (song != null) {
                    if (musicService.getPlayer().isPlaying()) {
                        pendingListeners.get(i).playing(song, false, 0);
                    } else {
                        pendingListeners.get(i).playing(song, false, 0);
                        pendingListeners.get(i).paused(song, 0);
                    }
                }
            }
            pendingListeners.clear();

            // pass list
            musicBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            musicBound = false;
            for (int i = 0; i < pendingListeners.size(); i++) {
                pendingListeners.get(i).disconnected();
            }
        }

    };

    private UtilMusicPlayer() {
        MessicCoreApp.getInstance().getComponent().inject(this);
    }

    public static UtilMusicPlayer get() {
        if (instance == null) {
            instance = new UtilMusicPlayer();
        }
        return instance;
    }

    public void startMessicMusicService(Class<? extends IMessicPlayerNotification> notification) {
        startMessicMusicService(notification, MessicPlayerService.class);
    }

    public void startMessicMusicService(Class notificationClass, Class serviceClass) {
        if (!musicBound && notificationClass != null) {
            IMessicPlayerNotification notification = null;
            try {
                notification = (IMessicPlayerNotification) (notificationClass.getConstructor().newInstance());
            } catch (NoSuchMethodException nsme) {
                nsme.printStackTrace();
                return;
            } catch (InstantiationException ie) {
                ie.printStackTrace();
                return;
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
                return;
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
                return;
            }

            setMessicPlayerNotification(notification);

            // first we start the service (if it is not started yet)
            Context appctx = MessicCoreApp.getInstance();
            Intent messicPlayerIntent = new Intent(appctx, serviceClass);
            appctx.startService(messicPlayerIntent);

            // and we bind the service to interact with it
            appctx.bindService(messicPlayerIntent, messicPlayerConnection, Context.BIND_AUTO_CREATE);


            MessicPreferences mp = MessicPreferences.get();
            mp.setLastMessicNotificationClassUsed(notification.getClass().getName());
            mp.setLastMessicServiceClassUsed(serviceClass.getName());
        }
    }

    public void stopMessicMusicService(Context context) {
        if (musicBound) {
            // we unbind the service to interact with it
            Context appctx = context.getApplicationContext();
            appctx.unbindService(messicPlayerConnection);
        }
    }

    public MessicPlayerService getMessicPlayerService() {
        if (musicBound) {
            return musicService;
        } else {
            MessicPreferences mp = MessicPreferences.get();

            String sNotificationClass = mp.getLastMessicNotificationClassUsed();
            String sServiceClass = mp.getLastMessicServiceClassUsed();
            if (sNotificationClass != null && sServiceClass != null && sNotificationClass.length() > 0 && sServiceClass.length() > 0) {
                try {
                    startMessicMusicService(Class.forName(sNotificationClass), Class.forName(sServiceClass));
                    long time = 0;
                    while (!musicBound && time < 5000) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                        time = time + 300;
                    }
                    if (musicBound) {
                        return musicService;
                    } else {
                        return null;
                    }
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public void clearQueue() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().clearQueue();
        }

    }

    public void clearAndStopAll() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().stop();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public boolean prevSong() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().prevSong();
            return true;
        }
        return false;
    }

    public boolean addAlbum(MDMAlbum album) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().addAlbum(album);
            return true;
        }
        return false;
    }

    public boolean addPlaylist(MDMPlaylist playlist) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().addPlaylist(playlist);
            return true;
        }
        return false;
    }

    public boolean addSongsAndPlay(List<MDMSong> songs) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            if (config.isOffline()) {
                List<MDMSong> fsongs = new ArrayList<MDMSong>();
                for (int i = 0; i < songs.size(); i++) {
                    MDMSong song = songs.get(i);
                    if (song.isDownloaded(config)) {
                        fsongs.add(song);
                    }
                }

                if (fsongs.size() > 0) {
                    mps.getPlayer().addAndPlay(fsongs);
                }

            } else {
                mps.getPlayer().addAndPlay(songs);
            }

            return true;
        }
        return false;
    }

    public boolean addSongAndPlay(MDMSong song) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().addAndPlay(song);
            return true;
        }
        return false;
    }

    public boolean addSong(MDMSong song) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().addSong(song);
            return true;
        }
        return false;
    }

    public boolean nextSong() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().nextSong();
            return true;
        }
        return false;
    }

    public boolean resumeSong() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().resumeSong();
            return true;
        }
        return false;
    }

    public boolean pauseSong() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().pauseSong();
            return true;
        }
        return false;
    }

    public MDMSong getCurrentSong() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            return mps.getPlayer().getCurrentSong();
        }
        return null;

    }

    public boolean isPlaying() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            return mps.getPlayer().isPlaying();
        }
        return false;
    }

    public boolean setSong(int index) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().setSong(index);
            return true;
        }
        return false;
    }

    public boolean removeSong(int index) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().removeSong(index);
            return true;
        }
        return false;
    }

    public boolean playSong() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            mps.getPlayer().playSong();
            return true;
        }
        return false;
    }

    public int getCursor() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            return mps.getPlayer().getCursor();
        }
        return -1;
    }

    public void addListener(PlayerEventListener listener) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            if (mps.getPlayer() != null) {
                mps.getPlayer().addListener(listener);
            }
        } else {
            pendingListeners.add(listener);
        }
    }

    public void removeListener(PlayerEventListener listener) {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            if (mps.getPlayer() != null) {
                mps.getPlayer().removeListener(listener);
            }
        } else {
            pendingListeners.add(listener);
        }
    }

    public List<MDMSong> getQueue() {
        MessicPlayerService mps = getMessicPlayerService();
        if (mps != null && mps.getPlayer() != null) {
            return mps.getPlayer().getQueue();
        }
        return null;

    }

    public IMessicPlayerNotification getMessicPlayerNotification() {
        return playernotification;
    }

    /**
     * @param newMessicPlayerNotification
     */
    public void setMessicPlayerNotification(IMessicPlayerNotification newMessicPlayerNotification) {
        playernotification = newMessicPlayerNotification;
    }
}
