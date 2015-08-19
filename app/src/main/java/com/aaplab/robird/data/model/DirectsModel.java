package com.aaplab.robird.data.model;

import android.content.ContentValues;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Direct;
import com.aaplab.robird.data.provider.contract.DirectContract;
import com.aaplab.robird.data.provider.contract.UserListContract;
import com.aaplab.robird.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import twitter4j.DirectMessage;
import twitter4j.TwitterException;

/**
 * Created by majid on 18.08.15.
 */
public class DirectsModel extends BaseTwitterModel {

    private final SqlBriteContentProvider mSqlBriteContentProvider =
            SqlBriteContentProvider.create(Inject.contentResolver());

    public DirectsModel(Account account) {
        super(account);
    }

    public Observable<Integer> update() {
        return Observable.create(new Observable.OnSubscribe<List<DirectMessage>>() {
            @Override
            public void call(Subscriber<? super List<DirectMessage>> subscriber) {
                try {
                    ArrayList<DirectMessage> directs = new ArrayList<>();

                    directs.addAll(mTwitter.getDirectMessages());
                    directs.addAll(mTwitter.getSentDirectMessages());

                    subscriber.onNext(directs);
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        })
                .doOnNext(new DirectsPersister(mAccount))
                .map(new Func1<List<DirectMessage>, Integer>() {
                    @Override
                    public Integer call(List<DirectMessage> directMessages) {
                        return directMessages.size();
                    }
                });
    }

    public Observable<List<Direct>> directs() {
        return mSqlBriteContentProvider.query(
                DirectContract.CONTENT_URI, DirectContract.PROJECTION,
                String.format("%s=%d", DirectContract.ACCOUNT_ID, mAccount.id()),
                null, DirectContract.DIRECT_ID + " DESC", false
        ).map(MapFunctions.DIRECTS);
    }

    public Observable<List<Direct>> directs(final String username) {
        return mSqlBriteContentProvider.query(
                DirectContract.CONTENT_URI, DirectContract.PROJECTION,
                String.format("%s=%d AND (%s='%s' OR %s='%s')",
                        DirectContract.ACCOUNT_ID, mAccount.id(),
                        DirectContract.RECIPIENT, username,
                        DirectContract.USERNAME, username
                ), null, DirectContract.DIRECT_ID, false
        ).map(MapFunctions.DIRECTS);
    }

    public Observable<DirectMessage> send(final String username, final String text) {
        return Observable.create(new Observable.OnSubscribe<DirectMessage>() {
            @Override
            public void call(Subscriber<? super DirectMessage> subscriber) {
                try {
                    subscriber.onNext(mTwitter.sendDirectMessage(username, text));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static final class DirectsPersister implements Action1<List<DirectMessage>> {
        private final Account account;

        public DirectsPersister(Account account) {
            this.account = account;
        }

        @Override
        public void call(List<twitter4j.DirectMessage> directMessages) {
            ArrayList<Direct> directs = new ArrayList<>(directMessages.size());

            for (DirectMessage directMessage : directMessages)
                directs.add(Direct.from(directMessage));

            ArrayList<ContentValues> contentValues = new ArrayList<>();

            for (Direct direct : directs) {
                ContentValues values = direct.toContentValues();
                values.put(UserListContract.ACCOUNT_ID, account.id());
                contentValues.add(values);
            }

            ContentValues[] values = new ContentValues[directs.size()];
            Inject.contentResolver().delete(DirectContract.CONTENT_URI, null, null);
            Inject.contentResolver().bulkInsert(DirectContract.CONTENT_URI, contentValues.toArray(values));
        }
    }
}
