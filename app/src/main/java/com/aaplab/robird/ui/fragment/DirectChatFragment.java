package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Direct;
import com.aaplab.robird.data.model.DirectsModel;
import com.aaplab.robird.ui.adapter.DirectsAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by majid on 19.08.15.
 */
public class DirectChatFragment extends BaseSwipeToRefreshRecyclerFragment {
    public static DirectChatFragment create(Account account, String username) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("account", account);
        bundle.putString("username", username);

        DirectChatFragment fragment = new DirectChatFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private DirectsModel mDirectsModel;
    private Account mAccount;
    private String mUserName;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = getArguments().getParcelable("account");
        mUserName = getArguments().getString("username");
        mDirectsModel = new DirectsModel(mAccount);

        mSubscriptions.add(
                mDirectsModel
                        .directs(mUserName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<Direct>>() {
                            @Override
                            public void onNext(List<Direct> directs) {
                                super.onNext(directs);
                                mRecyclerView.setAdapter(new DirectsAdapter(getActivity(), mAccount, directs, false));
                                mLayoutManager.scrollToPosition(directs.size() - 1);
                            }
                        })
        );
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
                            }
                        })
        );
    }
}
