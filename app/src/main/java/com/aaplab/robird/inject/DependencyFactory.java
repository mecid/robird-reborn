package com.aaplab.robird.inject;

import android.content.ContentResolver;
import android.content.SharedPreferences;

import com.squareup.otto.Bus;

/**
 * Created by majid on 13.05.15.
 */
public interface DependencyFactory {
    ContentResolver contentResolver();

    SharedPreferences preferences();

    Bus eventBus();
}
