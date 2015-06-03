package com.aaplab.robird.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.aaplab.robird.ui.activity.HomeActivity;
import com.aaplab.robird.ui.activity.SignInActivity;

/**
 * Created by majid on 18.01.15.
 */
public final class NavigationUtils {
    public static void changeDefaultActivityToSignIn(Context context, boolean isSign) {
        ComponentName signActivity = new ComponentName(context, SignInActivity.class);
        ComponentName homeActivity = new ComponentName(context, HomeActivity.class);
        PackageManager packageManager = context.getPackageManager();

        if (isSign) {
            packageManager.setComponentEnabledSetting(signActivity,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(homeActivity,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(signActivity,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(homeActivity,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
