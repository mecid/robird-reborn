package com.aaplab.robird.data.provider.contract;

import android.net.Uri;

import com.aaplab.robird.data.provider.RobirdContentProvider;
import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Created by majid on 17.08.15.
 */
public interface UserListContract extends ProviGenBaseContract {

    public static final String TABLE_NAME = "list";

    @Column(Column.Type.INTEGER)
    public static final String ACCOUNT_ID = "account_id";

    @Column(Column.Type.INTEGER)
    public static final String LIST_ID = "list_id";

    @Column(Column.Type.TEXT)
    public static final String NAME = "name";

    @ContentUri
    public static final Uri CONTENT_URI = Uri.parse(RobirdContentProvider.BASE_URI + TABLE_NAME);

    public static final String[] PROJECTION = new String[]{
            _ID, NAME, LIST_ID
    };
}
