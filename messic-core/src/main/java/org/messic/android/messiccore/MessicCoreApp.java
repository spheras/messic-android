package org.messic.android.messiccore;

import android.app.Application;

import org.messic.android.messiccore.dagger2.AndroidCoreModule;
import org.messic.android.messiccore.dagger2.ApplicationCoreComponent;
//important to import (it doesn't matter that it gives you error at the ide)
import org.messic.android.messiccore.dagger2.DaggerApplicationCoreComponent;
import org.messic.android.messiccore.util.UtilMusicPlayer;

import timber.log.Timber;

public abstract class MessicCoreApp extends Application {

    private static MessicCoreApp instance;
    protected ApplicationCoreComponent component;

    public static MessicCoreApp getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;


        initializeTimber();
        this.component = initializeDependencyInjector();
    }

    public ApplicationCoreComponent initializeDependencyInjector() {
        return DaggerApplicationCoreComponent.builder().androidCoreModule(new AndroidCoreModule()).build();
    }


    private void initializeTimber() {
        Timber.plant(new Timber.DebugTree());
    }

    public ApplicationCoreComponent getComponent() {
        return component;
    }

    public void setComponent(ApplicationCoreComponent component) {
        this.component = component;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
