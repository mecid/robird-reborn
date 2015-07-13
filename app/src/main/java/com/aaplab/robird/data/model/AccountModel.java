package com.aaplab.robird.data.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.provider.contract.AccountContract;
import com.aaplab.robird.inject.Inject;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import twitter4j.auth.AccessToken;

/**
 * Created by majid on 14.05.15.
 */
public class AccountModel {

    private final SqlBriteContentProvider mSqlBriteContentProvider =
            SqlBriteContentProvider.create(Inject.contentResolver());

    public Observable<List<Account>> accounts() {
        return mSqlBriteContentProvider
                .query(AccountContract.CONTENT_URI,
                        AccountContract.PROJECTION, null, null,
                        AccountContract.ACTIVE + " DESC", false)
                .map(MapFunctions.ACCOUNT_LIST);
    }

    public Observable<Integer> activate(final Account account) {
        final ContentValues disable = new ContentValues();
        disable.put(AccountContract.ACTIVE, 0);

        final ContentValues activate = new ContentValues();
        activate.put(AccountContract.ACTIVE, 1);

        return mSqlBriteContentProvider
                .update(AccountContract.CONTENT_URI, disable, null, null)
                .flatMap(new Func1<Integer, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer integer) {
                        return mSqlBriteContentProvider
                                .update(AccountContract.CONTENT_URI, activate,
                                        String.format("%s=%d", AccountContract._ID, account.id()),
                                        null
                                );
                    }
                });
    }

    public Observable<Account> add(final AccessToken accessToken) {
        ContentValues values = new ContentValues();

        values.put(AccountContract.TOKEN, accessToken.getToken());
        values.put(AccountContract.TOKEN_SECRET, accessToken.getTokenSecret());
        values.put(AccountContract.SCREEN_NAME, accessToken.getScreenName());
        values.put(AccountContract.USER_ID, accessToken.getUserId());

        return mSqlBriteContentProvider
                .insert(AccountContract.CONTENT_URI, values)
                .flatMap(new Func1<Uri, Observable<SqlBriteContentProvider.Query>>() {
                    @Override
                    public Observable<SqlBriteContentProvider.Query> call(Uri uri) {
                        return mSqlBriteContentProvider
                                .query(uri, AccountContract.PROJECTION,
                                        null, null, null, false);
                    }
                })
                .map(MapFunctions.ACCOUNT)
                .take(1);
    }

    public Observable<Account> update(final Account account) {
        return mSqlBriteContentProvider
                .update(AccountContract.CONTENT_URI, account.toContentValues(),
                        String.format("%s=%d", AccountContract._ID, account.id()), null)
                .flatMap(new Func1<Integer, Observable<SqlBriteContentProvider.Query>>() {
                    @Override
                    public Observable<SqlBriteContentProvider.Query> call(Integer integer) {
                        return mSqlBriteContentProvider
                                .query(
                                        ContentUris.withAppendedId(AccountContract.CONTENT_URI, account.id()),
                                        AccountContract.PROJECTION, null, null, null, false
                                );
                    }
                })
                .map(MapFunctions.ACCOUNT)
                .take(1);
    }
}
