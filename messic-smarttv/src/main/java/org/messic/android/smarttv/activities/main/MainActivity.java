package org.messic.android.smarttv.activities.main;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import org.messic.android.R;
import org.messic.android.smarttv.activities.MessicBaseActivity;
import org.messic.android.smarttv.utils.RemoteControlReceiver;

public class MainActivity extends MessicBaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_activity_main);
        AudioManager manager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        manager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));
        startServices();

    }
}
