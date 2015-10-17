package com.aaplab.robird.data.model;

import android.content.ContentValues;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Contact;
import com.aaplab.robird.data.provider.contract.ContactContract;
import com.aaplab.robird.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;
import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by majid on 17.10.15.
 */
public class ContactModel extends BaseTwitterModel {

    private final SqlBriteContentProvider mSqlBriteContentProvider
            = SqlBriteContentProvider.create(Inject.contentResolver());

    public ContactModel(Account account) {
        super(account);
    }

    public Observable<List<Contact>> contacts() {
        return mSqlBriteContentProvider.query(
                ContactContract.CONTENT_URI,
                ContactContract.PROJECTION,
                String.format("%s=%d", ContactContract.ACCOUNT_ID, mAccount.id()),
                null, null, false
        )
                .map(MapFunctions.CONTACT_LIST);
    }

    public Observable<Integer> update() {
        return Observable.create(new Observable.OnSubscribe<List<User>>() {
            @Override
            public void call(Subscriber<? super List<User>> subscriber) {
                try {
                    List<User> users = new ArrayList<>();
                    long cursor = -1;

                    do {
                        PagableResponseList<User> list = mTwitter.getFriendsList(mAccount.userId(), cursor);
                        cursor = list.getNextCursor();
                        users.addAll(list);
                    } while (cursor != 0);

                    subscriber.onNext(users);
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        })
                .doOnNext(new ContactsPersister(mAccount))
                .map(new Func1<List<User>, Integer>() {
                    @Override
                    public Integer call(List<User> users) {
                        Timber.d("saved contact size = %d", users.size());
                        return users.size();
                    }
                });
    }

    private static final class ContactsPersister implements Action1<List<User>> {

        private Account account;

        public ContactsPersister(Account account) {
            this.account = account;
        }

        @Override
        public void call(List<User> users) {
            List<ContentValues> contentValues = new ArrayList<>(users.size());

            for (User user : users) {
                ContentValues values = Contact.from(user).toContentValues();
                values.put(ContactContract.ACCOUNT_ID, account.id());
                contentValues.add(values);
            }

            ContentValues[] values = new ContentValues[users.size()];
            Inject.contentResolver().delete(ContactContract.CONTENT_URI,
                    String.format("%s=%d", ContactContract.ACCOUNT_ID, account.id()), null);
            Inject.contentResolver().bulkInsert(ContactContract.CONTENT_URI, contentValues.toArray(values));
        }
    }
}
