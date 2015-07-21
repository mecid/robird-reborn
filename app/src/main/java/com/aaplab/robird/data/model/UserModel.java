package com.aaplab.robird.data.model;

import android.support.annotation.IntDef;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by majid on 14.05.15.
 */
public class UserModel extends BaseTwitterModel {

    public static final int TWEETS = 0;
    public static final int FAVORITES = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TWEETS, FAVORITES})
    public @interface TimelineType {
    }

    public static final int FRIENDS = 0;
    public static final int FOLLOWERS = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FRIENDS, FOLLOWERS})
    public @interface FriendsType {
    }

    private final String mScreenName;

    public UserModel(Account account, String screenName) {
        super(account);
        mScreenName = screenName;
    }

    public Observable<User> user() {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    subscriber.onNext(mTwitter.showUser(mScreenName));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<Relationship> report() {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    subscriber.onNext(mTwitter.reportSpam(mScreenName));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        }).flatMap(new Func1<User, Observable<Relationship>>() {
            @Override
            public Observable<Relationship> call(User user) {
                return relationship();
            }
        });
    }

    public Observable<Relationship> follow() {
        return relationship()
                .flatMap(new Func1<Relationship, Observable<User>>() {
                    @Override
                    public Observable<User> call(final Relationship relationship) {
                        return Observable.create(new Observable.OnSubscribe<User>() {
                            @Override
                            public void call(Subscriber<? super User> subscriber) {
                                try {
                                    if (relationship.isSourceFollowingTarget())
                                        subscriber.onNext(mTwitter.destroyFriendship(mScreenName));
                                    else
                                        subscriber.onNext(mTwitter.createFriendship(mScreenName));
                                    subscriber.onCompleted();
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<User, Observable<Relationship>>() {
                    @Override
                    public Observable<Relationship> call(User user) {
                        return relationship();
                    }
                });
    }

    public Observable<Relationship> block() {
        return relationship()
                .flatMap(new Func1<Relationship, Observable<User>>() {
                    @Override
                    public Observable<User> call(final Relationship relationship) {
                        return Observable.create(new Observable.OnSubscribe<User>() {
                            @Override
                            public void call(Subscriber<? super User> subscriber) {
                                try {
                                    if (relationship.isSourceBlockingTarget())
                                        subscriber.onNext(mTwitter.destroyBlock(mScreenName));
                                    else
                                        subscriber.onNext(mTwitter.createBlock(mScreenName));
                                    subscriber.onCompleted();
                                } catch (TwitterException e) {
                                    subscriber.onError(e);
                                }
                            }
                        });
                    }
                })
                .flatMap(new Func1<User, Observable<Relationship>>() {
                    @Override
                    public Observable<Relationship> call(User user) {
                        return relationship();
                    }
                });
    }

    public Observable<Relationship> relationship() {
        return Observable.create(new Observable.OnSubscribe<Relationship>() {
            @Override
            public void call(Subscriber<? super Relationship> subscriber) {
                try {
                    subscriber.onNext(mTwitter.showFriendship(mAccount.screenName(), mScreenName));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<Tweet>> tweets(final int type,
                                          final Paging paging) {
        return Observable.create(new Observable.OnSubscribe<List<Status>>() {
            @Override
            public void call(Subscriber<? super List<Status>> subscriber) {
                try {
                    subscriber.onNext(
                            type == TWEETS ?
                                    mTwitter.getUserTimeline(mScreenName, paging) :
                                    mTwitter.getFavorites(mScreenName, paging)
                    );
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        }).map(MapFunctions.STATUSES_TO_TWEETS);
    }

    public Observable<PagableResponseList<User>> friends(final int type,
                                                         final long cursor) {
        return Observable.create(new Observable.OnSubscribe<PagableResponseList<User>>() {
            @Override
            public void call(Subscriber<? super PagableResponseList<User>> subscriber) {
                try {
                    subscriber.onNext(
                            type == FRIENDS ?
                                    mTwitter.getFriendsList(mScreenName, cursor) :
                                    mTwitter.getFollowersList(mScreenName, cursor)
                    );
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
