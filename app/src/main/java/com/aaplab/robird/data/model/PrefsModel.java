package com.aaplab.robird.data.model;

import android.content.SharedPreferences;

import com.aaplab.robird.inject.Inject;

/**
 * Created by majid on 01.09.15.
 */
public final class PrefsModel {
    public static final String PREFER_DARK_THEME = "prefer_dark_theme";
    public static final String TIMELINE_FONT_SIZE = "font_size";

    private final SharedPreferences mPreferences = Inject.preferences();

    public boolean isDarkTheme() {
        return mPreferences.getBoolean(PREFER_DARK_THEME, false);
    }

    public int fontSize() {
        return Integer.parseInt(mPreferences.getString(TIMELINE_FONT_SIZE, "14"));
    }
}
