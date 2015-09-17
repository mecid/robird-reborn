package com.aaplab.robird;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aaplab.robird.data.model.PrefsModel;

/**
 * Created by majid on 02.09.15.
 */
public final class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PrefsModel prefsModel = new PrefsModel();
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                new Intent(context, UpdateService.class), 0);

        if (intent.hasExtra("cancel")) {
            am.cancel(pendingIntent);
        } else if (prefsModel.isBackgroundUpdateServiceEnabled()) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, prefsModel.backgroundUpdateInterval(),
                    prefsModel.backgroundUpdateInterval(), pendingIntent);
        }
    }
}
