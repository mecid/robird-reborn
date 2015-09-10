package com.aaplab.robird;

import android.app.Activity;
import android.content.Context;

import com.yandex.metrica.YandexMetrica;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by majid on 23.08.15.
 */
public final class Analytics {
    public static final String SHARE_VIA_ROBIRD = "Share via Robird";
    public static final String ATTACH_IMAGE = "Attach Image";
    public static final String ADD_ACCOUNT = "Add account";

    public static final String FOLLOW = "Follow";
    public static final String BLOCK = "Block";
    public static final String SPAM = "Spam";
    public static final String ADD_TO_LIST = "Add to list";

    public static final String SEND = "Send";
    public static final String RETWEET = "Retweet";
    public static final String FAVORITE = "Favorite";
    public static final String SHARE = "Share";
    public static final String SEARCH = "Search";
    public static final String DELETE = "Delete";
    public static final String COPY = "Copy";

    public static final String PURCHASE = "Purchase";
    public static final String PRODUCT = "Product";

    private static final String YANDEX_METRICA_KEY = "af3babcc-d5a6-451e-b955-8ca9ba933440";

    public static void setup(Context context) {
        YandexMetrica.activate(context.getApplicationContext(), YANDEX_METRICA_KEY);
    }

    public static void event(String name) {
        YandexMetrica.reportEvent(name);
    }

    public static void purchase(String id) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(PRODUCT, id);

        YandexMetrica.reportEvent(PURCHASE, map);
    }

    public static void onResume(Activity activity) {
        YandexMetrica.onResumeActivity(activity);
    }

    public static void onPause(Activity activity) {
        YandexMetrica.onPauseActivity(activity);
    }

    public static final class YandexMeticaTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (t != null)
                YandexMetrica.reportError(message, t);
        }
    }
}
