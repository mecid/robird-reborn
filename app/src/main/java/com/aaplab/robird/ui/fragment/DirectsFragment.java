package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Direct;
import com.aaplab.robird.data.model.DirectsModel;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.ui.adapter.DirectsAdapter;
import com.aaplab.robird.util.DefaultObserver;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by majid on 18.08.15.
 */
public class DirectsFragment extends BaseSwipeToRefreshRecyclerFragment {
    public static DirectsFragment create(Account account) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);

        DirectsFragment fragment = new DirectsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private DirectsModel mDirectsModel;
    private Account mAccount;
    private PrefsModel mPrefsModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = getArguments().getParcelable("account");
        mDirectsModel = new DirectsModel(mAccount);
        mPrefsModel = new PrefsModel();

        mSubscriptions.add(
                mDirectsModel
                        .directs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<Direct>>() {
                            @Override
                            public void onNext(List<Direct> directs) {
                                super.onNext(directs);
                                setupDirects(directs);
                            }
                        })
        );

        if (mPrefsModel.isStreamingEnabled(getActivity())) {
            if (savedInstanceState == null) {
                setRefreshing(true);
                onRefresh();
            } else {
                mRefreshLayout.setEnabled(false);
            }
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();

        mSubscriptions.add(
                mDirectsModel
                        .update()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<Integer>() {
                            @Override
                            public void onNext(Integer integer) {
                                super.onNext(integer);
                                setRefreshing(false);
                                mRefreshLayout.setEnabled(!mPrefsModel.isStreamingEnabled(getActivity()));
                            }
                        })
        );
    }

    private void setupDirects(List<Direct> directs) {
        HashMap<String, Direct> map = new HashMap<>();

        for (Direct direct : directs) {
            final String username = TextUtils.equals(direct.recipient(), mAccount.screenName()) ?
                    direct.userName() : direct.recipient();

            if (!map.containsKey(username))
                map.put(username, direct);
        }

        ArrayList<Direct> sortedDirects = new ArrayList<>(map.values());
        Collections.sort(sortedDirects, new Ordering<Direct>() {
            @Override
            public int compare(@Nullable Direct left, @Nullable Direct right) {
                return Long.valueOf(left.directId()).compareTo(right.directId());
            }
        });

        Collections.reverse(sortedDirects);

        mRecyclerView.setAdapter(new DirectsAdapter(getActivity(), mAccount, sortedDirects, true));
    }
}
