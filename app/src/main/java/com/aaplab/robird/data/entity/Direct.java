package com.aaplab.robird.data.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;

import com.aaplab.robird.data.provider.contract.DirectContract;

import auto.parcel.AutoParcel;
import twitter4j.DirectMessage;
import twitter4j.User;

/**
 * Created by majid on 18.08.15.
 */

@AutoParcel
public abstract class Direct implements Parcelable {
    public abstract int id();

    public abstract long directId();

    public abstract String avatar();

    public abstract String userName();

    public abstract String fullName();

    public abstract String text();

    public abstract long createdAt();

    public abstract String recipient();

    public abstract String recipientFullName();

    public abstract String recipientAvatar();

    public static Direct from(DirectMessage directMessage) {
        final User sender = directMessage.getSender();
        final User recipient = directMessage.getRecipient();

        return new AutoParcel_Direct(
                0, directMessage.getId(),
                sender.getOriginalProfileImageURL(),
                sender.getScreenName(), sender.getName(), directMessage.getText(),
                directMessage.getCreatedAt().getTime(),
                recipient.getScreenName(), recipient.getName(),
                recipient.getOriginalProfileImageURL()
        );
    }

    public static Direct from(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DirectContract._ID));
        final long directId = cursor.getLong(cursor.getColumnIndexOrThrow(DirectContract.DIRECT_ID));
        final String avatar = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.AVATAR));
        final String userName = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.USERNAME));
        final String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.FULLNAME));
        final long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DirectContract.CREATED_AT));
        final String recipient = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.RECIPIENT));
        final String recipientAvatar = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.RECIPIENT_AVATAR));
        final String recipientFullName = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.RECIPIENT_FULLNAME));
        final String text = cursor.getString(cursor.getColumnIndexOrThrow(DirectContract.TEXT));

        return new AutoParcel_Direct(id, directId, avatar, userName, fullName, text, createdAt, recipient, recipientFullName, recipientAvatar);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(DirectContract.DIRECT_ID, directId());
        values.put(DirectContract.USERNAME, userName());
        values.put(DirectContract.FULLNAME, fullName());
        values.put(DirectContract.AVATAR, avatar());
        values.put(DirectContract.TEXT, text());
        values.put(DirectContract.CREATED_AT, createdAt());
        values.put(DirectContract.RECIPIENT, recipient());
        values.put(DirectContract.RECIPIENT_AVATAR, recipientAvatar());
        values.put(DirectContract.RECIPIENT_FULLNAME, recipientFullName());

        return values;
    }
}
