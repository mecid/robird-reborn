package com.aaplab.robird.data.model;

import com.aaplab.robird.data.entity.Account;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import twitter4j.GeoLocation;
import twitter4j.Location;
import twitter4j.Trend;
import twitter4j.TwitterException;

/**
 * Created by majid on 25.08.15.
 */
public class TrendsModel extends BaseTwitterModel {
    public static final int GLOBAL = 1;
    public static final int LOCAL = -1;

    public TrendsModel(Account account) {
        super(account);
    }

    public Observable<List<Trend>> trends(final int woeid) {
        return Observable.create(new Observable.OnSubscribe<List<Trend>>() {
            @Override
            public void call(Subscriber<? super List<Trend>> subscriber) {
                try {
                    subscriber.onNext(Arrays.asList(mTwitter.getPlaceTrends(woeid).getTrends()));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<Trend>> local(final GeoLocation location) {
        return Observable.create(new Observable.OnSubscribe<List<Location>>() {
            @Override
            public void call(Subscriber<? super List<Location>> subscriber) {
                try {
                    subscriber.onNext(mTwitter.getClosestTrends(location));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        }).flatMap(new Func1<List<Location>, Observable<List<Trend>>>() {
            @Override
            public Observable<List<Trend>> call(List<Location> locations) {
                return trends(locations.get(0).getWoeid());
            }
        });
    }
}
