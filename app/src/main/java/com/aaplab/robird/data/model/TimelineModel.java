package com.aaplab.robird.data.model;

import android.content.ContentValues;
import android.support.annotation.IntDef;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by majid on 13.05.15.
 */
public class TimelineModel extends BaseTwitterModel {

    public static final int TIMELINE_HOME = 0;
    public static final int TIMELINE_MENTIONS = 1;
    public static final int TIMELINE_RETWEETS = 2;
    public static final int TIMELINE_FAVORITES = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TIMELINE_HOME, TIMELINE_MENTIONS, TIMELINE_RETWEETS, TIMELINE_FAVORITES})
    public @interface Type {
    }

    private final SqlBriteContentProvider mSqlBriteContentProvider =
            SqlBriteContentProvider.create(Inject.contentResolver());
    private final int mType;

    public TimelineModel(Account account, int type) {
        super(account);
        mType = type;
    }

    public void saveTimelinePosition(long position) {
        Inject.preferences().edit().putLong(
                String.format("%s#type#%d", mAccount.screenName, mType),
                position).apply();
    }

    public long timelinePosition() {
        return Inject.preferences().getLong(
                String.format("%s#type#%d", mAccount.screenName, mType)
                , 0);
    }

    public Observable<List<Tweet>> timeline() {
        return mSqlBriteContentProvider.query(
                TweetContract.CONTENT_URI, TweetContract.PROJECTION,
                String.format("%s=%d AND %s=%d",
                        TweetContract.ACCOUNT_ID, mAccount.id,
                        TweetContract.TIMELINE_TYPE, mType
                ),
                null, TweetContract.TWEET_ID + " DESC", false)
                .map(MapFunctions.TWEET_LIST);
    }

    public Observable<Integer> update() {
        return timeline()
                .take(1)
                .flatMap(new Func1<List<Tweet>, Observable<List<Status>>>() {
                    @Override
                    public Observable<List<Status>> call(List<Tweet> tweets) {
                        Paging paging = new Paging().count(200);
                        if (!tweets.isEmpty()) paging.sinceId(tweets.get(0).tweetId);
                        return downloadTimeline(paging);
                    }
                })
                .doOnNext(new TimelinePersister(mAccount, mType))
                .map(new Func1<List<Status>, Integer>() {
                    @Override
                    public Integer call(List<Status> statuses) {
                        return statuses.size();
                    }
                });
    }

    public Observable<Integer> makeOld() {
        return timeline()
                .take(1)
                .flatMap(new Func1<List<Tweet>, Observable<List<Status>>>() {
                    @Override
                    public Observable<List<Status>> call(List<Tweet> tweets) {
                        Paging paging = new Paging().count(50);
                        if (!tweets.isEmpty()) paging.maxId(tweets.get(tweets.size() - 1).tweetId);
                        return downloadTimeline(paging);
                    }
                })
                .map(new Func1<List<Status>, List<Status>>() {
                    @Override
                    public List<Status> call(List<Status> statuses) {
                        ArrayList<Status> filtered = new ArrayList<Status>(statuses);
                        filtered.remove(0);
                        return filtered;
                    }
                })
                .doOnNext(new TimelinePersister(mAccount, mType))
                .map(new Func1<List<Status>, Integer>() {
                    @Override
                    public Integer call(List<Status> statuses) {
                        return statuses.size();
                    }
                });
    }

    private Observable<List<Status>> downloadTimeline(final Paging paging) {
        return Observable.create(new Observable.OnSubscribe<List<Status>>() {
            @Override
            public void call(Subscriber<? super List<Status>> subscriber) {
                try {
                    if (mType == TIMELINE_HOME) {
                        subscriber.onNext(mTwitter.getHomeTimeline(paging));
                    } else if (mType == TIMELINE_MENTIONS) {
                        subscriber.onNext(mTwitter.getMentionsTimeline(paging));
                    } else if (mType == TIMELINE_RETWEETS) {
                        subscriber.onNext(mTwitter.getRetweetsOfMe(paging));
                    } else if (mType == TIMELINE_FAVORITES) {
                        subscriber.onNext(mTwitter.getFavorites(paging));
                    }

                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static final class TimelinePersister implements Action1<List<Status>> {
        private Account account;
        private int type;

        public TimelinePersister(Account account, int type) {
            this.account = account;
            this.type = type;
        }

        @Override
        public void call(List<Status> statuses) {
            ArrayList<Tweet> tweets = new ArrayList<>(statuses.size());

            for (Status status : statuses) {
                tweets.add(new Tweet(status));
            }

            ArrayList<ContentValues> values = new ArrayList<>(tweets.size());

            for (Tweet tweet : tweets) {
                ContentValues cv = tweet.toContentValues();
                cv.put(TweetContract.ACCOUNT_ID, account.id);
                cv.put(TweetContract.TIMELINE_TYPE, type);
                values.add(cv);
            }

            ContentValues[] contentValues = new ContentValues[values.size()];
            Inject.contentResolver().bulkInsert(TweetContract.CONTENT_URI, values.toArray(contentValues));
        }
    }
}
