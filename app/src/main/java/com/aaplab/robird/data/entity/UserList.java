package com.aaplab.robird.data.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.aaplab.robird.data.provider.contract.UserListContract;

import auto.parcel.AutoParcel;

/**
 * Created by majid on 17.08.15.
 */

@AutoParcel
public abstract class UserList implements Parcelable {

    public abstract int id();

    public abstract long listId();

    public abstract String name();

    public static UserList from(twitter4j.UserList userList) {
        return new AutoParcel_UserList(0, userList.getId(), userList.getName());
    }

    public static UserList from(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(UserListContract._ID));
        final long listId = cursor.getLong(cursor.getColumnIndexOrThrow(UserListContract.LIST_ID));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(UserListContract.NAME));


        return new AutoParcel_UserList(id, listId, name);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(UserListContract.LIST_ID, listId());
        values.put(UserListContract.NAME, name());

        return values;
    }
}
