package com.aaplab.robird.inject;

import android.content.ContentResolver;
import android.content.SharedPreferences;

/**
 * Created by majid on 13.05.15.
 */
public interface DependencyFactory {
    ContentResolver contentResolver();

    SharedPreferences preferences();
}
