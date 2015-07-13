package com.aaplab.robird.data.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.util.TweetLongerUtils;
import com.aaplab.robird.util.TweetUtils;

import auto.parcel.AutoParcel;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by majid on 19.01.15.
 */
@AutoParcel
public abstract class Tweet implements Parcelable {
    public abstract int id();

    public abstract long tweetId();

    public abstract long inReplyToStatus();

    public abstract long createdAt();

    public abstract String text();

    public abstract boolean favorited();

    public abstract boolean retweetedByMe();

    @Nullable
    public abstract String retweetedBy();

    public abstract String username();

    public abstract String fullname();

    public abstract String avatar();

    @Nullable
    public abstract String media();

    public abstract String source();

    public abstract String mentions();

    public static Tweet from(Status status) {
        long tweetId = status.getId();
        long createdAt = status.getCreatedAt().getTime();
        String source = TweetUtils.getSourceName(status.getSource());

        String retweetedBy = null;
        boolean retweetedByMe = status.isRetweetedByMe();
        if (status.isRetweet()) {
            retweetedBy = status.getUser().getScreenName();
            status = status.getRetweetedStatus();
        }

        String text = status.getText();
        String fullname = status.getUser().getName();
        String username = status.getUser().getScreenName();
        String avatar = status.getUser().getOriginalProfileImageURL();
        long inReplyToStatus = status.getInReplyToStatusId();
        boolean favorited = status.isFavorited();

        StringBuilder sb = new StringBuilder();
        for (UserMentionEntity mention : status.getUserMentionEntities()) {
            sb.append("@").append(mention.getScreenName()).append(" ");
        }
        String mentions = sb.toString();
        sb = new StringBuilder();

        MediaEntity[] mediaEntities = status.getExtendedMediaEntities();
        if (mediaEntities != null && mediaEntities.length > 0) {
            for (MediaEntity mediaEntity : mediaEntities) {
                sb.append(mediaEntity.getMediaURL()).append("+++++");
            }
        }

        URLEntity[] urlEntities = status.getURLEntities();
        if (urlEntities != null && urlEntities.length > 0) {
            for (URLEntity urlEntity : urlEntities) {
                text = text.replace(urlEntity.getURL(), urlEntity.getExpandedURL());
                String mediaUrl = TweetUtils.detectMedia(urlEntity.getExpandedURL());
                if (!TextUtils.isEmpty(mediaUrl)) {
                    sb.append(mediaUrl).append("+++++");
                }
            }
        }

        if (urlEntities != null && urlEntities.length > 0) {
            for (URLEntity urlEntity : urlEntities) {
                String detected = TweetLongerUtils.detectTwitLonger(urlEntity.getExpandedURL());
                if (!TextUtils.isEmpty(detected)) {
                    text = detected;
                    break;
                }
            }
        }

        String media = null;
        if (!TextUtils.isEmpty(sb.toString()))
            media = sb.toString();

        if (status.getMediaEntities() != null && status.getMediaEntities().length > 0) {
            MediaEntity mediaEntity = status.getMediaEntities()[0];
            text = text.replaceAll(mediaEntity.getURL(), mediaEntity.getDisplayURL());

            if (TextUtils.isEmpty(media))
                media = mediaEntity.getMediaURL();
        }

        return new AutoParcel_Tweet(0, tweetId, inReplyToStatus, createdAt, text, favorited,
                retweetedByMe, retweetedBy, username, fullname, avatar, media, source, mentions);
    }

    public static Tweet from(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(TweetContract._ID));
        long tweetId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetContract.TWEET_ID));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(TweetContract.CREATED_AT));
        long inReplyToStatus = cursor.getLong(cursor.getColumnIndexOrThrow(TweetContract.IN_REPLY_TO_STATUS));
        String avatar = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.AVATAR));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.USERNAME));
        String fullname = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.FULLNAME));
        boolean favorited = cursor.getInt(cursor.getColumnIndexOrThrow(TweetContract.FAVORITED)) == 1;
        boolean retweetedByMe = cursor.getInt(cursor.getColumnIndexOrThrow(TweetContract.RETWEETED_BY_ME)) == 1;
        String retweetedBy = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.RETWEETED_BY));
        String text = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.TEXT));
        String media = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.MEDIA));
        String mentions = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.MENTIONS));
        String source = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.SOURCE));

        return new AutoParcel_Tweet(id, tweetId, inReplyToStatus, createdAt, text, favorited,
                retweetedByMe, retweetedBy, username, fullname, avatar, media, source, mentions);
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(TweetContract.TWEET_ID, tweetId());
        values.put(TweetContract.TEXT, text());
        values.put(TweetContract.SOURCE, source());
        values.put(TweetContract.AVATAR, avatar());
        values.put(TweetContract.TWEET_ID, tweetId());
        values.put(TweetContract.CREATED_AT, createdAt());
        values.put(TweetContract.FAVORITED, favorited());
        values.put(TweetContract.FULLNAME, fullname());
        values.put(TweetContract.MEDIA, media());
        values.put(TweetContract.MENTIONS, mentions());
        values.put(TweetContract.USERNAME, username());
        values.put(TweetContract.RETWEETED_BY, retweetedBy());
        values.put(TweetContract.RETWEETED_BY_ME, retweetedByMe());
        values.put(TweetContract.IN_REPLY_TO_STATUS, inReplyToStatus());

        return values;
    }
}
