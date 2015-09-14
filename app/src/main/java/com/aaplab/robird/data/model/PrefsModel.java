package com.aaplab.robird.data.model;

import android.content.SharedPreferences;

import com.aaplab.robird.inject.Inject;

/**
 * Created by majid on 01.09.15.
 */
public final class PrefsModel {
    public static final String PREFER_DARK_THEME = "prefer_dark_theme";
    public static final String TIMELINE_FONT_SIZE = "font_size";
    public static final String HIDE_AVATARS = "hide_avatars";
    public static final String HIDE_MEDIA = "hide_media";
    public static final String USE_IN_APP_BROWSER = "use_in_app_browser";
    public static final String USE_MOBILE_VIEW_BROWSER = "in_app_browser_mobile";
    public static final String HIGHLIGHT_TIMELINE_LINKS = "highlight_timeline_links";

    private final SharedPreferences mPreferences = Inject.preferences();

    public boolean isDarkTheme() {
        return mPreferences.getBoolean(PREFER_DARK_THEME, false);
    }

    public int fontSize() {
        return Integer.parseInt(mPreferences.getString(TIMELINE_FONT_SIZE, "14"));
    }

    public boolean hideAvatarOnMobileConnection() {
        return mPreferences.getBoolean(HIDE_AVATARS, false);
    }

    public boolean hideMediaOnMobileConnection() {
        return mPreferences.getBoolean(HIDE_MEDIA, false);
    }

    public boolean isInAppBrowserEnabled() {
        return mPreferences.getBoolean(USE_IN_APP_BROWSER, false);
    }

    public boolean isInAppBrowserUseMobileView() {
        return mPreferences.getBoolean(USE_MOBILE_VIEW_BROWSER, false);
    }

    public boolean highlightTimelineLinks() {
        return mPreferences.getBoolean(HIGHLIGHT_TIMELINE_LINKS, false);
    }
}
