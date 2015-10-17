package com.aaplab.robird.data.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.aaplab.robird.data.provider.contract.AccountContract;
import com.aaplab.robird.data.provider.contract.ContactContract;
import com.aaplab.robird.data.provider.contract.DirectContract;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.data.provider.contract.UserListContract;
import com.tjeannin.provigen.ProviGenOpenHelper;
import com.tjeannin.provigen.ProviGenProvider;
import com.tjeannin.provigen.model.Contract;

/**
 * Created by majid on 16.01.15.
 */
public class RobirdContentProvider extends ProviGenProvider {
    public static final String AUTHORITY = "com.aaplab.robird";
    public static final String BASE_URI = "content://" + AUTHORITY + "/";
    public static final String DATABASE = "robird";

    public static final Class[] contracts = new Class[]{
            AccountContract.class, TweetContract.class,
            UserListContract.class, DirectContract.class,
            ContactContract.class
    };

    @Override
    public SQLiteOpenHelper openHelper(Context context) {
        return new ProviGenOpenHelper(context, DATABASE, null, 3, contracts);
    }

    @Override
    public Class[] contractClasses() {
        return contracts;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase database = openHelper(getContext()).getWritableDatabase();
        Contract contract = findMatchingContract(uri);

        try {
            database.beginTransaction();

            for (ContentValues value : values) {
                database.insert(contract.getTable(), null, value);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        getContext().getContentResolver().notifyChange(uri, null);
        database.close();

        return values.length;
    }
}
