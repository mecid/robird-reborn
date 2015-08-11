package com.aaplab.robird.data.model;

import com.aaplab.robird.data.MapFunctions;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import twitter4j.Query;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by majid on 09.08.15.
 */
public class SearchModel extends BaseTwitterModel {

    public SearchModel(Account account) {
        super(account);
    }

    public Observable<List<Tweet>> tweets(final Query query) {
        return Observable.create(new Observable.OnSubscribe<List<Status>>() {
            @Override
            public void call(Subscriber<? super List<Status>> subscriber) {
                try {
                    subscriber.onNext(mTwitter.search(query).getTweets());
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        }).map(MapFunctions.STATUSES_TO_TWEETS);
    }

    public Observable<List<User>> users(final String query, final int page) {
        return Observable.create(new Observable.OnSubscribe<List<User>>() {
            @Override
            public void call(Subscriber<? super List<User>> subscriber) {
                try {
                    subscriber.onNext(mTwitter.searchUsers(query, page));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<SavedSearch>> savedSearch() {
        return Observable.create(new Observable.OnSubscribe<List<SavedSearch>>() {
            @Override
            public void call(Subscriber<? super List<SavedSearch>> subscriber) {
                try {
                    subscriber.onNext(mTwitter.getSavedSearches());
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<SavedSearch> save(final String query) {
        return Observable.create(new Observable.OnSubscribe<SavedSearch>() {
            @Override
            public void call(Subscriber<? super SavedSearch> subscriber) {
                try {
                    subscriber.onNext(mTwitter.createSavedSearch(query));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<SavedSearch> remove(final long id) {
        return Observable.create(new Observable.OnSubscribe<SavedSearch>() {
            @Override
            public void call(Subscriber<? super SavedSearch> subscriber) {
                try {
                    subscriber.onNext(mTwitter.destroySavedSearch(id));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
