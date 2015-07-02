package com.aaplab.robird.util;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.regex.Pattern;

/**
 * Created by user on 20.03.14.
 */
public class LinkifyUtils {

    public static final String MENTION_SCHEME = "robird://profile?username=";
    public static final String HASHTAG_SCHEME = "robird://hashtag?name=";

    public static void linkifyTextView(TextView textView, boolean clickable) {

        Linkify.addLinks(textView, Linkify.WEB_URLS);
        Linkify.addLinks(textView, Pattern.compile("@([A-Za-z0-9_-]+)"), MENTION_SCHEME);
        Linkify.addLinks(textView, Pattern.compile("#([A-Za-z0-9_-]+)"), HASHTAG_SCHEME);

        stripUnderlines(textView);

        if (!clickable)
            textView.setMovementMethod(null);
    }

    private static void stripUnderlines(TextView textView) {
        CharSequence text = textView.getText();
        Spannable s = new SpannableString(text);
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private static class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}