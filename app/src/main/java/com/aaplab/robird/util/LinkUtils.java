package com.aaplab.robird.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.model.PrefsModel;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by user on 20.03.14.
 */
public class LinkUtils {

    public static final String MENTION_SCHEME = "robird://profile?username=";
    public static final String HASHTAG_SCHEME = "robird://hashtag?name=";

    public static void activate(Activity activity, TextView textView) {

        Linkify.addLinks(textView, Linkify.WEB_URLS);
        Linkify.addLinks(textView, Pattern.compile("@([A-Za-z0-9_-]+)"), MENTION_SCHEME);
        Linkify.addLinks(textView, Pattern.compile("#([A-Za-z0-9_-]+)"), HASHTAG_SCHEME);

        // Replace default URL spans with custom spans,
        // which use Custom Tabs for web links handling,
        // if In-App browser enabled in the settings.
        addCustomTabUrlHandler(activity, textView);
    }

    private static void addCustomTabUrlHandler(Activity activity, TextView textView) {
        CharSequence text = textView.getText();
        Spannable s = new SpannableString(text);
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new CustomTabUrlSpan(activity, span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private static boolean canHandleByApp(Activity activity, String url) {
        final PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(new Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolveInfos) {
            if (TextUtils.equals(resolveInfo.resolvePackageName, activity.getPackageName()))
                return true;
        }

        return false;
    }

    private static class CustomTabUrlSpan extends URLSpan {
        private PrefsModel mPrefsModel;
        private Activity mActivity;

        public CustomTabUrlSpan(Activity activity, String url) {
            super(url);
            mActivity = activity;
            mPrefsModel = new PrefsModel();
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (mPrefsModel.isInAppBrowserEnabled() && !canHandleByApp(mActivity, getURL())) {
                new CustomTabsIntent.Builder()
                        .setToolbarColor(mActivity.getResources().getColor(R.color.primary))
                        .build().launchUrl(mActivity, Uri.parse(getURL()));
            } else {
                super.onClick(widget);
            }
        }
    }
}