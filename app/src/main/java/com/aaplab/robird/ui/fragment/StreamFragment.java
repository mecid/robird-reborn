package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.StreamModel;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by arazabishov on 2/14/16.
 */
public final class StreamFragment extends Fragment {
    private Subscription mSubscription = Subscriptions.empty();
    private StreamModel mStreamModel;

    public static StreamFragment create() {
        return new StreamFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        AccountModel accountModel = new AccountModel();

        // we have to fetch active user from database first
        mSubscription = accountModel.accounts()
                .map(new Func1<List<Account>, Account>() {
                    @Override
                    public Account call(List<Account> accounts) {
                        return accounts.get(0);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<Account>() {
                    @Override
                    public void onNext(Account account) {
                        // we have to shutdown stream
                        // for previous user
                        if (mStreamModel != null) {
                            mStreamModel.stop();
                        }

                        mStreamModel = new StreamModel(account);
                        mStreamModel.start();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // un-subscribing in order to prevent context leaks
        if (!mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        if (mStreamModel != null) {

            // shutting down streaming
            mStreamModel.stop();
        }
    }
}
