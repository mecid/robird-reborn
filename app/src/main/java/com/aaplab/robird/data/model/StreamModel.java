package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.net.Uri;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

import java.util.List;

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
            System.out.println("AccountID: " + status.getUser().getId());
            System.out.println("Status.text: " + status.getText());

            Tweet tweet = Tweet.from(status);

            ContentValues contentValues = tweet.toContentValues();
            contentValues.put(TweetContract.ACCOUNT_ID, mAccount.id());
            contentValues.put(TweetContract.TIMELINE_ID, 1);

            Uri uri = Inject.contentResolver().insert(TweetContract.CONTENT_URI, contentValues);
            System.out.println("Tweet URI: " + uri);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            System.out.println("StatusRemoved: " + statusDeletionNotice.getStatusId() +
                    "\n" + "UserID: " + statusDeletionNotice.getUserId());

            List<Tweet> persistedTweets = mSqlBriteContentProvider
                    .query(TweetContract.CONTENT_URI, TweetContract.PROJECTION,
                            String.format("%s=%d AND %s=%d",
                                    TweetContract.TWEET_ID, statusDeletionNotice.getStatusId(),
                                    TweetContract.ACCOUNT_ID, mAccount.id()),
                            null, null, false)
                    .map(MapFunctions.TWEET_LIST)
                    .toBlocking()
                    .first();

            if (persistedTweets != null && persistedTweets.size() > 0) {
                Tweet persistedTweet = persistedTweets.get(0);

                Timber.d(String.format("Deleting tweet \"%s\" with id=%d",
                        persistedTweet.text(),
                        persistedTweet.tweetId()));

                Uri persistedTweetUri = Uri.withAppendedPath(TweetContract.CONTENT_URI,
                        String.valueOf(persistedTweet.id()));
                Integer status = mSqlBriteContentProvider
                        .delete(persistedTweetUri, null, null).toBlocking().first();
                System.out.println("Tweet removal status: " + status);
            }
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

        @Override
        public void onException(Exception ex) {
            ex.printStackTrace();
        }
    }
}
