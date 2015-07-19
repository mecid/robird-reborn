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
import twitter4j.Paging;
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
    public @interface Type {
    }

    public UserModel(Account account) {
        super(account);
    }

    public Observable<User> user(final String screenName) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                try {
                    subscriber.onNext(mTwitter.showUser(screenName));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<Tweet>> tweets(final String screenName,
                                          final int type,
                                          final Paging paging) {
        return Observable.create(new Observable.OnSubscribe<List<Status>>() {
            @Override
            public void call(Subscriber<? super List<Status>> subscriber) {
                try {
                    subscriber.onNext(
                            type == TWEETS ?
                                    mTwitter.getUserTimeline(screenName, paging) :
                                    mTwitter.getFavorites(screenName, paging)
                    );
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        }).map(MapFunctions.STATUSES_TO_TWEETS);
    }
}
