package com.aaplab.robird.data.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.util.TweetLongerUtils;
import com.aaplab.robird.util.TweetUtils;

import java.io.Serializable;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by majid on 19.01.15.
 */
public class Tweet implements Serializable {
    public int id;
    public long tweetId;
    public long inReplyToStatus;
    public long createdAt;
    public String text;
    public boolean favorited;
    public boolean retweetedByMe;
    public String retweetedBy;
    public String username;
    public String fullname;
    public String avatar;
    public String media;
    public String source;
    public String mentions;

    public Tweet() {
    }

    public Tweet(Status status) {
        tweetId = status.getId();
        createdAt = status.getCreatedAt().getTime();
        source = TweetUtils.getSourceName(status.getSource());

        retweetedByMe = status.isRetweetedByMe();
        if (status.isRetweet()) {
            retweetedBy = status.getUser().getScreenName();
            status = status.getRetweetedStatus();
        }

        text = status.getText();
        fullname = status.getUser().getName();
        username = status.getUser().getScreenName();
        avatar = status.getUser().getOriginalProfileImageURL();
        inReplyToStatus = status.getInReplyToStatusId();
        favorited = status.isFavorited();

        StringBuilder sb = new StringBuilder();
        for (UserMentionEntity mention : status.getUserMentionEntities()) {
            sb.append("@").append(mention.getScreenName()).append(" ");
        }
        mentions = sb.toString();
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

        if (!TextUtils.isEmpty(sb.toString()))
            media = sb.toString();

        if (status.getMediaEntities() != null && status.getMediaEntities().length > 0) {
            MediaEntity mediaEntity = status.getMediaEntities()[0];
            text = text.replaceAll(mediaEntity.getURL(), mediaEntity.getDisplayURL());

            if (TextUtils.isEmpty(media))
                media = mediaEntity.getMediaURL();
        }
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(TweetContract.TWEET_ID, tweetId);
        values.put(TweetContract.TEXT, text);
        values.put(TweetContract.SOURCE, source);
        values.put(TweetContract.AVATAR, avatar);
        values.put(TweetContract.TWEET_ID, tweetId);
        values.put(TweetContract.CREATED_AT, createdAt);
        values.put(TweetContract.FAVORITED, favorited);
        values.put(TweetContract.FULLNAME, fullname);
        values.put(TweetContract.MEDIA, media);
        values.put(TweetContract.MENTIONS, mentions);
        values.put(TweetContract.USERNAME, username);
        values.put(TweetContract.RETWEETED_BY, retweetedBy);
        values.put(TweetContract.RETWEETED_BY_ME, retweetedByMe);
        values.put(TweetContract.IN_REPLY_TO_STATUS, inReplyToStatus);

        return values;
    }

    public static Tweet from(Cursor cursor) {
        Tweet tweet = new Tweet();

        tweet.id = cursor.getInt(cursor.getColumnIndexOrThrow(TweetContract._ID));
        tweet.tweetId = cursor.getLong(cursor.getColumnIndexOrThrow(TweetContract.TWEET_ID));
        tweet.avatar = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.AVATAR));
        tweet.username = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.USERNAME));
        tweet.fullname = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.FULLNAME));
        tweet.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(TweetContract.CREATED_AT));
        tweet.favorited = cursor.getInt(cursor.getColumnIndexOrThrow(TweetContract.FAVORITED)) == 1;
        tweet.retweetedByMe = cursor.getInt(cursor.getColumnIndexOrThrow(TweetContract.RETWEETED_BY_ME)) == 1;
        tweet.retweetedBy = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.RETWEETED_BY));
        tweet.inReplyToStatus = cursor.getLong(cursor.getColumnIndexOrThrow(TweetContract.IN_REPLY_TO_STATUS));
        tweet.text = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.TEXT));
        tweet.media = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.MEDIA));
        tweet.mentions = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.MENTIONS));
        tweet.source = cursor.getString(cursor.getColumnIndexOrThrow(TweetContract.SOURCE));

        return tweet;
    }
}
