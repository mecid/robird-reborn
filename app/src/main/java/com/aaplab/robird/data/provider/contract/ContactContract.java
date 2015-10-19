package com.aaplab.robird.data.provider.contract;

import android.net.Uri;

import com.aaplab.robird.data.provider.RobirdContentProvider;
import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Created by majid on 17.10.15.
 */
public interface ContactContract extends ProviGenBaseContract {

    public static final String TABLE_NAME = "contact";

    @Column(Column.Type.INTEGER)
    public static final String USER_ID = "user_id";

    @Column(Column.Type.INTEGER)
    public static final String ACCOUNT_ID = "account_id";

    @Column(Column.Type.TEXT)
    public static final String USERNAME = "username";

    @Column(Column.Type.TEXT)
    public static final String AVATAR = "avatar";

    @ContentUri
    public static final Uri CONTENT_URI = Uri.parse(RobirdContentProvider.BASE_URI + TABLE_NAME);

    public static final String[] PROJECTION = new String[]{
            _ID, USER_ID, USERNAME, AVATAR
    };
}
