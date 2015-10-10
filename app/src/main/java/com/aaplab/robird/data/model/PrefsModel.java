package com.aaplab.robird.data.model;

import android.app.AlarmManager;
import android.content.SharedPreferences;

import com.aaplab.robird.inject.Inject;

/**
 * Created by majid on 01.09.15.
 */
public final class PrefsModel {
    public static final String PREFER_DARK_THEME = "prefer_dark_theme";
    public static final String SHOW_CLIENT_NAME_IN_TIMELINE = "client_name";
    public static final String COMPACT_TIMELINE = "compact_timeline";
    public static final String SHOW_ABSOLUTE_TIME = "absolute_time";
    public static final String TIMELINE_FONT_SIZE = "font_size";
    public static final String HIDE_AVATARS = "hide_avatars";
    public static final String HIDE_MEDIA = "hide_media";
    public static final String USE_IN_APP_BROWSER = "use_in_app_browser";
    public static final String USE_MOBILE_VIEW_BROWSER = "in_app_browser_mobile";
    public static final String HIGHLIGHT_TIMELINE_LINKS = "highlight_timeline_links";
    public static final String BACKGROUND_UPDATE_SERVICE = "background_update_service";
    public static final String BACKGROUND_UPDATE_INTERVAL = "background_updates_interval";
    public static final String NOTIFICATIONS = "notifications";
    public static final String TWEETMARKER = "tweetmarker";

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

    public boolean compactTimeline() {
        return mPreferences.getBoolean(COMPACT_TIMELINE, false);
    }

    public boolean highlightTimelineLinks() {
        return mPreferences.getBoolean(HIGHLIGHT_TIMELINE_LINKS, false);
    }

    public boolean showAbsoluteTime() {
        return mPreferences.getBoolean(SHOW_ABSOLUTE_TIME, false);
    }

    public boolean showClientNameInTimeline() {
        return mPreferences.getBoolean(SHOW_CLIENT_NAME_IN_TIMELINE, false);
    }

    public boolean isBackgroundUpdateServiceEnabled() {
        return mPreferences.getBoolean(BACKGROUND_UPDATE_SERVICE, true);
    }

    public long backgroundUpdateInterval() {
        return Long.valueOf(mPreferences.getString(BACKGROUND_UPDATE_INTERVAL,
                String.valueOf(AlarmManager.INTERVAL_HALF_HOUR)));
    }

    public boolean isNotificationsEnabled() {
        return mPreferences.getBoolean(NOTIFICATIONS, true);
    }

    public boolean isTweetMarkerEnabled() {
        return mPreferences.getBoolean(TWEETMARKER, false);
    }
}
