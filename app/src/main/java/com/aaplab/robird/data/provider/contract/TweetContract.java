package com.aaplab.robird.data.provider.contract;

import android.net.Uri;

import com.aaplab.robird.data.provider.RobirdContentProvider;
import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Created by majid on 19.01.15.
 */
public interface TweetContract extends ProviGenBaseContract {

    public static final String TABLE_NAME = "tweet";

    @Column(Column.Type.INTEGER)
    public static final String TWEET_ID = "tweet_id";

    @Column(Column.Type.INTEGER)
    public static final String IN_REPLY_TO_STATUS = "in_reply_to_status";

    @Column(Column.Type.INTEGER)
    public static final String CREATED_AT = "created_at";

    @Column(Column.Type.TEXT)
    public static final String TEXT = "text";

    @Column(Column.Type.INTEGER)
    public static final String FAVORITED = "favorited";

    @Column(Column.Type.INTEGER)
    public static final String RETWEETED_BY_ME = "retweeted_by_me";

    @Column(Column.Type.TEXT)
    public static final String RETWEETED_BY = "retweeted_by";

    @Column(Column.Type.TEXT)
    public static final String USERNAME = "username";

    @Column(Column.Type.TEXT)
    public static final String FULLNAME = "fullname";

    @Column(Column.Type.TEXT)
    public static final String AVATAR = "avatar";

    @Column(Column.Type.TEXT)
    public static final String SOURCE = "source";

    @Column(Column.Type.TEXT)
    public static final String MEDIA = "media";

    @Column(Column.Type.TEXT)
    public static final String VIDEO = "video";

    @Column(Column.Type.TEXT)
    public static final String MENTIONS = "mentions";

    @Column(Column.Type.INTEGER)
    public static final String QUOTED_ID = "quoted_id";

    @Column(Column.Type.TEXT)
    public static final String QUOTED_TEXT = "quoted_text";

    @Column(Column.Type.TEXT)
    public static final String QUOTED_NAME = "quoted_name";

    @Column(Column.Type.TEXT)
    public static final String QUOTED_SCREEN_NAME = "quoted_screen_name";

    @Column(Column.Type.TEXT)
    public static final String QUOTED_MEDIA = "quoted_media";

    @Column(Column.Type.INTEGER)
    public static final String ACCOUNT_ID = "account_id";

    @Column(Column.Type.INTEGER)
    public static final String TIMELINE_ID = "timeline_id";

    @ContentUri
    public static final Uri CONTENT_URI = Uri.parse(RobirdContentProvider.BASE_URI + TABLE_NAME);

    public static final String[] PROJECTION = new String[]{
            _ID, TWEET_ID, IN_REPLY_TO_STATUS, CREATED_AT, TEXT, FAVORITED, RETWEETED_BY_ME,
            USERNAME, FULLNAME, AVATAR, SOURCE, MEDIA, VIDEO, MENTIONS, RETWEETED_BY,
            QUOTED_MEDIA, QUOTED_NAME, QUOTED_SCREEN_NAME, QUOTED_TEXT, QUOTED_ID
    };
}
