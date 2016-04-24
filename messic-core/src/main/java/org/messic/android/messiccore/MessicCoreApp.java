package org.messic.android.messiccore;

import android.app.Application;

import org.messic.android.messiccore.controllers.Configuration;
import org.messic.android.messiccore.dagger2.AndroidCoreModule;
import org.messic.android.messiccore.dagger2.ApplicationCoreComponent;
import org.messic.android.messiccore.dagger2.DaggerApplicationCoreComponent;
import org.messic.android.messiccore.datamodel.dao.DAOServerInstance;

import javax.inject.Inject;

import timber.log.Timber;

//important to import (it doesn't matter that it gives you error at the ide)

public abstract class MessicCoreApp extends Application {

    private static MessicCoreApp instance;
    protected ApplicationCoreComponent component;
    @Inject
    Configuration config;
    @Inject
    DAOServerInstance daoServerInstance;

    public static MessicCoreApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;


        initializeTimber();
        this.component = initializeDependencyInjector();

        // Perform injection so that when this call returns all dependencies will be available for use.
        this.getComponent().inject(this);

        // we inject manually... cyclic dependencies shit
        config.injectManuallyDaoServerInstance(daoServerInstance);
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
