package com.aaplab.robird.data.provider.contract;

import android.net.Uri;

import com.aaplab.robird.data.provider.RobirdContentProvider;
import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Created by majid on 16.01.15.
 */
public interface AccountContract extends ProviGenBaseContract {

    public static final String TABLE_NAME = "account";

    @Column(Column.Type.TEXT)
    public static final String TOKEN = "token";

    @Column(Column.Type.TEXT)
    public static final String TOKEN_SECRET = "token_secret";

    @Column(Column.Type.INTEGER)
    public static final String USER_ID = "user_id";

    @Column(Column.Type.TEXT)
    public static final String SCREEN_NAME = "screen_name";

    @Column(Column.Type.TEXT)
    public static final String FULL_NAME = "full_name";

    @Column(Column.Type.TEXT)
    public static final String AVATAR = "avatar";

    @Column(Column.Type.TEXT)
    public static final String USER_BACKGROUND = "user_background";

    @Column(Column.Type.INTEGER)
    public static final String ACTIVE = "active";

    @ContentUri
    public static final Uri CONTENT_URI = Uri.parse(RobirdContentProvider.BASE_URI + TABLE_NAME);

    public static final String[] PROJECTION = new String[]{
            _ID, TOKEN, TOKEN_SECRET, USER_ID, SCREEN_NAME, FULL_NAME, AVATAR, USER_BACKGROUND, ACTIVE
    };
}
