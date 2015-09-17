package com.aaplab.robird;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by majid on 02.09.15.
 */
public final class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                new Intent(context, UpdateService.class), 0);

        if (intent.hasExtra("cancel"))
            am.cancel(pendingIntent);
        else
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }
}
