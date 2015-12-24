package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.net.Uri;

import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

import timber.log.Timber;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public final class StreamModel extends BaseTwitterModel {
    private final TwitterStream mTwitterStream;
    private final SqlBriteContentProvider mSqlBriteContentProvider;

    public StreamModel(Account account) {
        super(account);

        mTwitterStream = new TwitterStreamFactory().getInstance(mTwitter.getAuthorization());
        mSqlBriteContentProvider = SqlBriteContentProvider.create(Inject.contentResolver());
    }

    public void start() {
        if (mTwitterStream != null) {
            mTwitterStream.addListener(new UserStatusListener());
            mTwitterStream.user();
        }
    }

    public void stop() {
        if (mTwitterStream != null) {
            // remove reference to listener
            mTwitterStream.clearListeners();

            // shutdown stream consuming thread
            mTwitterStream.cleanUp();
        }
    }

    private class UserStatusListener implements StatusListener {

        @Override
        public void onStatus(Status status) {
            Tweet tweet = Tweet.from(status);

            // TODO resolve TimeLine ID
            ContentValues contentValues = tweet.toContentValues();
            contentValues.put(TweetContract.ACCOUNT_ID, mAccount.id());
            contentValues.put(TweetContract.TIMELINE_ID, 1);

            Uri uri = mSqlBriteContentProvider
                    .insert(TweetContract.CONTENT_URI, contentValues)
                    .toBlocking()
                    .first();

            Timber.d("Inserted new tweet with URI=" + uri.toString());
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            Integer status = mSqlBriteContentProvider.delete(TweetContract.CONTENT_URI,
                    String.format("%s=%d AND %s=%d",
                            TweetContract.TWEET_ID, statusDeletionNotice.getStatusId(),
                            TweetContract.ACCOUNT_ID, mAccount.id()), null)
                    .toBlocking()
                    .first();

            Timber.d(String.format("Deleting tweet with id=%d; Removal status: %d",
                    statusDeletionNotice.getStatusId(), status));
        }

        @Override
        public void onException(Exception ex) {
            Timber.d(ex, "StreamModel exception");
        }

        @Override
        public final void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            // Stub implementation
        }

        @Override
        public final void onScrubGeo(long userId, long upToStatusId) {
            // Stub implementation
        }

        @Override
        public final void onStallWarning(StallWarning warning) {
            // Stub implementation
        }
    }
}
