package com.aaplab.robird.inject;

import android.content.ContentResolver;
import android.content.SharedPreferences;

/**
 * Created by majid on 13.05.15.
 */
public class Inject {
    private static Inject INSTANCE;

    private final ContentResolver contentResolver;
    private final SharedPreferences preferences;

    public static void using(DependencyFactory dependencyFactory) {
        INSTANCE = new Inject(
                dependencyFactory.contentResolver(),
                dependencyFactory.preferences()
        );
    }

    private static Inject instance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Inject instance not initiated.");
        }

        return INSTANCE;
    }

    private Inject(ContentResolver contentResolver, SharedPreferences preferences) {
        this.contentResolver = contentResolver;
        this.preferences = preferences;
    }

    public static ContentResolver contentResolver() {
        return instance().contentResolver;
    }

    public static SharedPreferences preferences() {
        return instance().preferences;
    }
}
