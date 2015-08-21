package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.SearchModel;
import com.aaplab.robird.ui.adapter.UserAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import icepick.Icicle;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.User;

/**
 * Created by majid on 10.08.15.
 */
public class UserSearchFragment extends BaseSwipeToRefreshRecyclerFragment {
    public static UserSearchFragment create(Account account, String query) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        args.putString("query", query);

        UserSearchFragment fragment = new UserSearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Icicle
    CopyOnWriteArrayList<User> mUsers;

    @Icicle
    int mPage = 1;

    private String mQuery;
    private Account mAccount;
    private UserAdapter mAdapter;
    private SearchModel mSearchModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = getArguments().getParcelable("account");
        mQuery = getArguments().getString("query");
        mSearchModel = new SearchModel(mAccount);
        mUsers = mUsers == null ? new CopyOnWriteArrayList<User>() : mUsers;
        mAdapter = new UserAdapter(getActivity(), mAccount, mUsers);
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState == null || mUsers.isEmpty()) {
            setRefreshing(true);
            mSubscriptions.add(
                    mSearchModel
                            .users(mQuery, mPage)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<List<User>>() {
                                @Override
                                public void onNext(List<User> users) {
                                    super.onNext(users);
                                    mUsers.addAll(users);
                                    mAdapter.notifyDataSetChanged();
                                    setRefreshing(false);
                                }
                            })
            );
        }
    }

    @Override
    public void onRefresh() {
        mPage = 1;

        mSubscriptions.add(
                mSearchModel
                        .users(mQuery, mPage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<User>>() {
                            @Override
                            public void onNext(List<User> users) {
                                super.onNext(users);
                                mUsers.clear();
                                mUsers.addAll(users);
                                mAdapter.notifyDataSetChanged();
                                setRefreshing(false);
                            }
                        })
        );
    }

    @Override
    public void startBottomLoading() {
        mPage++;

        mSubscriptions.add(
                mSearchModel
                        .users(mQuery, mPage)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<User>>() {
                            @Override
                            public void onNext(List<User> users) {
                                super.onNext(users);
                                mUsers.addAll(users);
                                mAdapter.notifyDataSetChanged();
                                stopBottoLoading(!users.isEmpty());
                            }
                        })
        );
    }
}
