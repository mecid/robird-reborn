package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.aaplab.robird.inject.Inject;
import com.squareup.otto.Bus;

import icepick.Icepick;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by majid on 08.05.15.
 */
public abstract class BaseFragment extends Fragment {

    protected CompositeSubscription mSubscriptions;
    protected Bus mBus;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        mBus = Inject.eventBus();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSubscriptions.unsubscribe();
    }
}
