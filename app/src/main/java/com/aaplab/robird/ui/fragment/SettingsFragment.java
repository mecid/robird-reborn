package com.aaplab.robird.ui.fragment;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.aaplab.robird.R;
import com.aaplab.robird.TimelineUpdateService;
import com.aaplab.robird.data.model.BillingModel;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.util.DefaultObserver;
import com.google.android.gms.gcm.GcmNetworkManager;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by majid on 28.08.15.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final int REQUEST_CODE_RINGTONE = 4324;

    private CompositeSubscription mSubscriptions;
    private BillingModel mBillingModel;
    private PrefsModel mPrefsModel;

    private Preference mThemePreference;
    private Preference mUnlockAllPreference;
    private Preference mUnlockUiPreference;
    private Preference mHideMediaPreference;
    private Preference mShowMediaPreviewPreference;
    private Preference mShowAbsoluteTimePreference;
    private Preference mShowClientNameInTimelinePreference;
    private Preference mCompactTimelinePreference;
    private Preference mHideAvatarsPreference;
    private Preference mUseInAppBrowserPreference;
    private Preference mUseMobileViewInAppBrowserPreference;
    private Preference mTimelineFontSizePreference;
    private Preference mUnlockInAppBrowserPreference;
    private Preference mHighlightTimelineLinksPreference;
    private Preference mBackgroundUpdatePreference;
    private Preference mBackgroundUpdateIntervalPreference;
    private Preference mUnlockOtherPreference;
    private Preference mRestorePreference;
    private Preference mNotificationRingtonePreference;
    private Preference mTwitterStreamingPreference;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        mBillingModel = new BillingModel(getActivity());
        mPrefsModel = new PrefsModel();

        mUnlockInAppBrowserPreference = findPreference("unlock_browser");
        mUnlockInAppBrowserPreference.setOnPreferenceClickListener(this);

        mUseInAppBrowserPreference = findPreference(PrefsModel.USE_IN_APP_BROWSER);
        mUseMobileViewInAppBrowserPreference = findPreference(PrefsModel.USE_MOBILE_VIEW_BROWSER);

        mShowAbsoluteTimePreference = findPreference(PrefsModel.SHOW_ABSOLUTE_TIME);
        mShowAbsoluteTimePreference.setOnPreferenceChangeListener(this);

        mCompactTimelinePreference = findPreference(PrefsModel.COMPACT_TIMELINE);
        mCompactTimelinePreference.setOnPreferenceChangeListener(this);

        mShowClientNameInTimelinePreference = findPreference(PrefsModel.SHOW_CLIENT_NAME_IN_TIMELINE);
        mShowClientNameInTimelinePreference.setOnPreferenceChangeListener(this);

        mHideAvatarsPreference = findPreference(PrefsModel.HIDE_AVATARS);
        mHideAvatarsPreference.setOnPreferenceChangeListener(this);

        mShowMediaPreviewPreference = findPreference(PrefsModel.MEDIA_PREVIEW);
        mShowMediaPreviewPreference.setOnPreferenceChangeListener(this);

        mHideMediaPreference = findPreference(PrefsModel.HIDE_MEDIA);
        mHideMediaPreference.setOnPreferenceChangeListener(this);

        mThemePreference = findPreference(PrefsModel.PREFER_DARK_THEME);
        mThemePreference.setOnPreferenceChangeListener(this);

        mTimelineFontSizePreference = findPreference(PrefsModel.TIMELINE_FONT_SIZE);
        mTimelineFontSizePreference.setOnPreferenceChangeListener(this);
        mTimelineFontSizePreference.setSummary("" + mPrefsModel.fontSize());

        mUnlockAllPreference = findPreference("unlock_all_settings");
        mUnlockAllPreference.setOnPreferenceClickListener(this);

        mUnlockUiPreference = findPreference("unlock_ui_settings");
        mUnlockUiPreference.setOnPreferenceClickListener(this);

        mHighlightTimelineLinksPreference = findPreference(PrefsModel.HIGHLIGHT_TIMELINE_LINKS);
        mHighlightTimelineLinksPreference.setOnPreferenceChangeListener(this);

        mUnlockOtherPreference = findPreference("unlock_other_settings");
        mUnlockOtherPreference.setOnPreferenceClickListener(this);

        mBackgroundUpdateIntervalPreference = findPreference(PrefsModel.BACKGROUND_UPDATE_INTERVAL);
        mBackgroundUpdateIntervalPreference.setOnPreferenceChangeListener(this);

        mBackgroundUpdatePreference = findPreference(PrefsModel.BACKGROUND_UPDATE_SERVICE);
        mBackgroundUpdatePreference.setOnPreferenceChangeListener(this);

        mRestorePreference = findPreference("restore");
        mRestorePreference.setOnPreferenceClickListener(this);

        mNotificationRingtonePreference = findPreference(PrefsModel.NOTIFICATION_RINGTONE);
        mNotificationRingtonePreference.setOnPreferenceClickListener(this);

        mTwitterStreamingPreference = findPreference(PrefsModel.TWITTER_STREAMING);
        mTwitterStreamingPreference.setOnPreferenceChangeListener(this);

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
        } else if (mUnlockInAppBrowserPreference == preference) {
            unlock(BillingModel.UNLOCK_IN_APP_BROWSER);
        } else if (mUnlockOtherPreference == preference) {
            unlock(BillingModel.UNLOCK_OTHER_PRODUCT_ID);
        } else if (mRestorePreference == preference) {
            if (mBillingModel.restorePurchaseHistory()) {
                enablePurchasedSettings();
                Snackbar.make(getActivity().findViewById(R.id.coordinator),
                        R.string.purchase_history_restored, Snackbar.LENGTH_SHORT).show();
            }
        } else if (mNotificationRingtonePreference == preference) {
            pickNotificationRingtone();
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        // UI or Streaming preference changed
        if (preference == mThemePreference || preference == mHighlightTimelineLinksPreference ||
                preference == mShowClientNameInTimelinePreference || preference == mTimelineFontSizePreference ||
                preference == mHideMediaPreference || preference == mHideAvatarsPreference ||
                preference == mShowAbsoluteTimePreference || preference == mCompactTimelinePreference ||
                preference == mShowMediaPreviewPreference || preference == mTwitterStreamingPreference) {

            Snackbar.make(getActivity().findViewById(R.id.coordinator),
                    R.string.need_app_restart, Snackbar.LENGTH_INDEFINITE).show();
        }

        if (preference == mTimelineFontSizePreference) {
            preference.setSummary((String) o);
        } else if (preference == mBackgroundUpdatePreference ||
                preference == mBackgroundUpdateIntervalPreference) {
            GcmNetworkManager.getInstance(getActivity())
                    .schedule(TimelineUpdateService.create(mPrefsModel.backgroundUpdateInterval() / 1000));
        }

        return true;
    }

    private void enablePurchasedSettings() {
        mUnlockUiPreference.setEnabled(!mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mUnlockAllPreference.setEnabled(!mBillingModel.isPurchased(BillingModel.UNLOCK_ALL_PRODUCT_ID));
        mUnlockInAppBrowserPreference.setEnabled(!mBillingModel.isPurchased(BillingModel.UNLOCK_IN_APP_BROWSER));

        if (mUnlockOtherPreference.isVisible())
            mUnlockOtherPreference.setVisible(false);

        mThemePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mShowMediaPreviewPreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mCompactTimelinePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mShowAbsoluteTimePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mShowClientNameInTimelinePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mHighlightTimelineLinksPreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mTimelineFontSizePreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mHideAvatarsPreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mHideMediaPreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_UI_PRODUCT_ID));
        mUseInAppBrowserPreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_IN_APP_BROWSER));
        mUseMobileViewInAppBrowserPreference.setEnabled(mBillingModel.isPurchased(BillingModel.UNLOCK_IN_APP_BROWSER));
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

    private void pickNotificationRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

        String existingValue = mPrefsModel.notificationSound();
        if (TextUtils.isEmpty(existingValue)) {
            // Select "Silent"
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        } else {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
        }

        startActivityForResult(intent, REQUEST_CODE_RINGTONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingModel.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RINGTONE && data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            mPrefsModel.setNotificationSound(ringtone != null ? ringtone.toString() : "");
        }
    }

    @Override
    public void onDestroyView() {
        mSubscriptions.unsubscribe();
        mBillingModel.onDestroy();
        super.onDestroyView();
    }
}