package com.aaplab.robird;

import android.app.Activity;
import android.content.Context;

import com.yandex.metrica.YandexMetrica;

import timber.log.Timber;

/**
 * Created by majid on 23.08.15.
 */
public final class Analytics {

    private static final String YANDEX_METRICA_KEY = "88861";

    public static void setup(Context context) {
        YandexMetrica.initialize(context.getApplicationContext(), YANDEX_METRICA_KEY);
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
