package com.aaplab.robird.data.model;

import android.content.ContentValues;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.SqlBriteContentProvider;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.provider.contract.TweetContract;
import com.aaplab.robird.inject.Inject;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.TweetMarkerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;

/**
 * Created by majid on 13.05.15.
 */
public class TimelineModel extends BaseTwitterModel {

    public static final long HOME_ID = 1;
    public static final long MENTIONS_ID = 2;
    public static final long RETWEETS_ID = 3;
    public static final long FAVORITES_ID = 4;

    private static final String REFRESHING = "refreshing?account=%s&type=%d";
    private static final String POSITION = "position?account=%s&type=%d";
    private static final String UNREAD = "unread?account=%s&type=%d";

    private static final Set<String> refreshingSet = Collections.synchronizedSet(new HashSet<String>());

    private final SqlBriteContentProvider mSqlBriteContentProvider =
            SqlBriteContentProvider.create(Inject.contentResolver());
    private final PrefsModel mPrefsModel = new PrefsModel();
    private final long mTimelineId;

    public TimelineModel(Account account, long timelineId) {
        super(account);
        mTimelineId = timelineId;
    }

    public void saveLastUnread(long id) {
        Inject.preferences().edit().putLong(
                String.format(UNREAD, mAccount.screenName(), mTimelineId), id
        ).apply();
    }

    public long lastUnread() {
        return Inject.preferences().getLong(
                String.format(UNREAD, mAccount.screenName(), mTimelineId), 0
        );
    }

    public void saveTimelinePosition(long position) {
        Inject.preferences().edit().putLong(
                String.format(POSITION, mAccount.screenName(), mTimelineId), position
        ).apply();

        if (mPrefsModel.isTweetMarkerEnabled())
            saveTweetMarker(position)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultObserver<Long>() {
                    });
    }

    public long timelinePosition() {
        return Inject.preferences().getLong(
                String.format(POSITION, mAccount.screenName(), mTimelineId), 0
        );
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
        if (isRefreshing())
            return Observable.just(0);

        setRefreshing(true);
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
        if (isRefreshing())
            return Observable.just(0);

        if (mPrefsModel.isTweetMarkerEnabled())
            getTweetMarker()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DefaultObserver<Long>() {
                        @Override
                        public void onNext(Long position) {
                            super.onNext(position);
                            saveTimelinePosition(position);
                        }
                    });

        setRefreshing(true);
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
                } finally {
                    setRefreshing(false);
                }
            }
        });
    }

    private boolean isRefreshing() {
        return refreshingSet.contains(String.format(REFRESHING, mAccount.screenName(), mTimelineId));
    }

    private void setRefreshing(boolean refreshing) {
        if (refreshing)
            refreshingSet.add(String.format(REFRESHING, mAccount.screenName(), mTimelineId));
        else
            refreshingSet.remove(String.format(REFRESHING, mAccount.screenName(), mTimelineId));
    }

    private Observable<Long> saveTweetMarker(final long tweetId) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                TweetMarkerUtils.save(tweetMarkerCollection(), tweetId, mAccount.screenName(),
                        (OAuthAuthorization) mTwitter.getAuthorization());
                subscriber.onNext(tweetId);
                subscriber.onCompleted();
            }
        });
    }

    private Observable<Long> getTweetMarker() {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                subscriber.onNext(TweetMarkerUtils.get(tweetMarkerCollection(), mAccount.screenName()));
                subscriber.onCompleted();
            }
        });
    }

    private String tweetMarkerCollection() {
        String collection;

        if (mTimelineId == HOME_ID) {
            collection = TweetMarkerUtils.TIMELINE;
        } else if (mTimelineId == MENTIONS_ID) {
            collection = TweetMarkerUtils.MENTIONS;
        } else if (mTimelineId == FAVORITES_ID) {
            collection = TweetMarkerUtils.FAVORITES;
        } else if (mTimelineId == RETWEETS_ID) {
            collection = TweetMarkerUtils.RETWEETS;
        } else {
            collection = String.valueOf(mTimelineId);
        }

        return collection;
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
