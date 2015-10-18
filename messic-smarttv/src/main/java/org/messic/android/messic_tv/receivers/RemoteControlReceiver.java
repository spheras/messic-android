package org.messic.android.messic_tv.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import org.messic.android.messic_tv.util.UtilMessic;
import org.messic.android.messiccore.util.UtilMusicPlayer;

/**
 * Created by Fco Javier Coira on 12/10/2015.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                UtilMusicPlayer.playSong(context);
            } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                UtilMusicPlayer.pauseSong(context);
            } else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode() && event.getAction() == KeyEvent.ACTION_UP) {
                if (UtilMusicPlayer.isPlaying(context)) {
                    UtilMusicPlayer.pauseSong(context);
                } else {
                    UtilMusicPlayer.resumeSong(context);
                }
            }
        }
    }
}
