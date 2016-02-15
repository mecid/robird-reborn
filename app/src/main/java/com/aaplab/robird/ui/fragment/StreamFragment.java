package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.StreamModel;
import com.aaplab.robird.util.DefaultObserver;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by arazabishov on 2/14/16.
 */
public final class StreamFragment extends Fragment {
    private List<StreamModel> mStreamModels;
    private Subscription mSubscription;

    public static StreamFragment create() {
        return new StreamFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        AccountModel accountModel = new AccountModel();

        mStreamModels = new ArrayList<>();
        mSubscription = accountModel.accounts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(1)
                .subscribe(new DefaultObserver<List<Account>>() {
                    @Override
                    public void onNext(List<Account> accounts) {
                        for (Account account : accounts) {
                            StreamModel streamModel = new StreamModel(account);
                            streamModel.start();

                            mStreamModels.add(streamModel);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // un-subscribing in order to prevent context leaks
        if (!mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = Subscriptions.empty();
        }

        // Shutting down streaming
        for (StreamModel streamModel : mStreamModels) {
            streamModel.stop();
        }
    }
}
