package com.aaplab.robird.data.model;

import android.content.ContentValues;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.data.provider.contract.UserListContract;
import com.aaplab.robird.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import twitter4j.TwitterException;

/**
 * Created by majid on 17.08.15.
 */
public class UserListsModel extends BaseTwitterModel {

    private final SqlBriteContentProvider mSqlBriteContentProvider =
            SqlBriteContentProvider.create(Inject.contentResolver());

    public UserListsModel(Account account) {
        super(account);
    }

    public Observable<List<UserList>> lists() {
        return mSqlBriteContentProvider.query(
                UserListContract.CONTENT_URI, UserListContract.PROJECTION,
                String.format("%s=%d", UserListContract.ACCOUNT_ID, mAccount.id()),
                null, null, false
        ).map(MapFunctions.USER_LISTS);
    }

    public Observable<Integer> update() {
        return Observable.create(new Observable.OnSubscribe<List<twitter4j.UserList>>() {

            @Override
            public void call(Subscriber<? super List<twitter4j.UserList>> subscriber) {
                try {
                    subscriber.onNext(mTwitter.getUserLists(mAccount.userId()));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        })
                .doOnNext(new UserListsPersister(mAccount))
                .map(new Func1<List<twitter4j.UserList>, Integer>() {
                    @Override
                    public Integer call(List<twitter4j.UserList> userLists) {
                        return userLists.size();
                    }
                });

    }

    public Observable<twitter4j.UserList> addUser(final long listId, final long userId) {
        return Observable.create(new Observable.OnSubscribe<twitter4j.UserList>() {
            @Override
            public void call(Subscriber<? super twitter4j.UserList> subscriber) {
                try {
                    subscriber.onNext(mTwitter.createUserListMember(listId, userId));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static final class UserListsPersister implements Action1<List<twitter4j.UserList>> {
        private final Account account;

        public UserListsPersister(Account account) {
            this.account = account;
        }

        @Override
        public void call(List<twitter4j.UserList> userLists) {
            ArrayList<UserList> lists = new ArrayList<>(userLists.size());

            for (twitter4j.UserList userList : userLists)
                lists.add(UserList.from(userList));

            ArrayList<ContentValues> contentValues = new ArrayList<>();
            for (UserList list : lists) {
                ContentValues values = list.toContentValues();
                values.put(UserListContract.ACCOUNT_ID, account.id());
                contentValues.add(values);
            }

            ContentValues[] values = new ContentValues[lists.size()];
            Inject.contentResolver().delete(UserListContract.CONTENT_URI, null, null);
            Inject.contentResolver().bulkInsert(UserListContract.CONTENT_URI, contentValues.toArray(values));
        }
    }
}
