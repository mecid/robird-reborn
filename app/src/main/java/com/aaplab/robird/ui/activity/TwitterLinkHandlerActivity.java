package com.aaplab.robird.ui.activity;

import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.TweetModel;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import twitter4j.Status;

/**
 * Created by majid on 28.09.15.
 */
public class TwitterLinkHandlerActivity extends BaseActivity {
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String AUTHORITY_TWITTER_COM = "twitter.com";

    private static final int URI_CODE_TWITTER_STATUS = 1;
    private static final int URI_CODE_TWITTER_USER = 2;

    static {
        URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#", URI_CODE_TWITTER_STATUS);
        URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#/photo/#", URI_CODE_TWITTER_STATUS);
        URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*", URI_CODE_TWITTER_USER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        final Account activeAccount = new AccountModel().accounts().toBlocking().first().get(0);
        final Uri data = getIntent().getData();

        if (data == null) {
            finish();
            return;
        }

        final Uri uri = data.buildUpon().authority(AUTHORITY_TWITTER_COM).build();
        final List<String> pathSegments = uri.getPathSegments();

        switch (URI_MATCHER.match(uri)) {
            case URI_CODE_TWITTER_STATUS: {
                new TweetModel(activeAccount, Long.parseLong(pathSegments.get(2)))
                        .tweet()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Func1<Status, Tweet>() {
                            @Override
                            public Tweet call(Status status) {
                                return Tweet.from(status);
                            }
                        })
                        .subscribe(new DefaultObserver<Tweet>() {
                            @Override
                            public void onNext(Tweet tweet) {
                                super.onNext(tweet);
                                TweetDetailsActivity.start(TwitterLinkHandlerActivity.this,
                                        activeAccount, tweet);
                                finish();
                            }
                        });
                break;
            }

            case URI_CODE_TWITTER_USER: {
                UserProfileActivity.start(this, activeAccount, pathSegments.get(0));
                finish();
                break;
            }
        }
    }
}
