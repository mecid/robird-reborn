package com.aaplab.robird.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.aaplab.robird.R;
import com.aaplab.robird.data.model.BillingModel;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.util.DefaultObserver;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by majid on 28.08.15.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private CompositeSubscription mSubscriptions;
    private BillingModel mBillingModel;

    private Preference mThemePreference;
    private Preference mUnlockAllPreference;
    private Preference mUnlockUiPreference;
    private ListPreference mTimelineFontSizePreference;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final PrefsModel prefsModel = new PrefsModel();
        mSubscriptions = new CompositeSubscription();
        mBillingModel = new BillingModel(getActivity());

        mThemePreference = findPreference(PrefsModel.PREFER_DARK_THEME);
        mThemePreference.setOnPreferenceChangeListener(this);

        mTimelineFontSizePreference = (ListPreference) findPreference(PrefsModel.TIMELINE_FONT_SIZE);
        mTimelineFontSizePreference.setOnPreferenceChangeListener(this);
        mTimelineFontSizePreference.setSummary("" + prefsModel.fontSize());

        mUnlockAllPreference = findPreference("unlock_all_settings");
        mUnlockAllPreference.setOnPreferenceClickListener(this);

        mUnlockUiPreference = findPreference("unlock_ui_settings");
        mUnlockUiPreference.setOnPreferenceClickListener(this);

        enablePurchasedSettings();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mUnlockAllPreference == preference) {
            unlock(BillingModel.UNLOCK_ALL_PRODUCT_ID);
        } else if (mUnlockUiPreference == preference) {
            unlock(BillingModel.UNLOCK_UI_PRODUCT_ID);
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == mTimelineFontSizePreference) {
            preference.setSummary((String) o);
        } else if (preference == mThemePreference) {
            getActivity().recreate();
        }

        return true;
    }

    private void enablePurchasedSettings() {
        mThemePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mTimelineFontSizePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
    }

    private void unlock(final String productId) {
        if (mBillingModel.isPurchased(productId)) {
            Snackbar.make(getActivity().findViewById(R.id.coordinator),
                    R.string.already_purchased, Snackbar.LENGTH_SHORT).show();
        } else {
            mSubscriptions.add(
                    mBillingModel
                            .purchase(productId)
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<String>() {
                                @Override
                                public void onNext(String s) {
                                    super.onNext(s);
                                    enablePurchasedSettings();
                                    if (TextUtils.equals(s, productId))
                                        Snackbar.make(getActivity().findViewById(R.id.coordinator),
                                                R.string.purchased, Snackbar.LENGTH_SHORT).show();
                                }
                            })
            );
        }
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