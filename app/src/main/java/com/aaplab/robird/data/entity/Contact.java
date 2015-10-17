package com.aaplab.robird.data.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.aaplab.robird.data.provider.contract.ContactContract;

import auto.parcel.AutoParcel;

/**
 * Created by majid on 17.10.15.
 */

@AutoParcel
public abstract class Contact implements Parcelable {
    public abstract int id();

    public abstract long userId();

    public abstract String username();

    public abstract String avatar();

    public static Contact from(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(ContactContract._ID));
        final long userId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.USER_ID));
        final String username = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.USERNAME));
        final String avatar = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.AVATAR));

        return new AutoParcel_Contact(id, userId, username, avatar);
    }

    public static Contact from(twitter4j.User user) {
        return new AutoParcel_Contact(0, user.getId(), user.getScreenName(), user.getOriginalProfileImageURL());
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(ContactContract.AVATAR, avatar());
        values.put(ContactContract.USER_ID, userId());
        values.put(ContactContract.USERNAME, username());

        return values;
    }
}
