package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.UserModel;
import com.aaplab.robird.ui.activity.UserProfileActivity;
import com.aaplab.robird.ui.adapter.UserAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.concurrent.CopyOnWriteArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.PagableResponseList;
import twitter4j.User;

/**
 * Created by majid on 20.07.15.
 */
public class UserFriendsFragment extends BaseSwipeToRefreshRecyclerFragment {
    public static UserFriendsFragment create(Account account,
                                             String screenName,
                                             @UserModel.FriendsType int type) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("account", account);
        bundle.putString(UserProfileActivity.SCREEN_NAME, screenName);
        bundle.putInt("type", type);

        UserFriendsFragment fragment = new UserFriendsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private UserModel mUserModel;
    private String mScreenName;
    private int mType;

    private CopyOnWriteArrayList<User> mUsers;
    private UserAdapter mAdapter;
    private long mCursor;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Account account = getArguments().getParcelable("account");
        mScreenName = getArguments().getString(UserProfileActivity.SCREEN_NAME);
        mType = getArguments().getInt("type");
        mUsers = new CopyOnWriteArrayList<>();
        mAdapter = new UserAdapter(getActivity(), account, mUsers);
        mRecyclerView.setAdapter(mAdapter);

        mUserModel = new UserModel(account, mScreenName);
        mSubscriptions.add(
                mUserModel
                        .friends(mType, -1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<PagableResponseList<User>>() {
                            @Override
                            public void onNext(PagableResponseList<User> users) {
                                super.onNext(users);
                                mUsers.addAll(users);
                                mAdapter.notifyDataSetChanged();
                                mCursor = users.getNextCursor();
                            }
                        })
        );
    }

    @Override
    public void onRefresh() {
        mSubscriptions.add(
                mUserModel
                        .friends(mType, -1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<PagableResponseList<User>>() {
                            @Override
                            public void onNext(PagableResponseList<User> users) {
                                super.onNext(users);
                                mUsers.clear();
                                mUsers.addAll(users);
                                mAdapter.notifyDataSetChanged();
                                mCursor = users.getNextCursor();
                            }
                        })
        );
    }

    @Override
    public void startBottomLoading() {
        mSubscriptions.add(
                mUserModel
                        .friends(mType, mCursor)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<PagableResponseList<User>>() {
                            @Override
                            public void onNext(PagableResponseList<User> users) {
                                super.onNext(users);
                                mUsers.addAll(users);
                                mAdapter.notifyDataSetChanged();
                                mCursor = users.getNextCursor();
                                stopBottoLoading(mCursor != 0);
                            }
                        })
        );
    }
}
