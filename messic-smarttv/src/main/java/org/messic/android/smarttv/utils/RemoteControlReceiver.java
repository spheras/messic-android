package org.messic.android.smarttv.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import org.messic.android.messiccore.util.UtilMusicPlayer;
import org.messic.android.smarttv.MessicSmarttvApp;

import javax.inject.Inject;

/**
 * Created by Fco Javier Coira on 12/10/2015.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Inject
    UtilMusicPlayer ump;

    public RemoteControlReceiver() {
        ((MessicSmarttvApp) MessicSmarttvApp.getInstance()).getSmarttvComponent().inject(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                ump.playSong();
            } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                ump.pauseSong();
            } else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode() && event.getAction() == KeyEvent.ACTION_UP) {
                if (ump.isPlaying()) {
                    ump.pauseSong();
                } else {
                    ump.resumeSong();
                }
            }
        }
    }
}
