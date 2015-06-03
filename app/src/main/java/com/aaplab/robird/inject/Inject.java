package com.aaplab.robird.inject;

import android.content.ContentResolver;
import android.content.SharedPreferences;

import com.squareup.otto.Bus;

/**
 * Created by majid on 13.05.15.
 */
public class Inject {
    private static Inject INSTANCE;

    private final ContentResolver contentResolver;
    private final SharedPreferences preferences;
    private final Bus eventBus;

    public static void using(DependencyFactory dependencyFactory) {
        INSTANCE = new Inject(
                dependencyFactory.contentResolver(),
                dependencyFactory.preferences(),
                dependencyFactory.eventBus()
        );
    }

    private static Inject instance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Inject instance not initiated.");
        }

        return INSTANCE;
    }

    private Inject(ContentResolver contentResolver, SharedPreferences preferences, Bus eventBus) {
        this.contentResolver = contentResolver;
        this.preferences = preferences;
        this.eventBus = eventBus;
    }

    public static ContentResolver contentResolver() {
        return instance().contentResolver;
    }

    public static SharedPreferences preferences() {
        return instance().preferences;
    }

    public static Bus eventBus() {
        return instance().eventBus;
    }
}
