package com.aaplab.robird.data.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.provider.contract.AccountContract;

import auto.parcel.AutoParcel;

/**
 * Created by majid on 16.01.15.
 */
@AutoParcel
public abstract class Account implements Parcelable {
    public abstract int id();

    public abstract String token();

    public abstract String tokenSecret();

    @Nullable
    public abstract String screenName();

    @Nullable
    public abstract String fullName();

    @Nullable
    public abstract String avatar();

    @Nullable
    public abstract String userBackground();

    public abstract long userId();

    public abstract int active();

    public Account updateMeta(String fullName, String screenName, String avatar, String userBackground) {
        return new AutoParcel_Account(id(), token(), tokenSecret(), screenName, fullName, avatar,
                userBackground, userId(), active());
    }

    public static Account from(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(AccountContract._ID));
        int active = cursor.getInt(cursor.getColumnIndexOrThrow(AccountContract.ACTIVE));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(AccountContract.USER_ID));
        String token = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.TOKEN));
        String tokenSecret = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.TOKEN_SECRET));
        String screenName = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.SCREEN_NAME));
        String fullName = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.FULL_NAME));
        String avatar = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.AVATAR));
        String userBackground = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.USER_BACKGROUND));

        return new AutoParcel_Account(id, token, tokenSecret, screenName,
                fullName, avatar, userBackground, userId, active);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(AccountContract.TOKEN, token());
        values.put(AccountContract.TOKEN_SECRET, tokenSecret());
        values.put(AccountContract.USER_ID, userId());

        values.put(AccountContract.FULL_NAME, fullName());
        values.put(AccountContract.SCREEN_NAME, screenName());

        values.put(AccountContract.AVATAR, avatar());
        values.put(AccountContract.USER_BACKGROUND, userBackground());

        return values;
    }
}
