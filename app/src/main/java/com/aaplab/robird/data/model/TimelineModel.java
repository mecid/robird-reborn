package com.aaplab.robird.data.model;

import android.content.ContentValues;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;

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

    public static final long HOME_ID = 1;
    public static final long MENTIONS_ID = 2;
    public static final long RETWEETS_ID = 3;
    public static final long FAVORITES_ID = 4;

    private final SqlBriteContentProvider mSqlBriteContentProvider =
            SqlBriteContentProvider.create(Inject.contentResolver());
    private final long mTimelineId;

    public TimelineModel(Account account, long timelineId) {
        super(account);
        mTimelineId = timelineId;
    }

    public void saveTimelinePosition(long position) {
        Inject.preferences().edit().putLong(
                String.format("%s#type#%d", mAccount.screenName(), mTimelineId),
                position).apply();
    }

    public long timelinePosition() {
        return Inject.preferences().getLong(
                String.format("%s#type#%d", mAccount.screenName(), mTimelineId)
                , 0);
    }

    public Observable<List<Tweet>> timeline() {
        return mSqlBriteContentProvider.query(
                TweetContract.CONTENT_URI, TweetContract.PROJECTION,
                String.format("%s=%d AND %s=%d",
                        TweetContract.ACCOUNT_ID, mAccount.id(),
                        TweetContract.TIMELINE_ID, mTimelineId
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
                        if (!tweets.isEmpty()) paging.sinceId(tweets.get(0).tweetId());
                        return downloadTimeline(paging);
                    }
                })
                .doOnNext(new TimelinePersister(mAccount, mTimelineId))
                .map(new Func1<List<Status>, Integer>() {
                    @Override
                    public Integer call(List<Status> statuses) {
                        return statuses.size();
                    }
                });
    }

    public Observable<Integer> old() {
        return timeline()
                .take(1)
                .flatMap(new Func1<List<Tweet>, Observable<List<Status>>>() {
                    @Override
                    public Observable<List<Status>> call(List<Tweet> tweets) {
                        Paging paging = new Paging().count(50);
                        if (!tweets.isEmpty())
                            paging.maxId(tweets.get(tweets.size() - 1).tweetId());
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
                .doOnNext(new TimelinePersister(mAccount, mTimelineId))
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
                    if (mTimelineId == HOME_ID) {
                        subscriber.onNext(mTwitter.getHomeTimeline(paging));
                    } else if (mTimelineId == MENTIONS_ID) {
                        subscriber.onNext(mTwitter.getMentionsTimeline(paging));
                    } else if (mTimelineId == RETWEETS_ID) {
                        subscriber.onNext(mTwitter.getRetweetsOfMe(paging));
                    } else if (mTimelineId == FAVORITES_ID) {
                        subscriber.onNext(mTwitter.getFavorites(paging));
                    } else {
                        subscriber.onNext(mTwitter.getUserListStatuses(mTimelineId, paging));
                    }

                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static final class TimelinePersister implements Action1<List<Status>> {
        private final Account account;
        private final long timelineId;

        public TimelinePersister(Account account, long timelineId) {
            this.account = account;
            this.timelineId = timelineId;
        }

        @Override
        public void call(List<Status> statuses) {
            ArrayList<Tweet> tweets = new ArrayList<>(statuses.size());

            for (Status status : statuses) {
                tweets.add(Tweet.from(status));
            }

            ArrayList<ContentValues> values = new ArrayList<>(tweets.size());

            for (Tweet tweet : tweets) {
                ContentValues cv = tweet.toContentValues();
                cv.put(TweetContract.ACCOUNT_ID, account.id());
                cv.put(TweetContract.TIMELINE_ID, timelineId);
                values.add(cv);
            }

            ContentValues[] contentValues = new ContentValues[values.size()];
            Inject.contentResolver().bulkInsert(TweetContract.CONTENT_URI, values.toArray(contentValues));
        }
    }
}
