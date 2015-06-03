package com.aaplab.robird.data.entity;

import android.database.Cursor;

import com.aaplab.robird.data.provider.contract.AccountContract;

import java.io.Serializable;

/**
 * Created by majid on 16.01.15.
 */
public class Account implements Serializable {
    public int id;
    public String token;
    public String tokenSecret;
    public String screenName;
    public String fullName;
    public String avatar;
    public String userBackground;
    public long userId;
    public int active;

    public static Account from(Cursor cursor) {
        Account account = new Account();

        account.id = cursor.getInt(cursor.getColumnIndexOrThrow(AccountContract._ID));
        account.userId = cursor.getLong(cursor.getColumnIndexOrThrow(AccountContract.USER_ID));
        account.token = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.TOKEN));
        account.tokenSecret = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.TOKEN_SECRET));
        account.screenName = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.SCREEN_NAME));
        account.fullName = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.FULL_NAME));
        account.avatar = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.AVATAR));
        account.userBackground = cursor.getString(cursor.getColumnIndexOrThrow(AccountContract.USER_BACKGROUND));
        account.active = cursor.getInt(cursor.getColumnIndexOrThrow(AccountContract.ACTIVE));

        return account;
    }
}
