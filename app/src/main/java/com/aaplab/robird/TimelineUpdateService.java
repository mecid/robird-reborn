package com.aaplab.robird;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.DirectsModel;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.data.model.UserListsModel;
import com.aaplab.robird.ui.activity.HomeActivity;
import com.aaplab.robird.util.DefaultObserver;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by majid on 02.09.15.
 */
public final class TimelineUpdateService extends GcmTaskService {

    public static PeriodicTask create(long periodInSeconds) {
        return new PeriodicTask.Builder()
                .setService(TimelineUpdateService.class)
                .setPeriod(periodInSeconds)
                .setTag("timeline_update")
                .setUpdateCurrent(true)
                .setPersisted(true)
                .build();
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        try {
            final PrefsModel prefsModel = new PrefsModel();

            for (final Account account : new AccountModel().accounts().toBlocking().first()) {
                new TimelineModel(account, TimelineModel.HOME_ID).update().toBlocking().first();
                new TimelineModel(account, TimelineModel.RETWEETS_ID).update().toBlocking().first();
                new TimelineModel(account, TimelineModel.FAVORITES_ID).update().toBlocking().first();
                new UserListsModel(account).update().toBlocking().first();
                new DirectsModel(account).update().toBlocking().first();

                List<UserList> userLists = new UserListsModel(account).lists().toBlocking().first();
                for (UserList userList : userLists)
                    new TimelineModel(account, userList.listId()).update().toBlocking().first();

                new TimelineModel(account, TimelineModel.MENTIONS_ID)
                        .update()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<Integer>() {
                            @Override
                            public void onNext(Integer newMentionCount) {
                                super.onNext(newMentionCount);
                                if (prefsModel.isNotificationsEnabled() && newMentionCount > 0) {
                                    notifyMentions(account, newMentionCount);
                                }
                            }
                        });
            }

            return GcmNetworkManager.RESULT_SUCCESS;
        } catch (Throwable t) {
            Timber.i(t, "");
        }

        return GcmNetworkManager.RESULT_FAILURE;
    }

    private void notifyMentions(final Account account, Integer count) {
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        final Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("account", account);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentText(getString(R.string.new_mentions, count));
        builder.setContentTitle(getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_at);
        builder.setAutoCancel(true);

        if (new PrefsModel().isNotificationSoundEnabled())
            builder.setDefaults(Notification.DEFAULT_SOUND);

        Picasso.with(this).load(account.avatar()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.setLargeIcon(bitmap);
                notificationManager.notify(account.screenName(), 7226, builder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }
}
