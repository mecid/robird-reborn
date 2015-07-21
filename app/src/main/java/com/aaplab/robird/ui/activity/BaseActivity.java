package com.aaplab.robird.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.aaplab.robird.inject.Inject;
import com.squareup.otto.Bus;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by majid on 07.05.15.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected CompositeSubscription mSubscriptions;
    protected Bus mBus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        mBus = Inject.eventBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    public boolean onSupportNavigateUp() {
        ActivityCompat.finishAfterTransition(this);
        return true;
    }
}
