package com.aaplab.robird.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.aaplab.robird.R;
import com.aaplab.robird.data.model.BillingModel;
import com.aaplab.robird.util.DefaultObserver;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by majid on 28.08.15.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {

    private BillingModel mBillingModel;
    private CompositeSubscription mSubscriptions;

    private Preference mUnlockAllPreference;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        mBillingModel = new BillingModel(getActivity());

        mUnlockAllPreference = findPreference("unlock_all");
        mUnlockAllPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mUnlockAllPreference == preference) {
            if (mBillingModel.isPurchased(BillingModel.UNLOCK_ALL_PRODUCT_ID)) {
                Snackbar.make(getActivity().findViewById(R.id.coordinator),
                        R.string.already_purchased, Snackbar.LENGTH_SHORT).show();
            } else {
                mSubscriptions.add(
                        mBillingModel
                                .purchase(BillingModel.UNLOCK_ALL_PRODUCT_ID)
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new DefaultObserver<String>() {
                                    @Override
                                    public void onNext(String s) {
                                        super.onNext(s);
                                        if (TextUtils.equals(s, BillingModel.UNLOCK_ALL_PRODUCT_ID))
                                            Snackbar.make(getActivity().findViewById(R.id.coordinator),
                                                    R.string.purchased, Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                );
            }
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingModel.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        mSubscriptions.unsubscribe();
        mBillingModel.onDestroy();
        super.onDestroyView();
    }
}