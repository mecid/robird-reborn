package com.aaplab.robird.data.provider.contract;

import android.net.Uri;

import com.aaplab.robird.data.provider.RobirdContentProvider;
import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Created by majid on 18.08.15.
 */
public interface DirectContract extends ProviGenBaseContract {
    public static final String TABLE_NAME = "direct";

    @Column(Column.Type.INTEGER)
    public static final String DIRECT_ID = "direct_id";

    @Column(Column.Type.TEXT)
    public static final String USERNAME = "username";

    @Column(Column.Type.TEXT)
    public static final String FULLNAME = "fullname";

    @Column(Column.Type.TEXT)
    public static final String AVATAR = "avatar";

    @Column(Column.Type.TEXT)
    public static final String RECIPIENT = "recipient";

    @Column(Column.Type.TEXT)
    public static final String RECIPIENT_FULLNAME = "recipient_fullname";

    @Column(Column.Type.TEXT)
    public static final String RECIPIENT_AVATAR = "recipient_avatar";

    @Column(Column.Type.INTEGER)
    public static final String CREATED_AT = "created_at";

    @Column(Column.Type.TEXT)
    public static final String TEXT = "text";

    @Column(Column.Type.INTEGER)
    public static final String ACCOUNT_ID = "account_id";

    @ContentUri
    public static final Uri CONTENT_URI = Uri.parse(RobirdContentProvider.BASE_URI + TABLE_NAME);

    public static final String[] PROJECTION = new String[]{
            _ID, DIRECT_ID, AVATAR, USERNAME, FULLNAME, RECIPIENT, CREATED_AT, TEXT, RECIPIENT_AVATAR, RECIPIENT_FULLNAME
    };
}
