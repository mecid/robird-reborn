package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.StreamModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.util.DefaultObserver;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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
                .flatMap(new Func1<List<Account>, Observable<Account>>() {
                    @Override
                    public Observable<Account> call(List<Account> accounts) {
                        return Observable.from(accounts);
                    }
                })
                .map(new Func1<Account, StreamModel>() {
                    @Override
                    public StreamModel call(Account account) {
                        (new TimelineModel(account, TimelineModel.HOME_ID)).update().toBlocking();
                        (new TimelineModel(account, TimelineModel.MENTIONS_ID)).update().toBlocking();
                        (new TimelineModel(account, TimelineModel.RETWEETS_ID)).update().toBlocking();
                        (new TimelineModel(account, TimelineModel.FAVORITES_ID)).update().toBlocking();

                        return new StreamModel(account);
                    }
                })
                .subscribe(new DefaultObserver<StreamModel>() {
                    @Override
                    public void onNext(StreamModel streamModel) {
                        mStreamModels.add(streamModel);
                        streamModel.start();
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
