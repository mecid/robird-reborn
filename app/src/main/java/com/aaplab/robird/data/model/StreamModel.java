package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.net.Uri;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

import timber.log.Timber;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public final class StreamModel extends BaseTwitterModel {
    private TwitterStream mTwitterStream;

    public StreamModel(Account account) {
        super(account);

        try {
            mTwitterStream = new TwitterStreamFactory()
                    .getInstance(mTwitter.getOAuthAccessToken());
        } catch (TwitterException e) {
            Timber.d(e, "Access token is not available or expired");
        }
    }

    public void start() {
        System.out.println("Called");

        if (mTwitterStream != null) {
            mTwitterStream.addListener(new UserStatusListener(mAccount));
            mTwitterStream.user();
        }
    }

    public void stop() {
        if (mTwitterStream != null) {
            mTwitterStream.clearListeners();
            mTwitterStream.cleanUp();
            mTwitterStream.shutdown();
        }
    }

    private static class UserStatusListener implements StatusListener {
        private final Account account;

        public UserStatusListener(Account account) {
            this.account = account;
        }

        @Override
        public void onStatus(Status status) {
            System.out.println("Status.text: " + status.getText());
            System.out.println("Scopes: " + status.getScopes());
            for (String id : status.getScopes().getPlaceIds()) {
                System.out.println("ID: " + id);
            }

            Tweet tweet = Tweet.from(status);

            ContentValues contentValues = tweet.toContentValues();
            contentValues.put(TweetContract.ACCOUNT_ID, account.id());
            contentValues.put(TweetContract.TIMELINE_ID, 1);

            Uri uri = Inject.contentResolver().insert(TweetContract.CONTENT_URI, contentValues);
            System.out.println("Tweet URI: " + uri);
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

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
