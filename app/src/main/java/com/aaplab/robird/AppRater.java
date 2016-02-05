package com.aaplab.robird;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

public class AppRater {
    private static final String PREF_LAUNCH_COUNT = "launch_count";
    private static final String PREF_DATE_FIRST_LAUNCH = "date_first_launch";
    private static final String PREF_DONT_SHOW_AGAIN = "dont_show_again";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     */
    public static void showRateDialogIfMeetsConditions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_rater", 0);
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launchCount = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launchCount);

        // Get date of first launch
        Long firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCH, 0);
        if (firstLaunch == 0) {
            firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_DATE_FIRST_LAUNCH, firstLaunch);
        }

        // Wait for at least the number of launches and the number of days used
        // until prompt
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= firstLaunch
                    + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateAlertDialog(context, editor);
            }
        }

        editor.commit();
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     */
    public static void showRateDialog(final Context context) {
        showRateAlertDialog(context, null);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     */
    private static void rateNow(final Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                .parse("market://details?id=" + context.getPackageName())));
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.rate_title));
        builder.setMessage(context.getString(R.string.rate_message));

        builder.setPositiveButton(context.getString(R.string.rate),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        rateNow(context);

                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            editor.commit();
                        }

                        dialog.dismiss();
                    }
                });

        builder.setNeutralButton(context.getString(R.string.later),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            Long date_firstLaunch = System.currentTimeMillis();
                            editor.putLong(PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
                            editor.commit();
                        }
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(context.getString(R.string.no_thanks),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            editor.commit();
                        }
                        dialog.dismiss();
                    }
                });

        builder.show();
    }
}