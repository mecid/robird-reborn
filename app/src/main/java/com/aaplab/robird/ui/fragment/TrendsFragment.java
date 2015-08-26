package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.TrendsModel;
import com.aaplab.robird.ui.adapter.TrendAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.ArrayList;
import java.util.List;

import icepick.Icicle;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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

    private TrendsModel mTrendsModel;
    private Account mAccount;
    private int mWoeid;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTrends = mTrends == null ? new ArrayList<Trend>() : mTrends;
        mAccount = getArguments().getParcelable("account");
        mWoeid = getArguments().getInt("woeid");
        mTrendsModel = new TrendsModel(mAccount);

        if (savedInstanceState == null || mTrends.isEmpty()) {
            setRefreshing(true);
            mSubscriptions.add(
                    mTrendsModel
                            .trends(mWoeid)
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
                mTrendsModel
                        .trends(mWoeid)
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
}
