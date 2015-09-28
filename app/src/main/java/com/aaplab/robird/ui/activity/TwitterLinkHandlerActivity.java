package com.aaplab.robird.ui.activity;

import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;

import java.util.List;

/**
 * Created by majid on 28.09.15.
 */
public class TwitterLinkHandlerActivity extends BaseActivity {
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String AUTHORITY_TWITTER_COM = "twitter.com";

    private static final int URI_CODE_TWITTER_USER = 2;

    static {
        URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*", URI_CODE_TWITTER_USER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Account activeAccount = new AccountModel().accounts().toBlocking().first().get(0);

        final Uri data = getIntent().getData();
        if (data == null) {
            finish();
            return;
        }

        final Uri uri = data.buildUpon().authority(AUTHORITY_TWITTER_COM).build();
        final List<String> pathSegments = uri.getPathSegments();

        switch (URI_MATCHER.match(uri)) {
            case URI_CODE_TWITTER_USER: {
                UserProfileActivity.start(this, activeAccount, pathSegments.get(0));
                finish();
                break;
            }

            default:
                finish();
        }
    }
}
