package com.aaplab.robird.data.model;

import com.aaplab.robird.data.entity.Account;

import rx.Observable;
import rx.Subscriber;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by majid on 14.05.15.
 */
public class UserModel extends BaseTwitterModel {

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
}
