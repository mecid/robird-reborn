package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.data.model.UserListsModel;
import com.aaplab.robird.ui.adapter.ListAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by majid on 17.08.15.
 */
public class UserListsFragment extends BaseSwipeToRefreshRecyclerFragment {
    public static UserListsFragment create(Account account) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);

        UserListsFragment fragment = new UserListsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private UserListsModel mUserListsModel;
    private Account mAccount;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = getArguments().getParcelable("account");
        mUserListsModel = new UserListsModel(mAccount);

        mSubscriptions.add(
                mUserListsModel
                        .lists()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<List<UserList>>() {
                            @Override
                            public void onNext(List<UserList> userLists) {
                                super.onNext(userLists);
                                mRecyclerView.setAdapter(new ListAdapter(getActivity(), mAccount, userLists));
                            }
                        })
        );
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mSubscriptions.add(
                mUserListsModel
                        .update()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<Integer>() {
                            @Override
                            public void onNext(Integer integer) {
                                super.onNext(integer);
                                mRefreshLayout.setRefreshing(false);
                            }
                        })
        );
    }
}
