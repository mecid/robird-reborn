package com.aaplab.robird;

import android.app.Application;

import com.aaplab.robird.inject.DefaultDependencyFactory;
import com.aaplab.robird.inject.Inject;

import jonathanfinerty.once.Once;
import timber.log.Timber;

public final class RobirdApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Analytics.setup(this);
        Once.initialise(this);
        Inject.using(new DefaultDependencyFactory(this));

        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new Analytics.YandexMeticaTree());
    }
}