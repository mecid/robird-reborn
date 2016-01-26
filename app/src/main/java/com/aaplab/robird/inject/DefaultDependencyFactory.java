package com.aaplab.robird.inject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by majid on 13.05.15.
 */
public class DefaultDependencyFactory implements DependencyFactory {

    private Context mContext;

    public DefaultDependencyFactory(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public ContentResolver contentResolver() {
        return mContext.getContentResolver();
    }

    @Override
    public SharedPreferences preferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }
}
