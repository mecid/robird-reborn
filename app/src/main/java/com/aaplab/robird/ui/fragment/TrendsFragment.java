package com.aaplab.robird.ui.fragment;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.TrendsModel;
import com.aaplab.robird.ui.adapter.TrendAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.ArrayList;
import java.util.List;

import icepick.Icicle;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import twitter4j.GeoLocation;
import twitter4j.Trend;

/**
 * Created by majid on 25.08.15.
 */
public class TrendsFragment extends BaseSwipeToRefreshRecyclerFragment {
    public static TrendsFragment create(Account account, int woeid) {
        final Bundle args = new Bundle();
        args.putParcelable("account", account);
        args.putInt("woeid", woeid);

        TrendsFragment fragment = new TrendsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Icicle
    ArrayList<Trend> mTrends;

    private LocationManager mLocationManager;
    private TrendsModel mTrendsModel;
    private Account mAccount;
    private int mWoeid;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mTrends = mTrends == null ? new ArrayList<Trend>() : mTrends;
        mAccount = getArguments().getParcelable("account");
        mWoeid = getArguments().getInt("woeid");
        mTrendsModel = new TrendsModel(mAccount);

        if (savedInstanceState == null || mTrends.isEmpty()) {
            setRefreshing(true);
            mSubscriptions.add(
                    location()
                            .flatMap(new Func1<GeoLocation, Observable<List<Trend>>>() {
                                @Override
                                public Observable<List<Trend>> call(GeoLocation location) {
                                    return mWoeid > 0 ?
                                            mTrendsModel.trends(mWoeid) :
                                            mTrendsModel.local(location);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<List<Trend>>() {
                                @Override
                                public void onNext(List<Trend> trends) {
                                    super.onNext(trends);
                                    setRefreshing(false);
                                    setupTrends(new ArrayList<>(trends));
                                }
                            })
            );
        } else {
            setupTrends(mTrends);
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mSubscriptions.add(
                location()
                        .flatMap(new Func1<GeoLocation, Observable<List<Trend>>>() {
                            @Override
                            public Observable<List<Trend>> call(GeoLocation location) {
                                return mWoeid > 0 ?
                                        mTrendsModel.trends(mWoeid) :
                                        mTrendsModel.local(location);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<Trend>>() {
                            @Override
                            public void onNext(List<Trend> trends) {
                                super.onNext(trends);
                                setRefreshing(false);
                                setupTrends(new ArrayList<>(trends));
                            }
                        })
        );
    }

    private void setupTrends(ArrayList<Trend> trends) {
        mTrends = trends;
        mRecyclerView.setAdapter(new TrendAdapter(getActivity(), mAccount, mTrends));
    }

    private Observable<GeoLocation> location() {
        return Observable.create(new Observable.OnSubscribe<GeoLocation>() {
            @Override
            public void call(Subscriber<? super GeoLocation> subscriber) {
                Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                subscriber.onNext(new GeoLocation(location.getLatitude(), location.getLongitude()));
                subscriber.onCompleted();
            }
        });
    }
}
