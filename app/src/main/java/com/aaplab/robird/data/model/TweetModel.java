package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.functions.Func1;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by majid on 18.06.15.
 */
public class TweetModel extends BaseTwitterModel {

    private final long mTweetId;

    public TweetModel(Account account, long tweetId) {
        super(account);
        mTweetId = tweetId;
    }

    public Observable<Tweet> tweet() {
        return Observable.defer(new Func0<Observable<Tweet>>() {
            @Override
            public Observable<Tweet> call() {
                return Observable.just(findTweetById(mTweetId));
            }
        }).flatMap(new Func1<Tweet, Observable<Tweet>>() {
            @Override
            public Observable<Tweet> call(Tweet tweet) {
                return tweet != null ?
                        Observable.just(tweet) :
                        detailedStatus().map(new Func1<Status, Tweet>() {
                            @Override
                            public Tweet call(Status status) {
                                return Tweet.from(status);
                            }
                        });
            }
        });
    }

    public Observable<Status> detailedStatus() {
        return Observable.create(new Observable.OnSubscribe<Status>() {
            @Override
            public void call(Subscriber<? super Status> subscriber) {
                try {
                    subscriber.onNext(mTwitter.showStatus(mTweetId));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<Status> retweet() {
        return Observable.create(new Observable.OnSubscribe<Status>() {
            @Override
            public void call(Subscriber<? super Status> subscriber) {
                try {
                    Status status = mTwitter.retweetStatus(mTweetId);
                    updateLocalTweet(status);
                    subscriber.onNext(status);
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<Status> favorite() {
        return Observable.create(new Observable.OnSubscribe<Status>() {
            @Override
            public void call(Subscriber<? super Status> subscriber) {
                try {
                    Status status = mTwitter.showStatus(mTweetId);

                    if (status.isFavorited())
                        status = mTwitter.destroyFavorite(status.getId());
                    else
                        status = mTwitter.createFavorite(status.getId());

                    updateLocalTweet(status);
                    subscriber.onNext(status);
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<Tweet>> replies() {
        return tweet().flatMap(new Func1<Tweet, Observable<List<Tweet>>>() {
            @Override
            public Observable<List<Tweet>> call(final Tweet tweet) {
                return Observable.create(new Observable.OnSubscribe<List<Tweet>>() {
                    @Override
                    public void call(Subscriber<? super List<Tweet>> subscriber) {
                        try {
                            List<Tweet> tweets = new ArrayList<>();
                            QueryResult result = mTwitter.search(new Query("@" + tweet.username()).count(100));

                            for (Status status : result.getTweets())
                                if (status.getInReplyToStatusId() == mTweetId)
                                    tweets.add(Tweet.from(status));

                            subscriber.onNext(tweets);
                            subscriber.onCompleted();
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    }
                });
            }
        });
    }

    public Observable<List<Tweet>> conversation() {
        return tweet().flatMap(new Func1<Tweet, Observable<List<Tweet>>>() {
            @Override
            public Observable<List<Tweet>> call(final Tweet tweet) {
                return Observable.create(new Observable.OnSubscribe<List<Tweet>>() {
                    @Override
                    public void call(Subscriber<? super List<Tweet>> subscriber) {
                        List<Tweet> conversation = new ArrayList<>();
                        long inReplyToStatus = tweet.inReplyToStatus();

                        try {
                            while (inReplyToStatus > 0) {
                                Tweet temp = findTweetById(inReplyToStatus);
                                if (temp == null) {
                                    temp = Tweet.from(mTwitter.showStatus(inReplyToStatus));
                                }

                                conversation.add(temp);
                                inReplyToStatus = temp.inReplyToStatus();
                            }

                            subscriber.onNext(conversation);
                            subscriber.onCompleted();
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    }
                });
            }
        });
    }

    public Observable<Status> delete() {
        return Observable.create(new Observable.OnSubscribe<Status>() {
            @Override
            public void call(Subscriber<? super Status> subscriber) {
                try {
                    subscriber.onNext(mTwitter.destroyStatus(mTweetId));
                    deleteLocalTweet();
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private void updateLocalTweet(Status status) {
        ContentValues values = new ContentValues();

        values.put(TweetContract.FAVORITED, status.isFavorited());
        values.put(TweetContract.RETWEETED_BY_ME, status.isRetweetedByMe());
        values.put(TweetContract.RETWEETED_BY, status.isRetweet() ? status.getUser().getScreenName() : "");

        Inject.contentResolver()
                .update(TweetContract.CONTENT_URI, values,
                        String.format("%s=%d AND %s=%d",
                                TweetContract.ACCOUNT_ID, mAccount.id(),
                                TweetContract.TWEET_ID, mTweetId),
                        null);
    }

    private void deleteLocalTweet() {
        Inject.contentResolver()
                .delete(TweetContract.CONTENT_URI,
                        String.format("%s=%d", TweetContract.TWEET_ID, mTweetId),
                        null);
    }

    private Tweet findTweetById(long id) {
        Cursor cursor = Inject.contentResolver()
                .query(TweetContract.CONTENT_URI,
                        TweetContract.PROJECTION,
                        String.format("%s=%d AND %s=%d",
                                TweetContract.ACCOUNT_ID, mAccount.id(),
                                TweetContract.TWEET_ID, id),
                        null, null);

        Tweet tweet = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            tweet = Tweet.from(cursor);
        }

        cursor.close();
        return tweet;
    }
}
