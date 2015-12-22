package com.aaplab.robird.data.model;

import com.aaplab.robird.Config;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public final class StreamModel {

    // Only one instance of TwitterStream per StreamModel
    private final TwitterStream mTwitterStream;

    public StreamModel(Account account) {
        mTwitterStream = configureTwitterStream(account);
    }

    private static TwitterStream configureTwitterStream(Account account) {
        final ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.setOAuthConsumerKey(Config.TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(Config.TWITTER_CONSUMER_SECRET);
        builder.setOAuthAccessTokenSecret(account.tokenSecret());
        builder.setOAuthAccessToken(account.token());

        return new TwitterStreamFactory(builder.build()).getInstance();
    }

    public void start() {
        System.out.println("Called");

        RxStatusListener listener = new RxStatusListener();
        Observable<Tweet> tweetStream = Observable.create(listener);

        mTwitterStream.addListener(listener);

        // TODO implement custom logic in order to prevent status bursting.
        tweetStream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Tweet>() {
                    @Override
                    public void call(Tweet tweet) {
                        System.out.println("Tweet: " + tweet.fullname() + " \n" + tweet.text());
                    }
                });

        mTwitterStream.user();
    }

    public void stop() {
        mTwitterStream.clearListeners();
        mTwitterStream.cleanUp();
        mTwitterStream.shutdown();
    }

    private static class RxStatusListener implements StatusListener, Observable.OnSubscribe<Tweet> {
        private Subscriber<? super Tweet> mSubscriber;

        public RxStatusListener() {
            mSubscriber = null;
        }

        @Override
        public void call(Subscriber<? super Tweet> subscriber) {
            mSubscriber = subscriber;
        }

        @Override
        public void onStatus(Status status) {
            // if we there is subscriber, we can push status down to it as soon as it arrives.
            if (mSubscriber != null) {
                Tweet tweet = Tweet.from(status);
                mSubscriber.onNext(tweet);
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            // Should find a way to push deletion notice to subscriber
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
            if (mSubscriber != null) {
                mSubscriber.onError(ex);
                mSubscriber.onCompleted();
            }
        }
    }
}
