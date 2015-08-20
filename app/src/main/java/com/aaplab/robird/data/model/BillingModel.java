package com.aaplab.robird.data.model;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.aaplab.robird.Config;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by majid on 20.08.15.
 */
public final class BillingModel implements BillingProcessor.IBillingHandler {

    private BillingProcessor mBillingProcessor;
    private PublishSubject<String> mProductSubject;
    private Activity mActivity;

    public BillingModel(Activity activity) {
        mBillingProcessor = new BillingProcessor(activity, Config.LICENSE_KEY, this);
        mProductSubject = PublishSubject.create();
        mActivity = activity;
    }

    public Observable<String> purchase(String productId) {
        mBillingProcessor.purchase(mActivity, productId);
        return mProductSubject;
    }

    public boolean isPurchased(String productId) {
        return mBillingProcessor.isPurchased(productId);
    }

    public boolean handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        return mBillingProcessor.handleActivityResult(requestCode, resultCode, data);
    }

    public void onDestroy() {
        if (mBillingProcessor != null)
            mBillingProcessor.release();
    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        mProductSubject.onNext(s);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int i, Throwable throwable) {
        mProductSubject.onError(throwable);
    }

    @Override
    public void onBillingInitialized() {

    }
}
