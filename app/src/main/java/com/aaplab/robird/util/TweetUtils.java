package com.aaplab.robird.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by majid on 01.06.15.
 */
public final class TweetUtils {
    public static String detectMedia(String url) {
        String result = null;
        Pattern pattern = Pattern.compile("http.://(d.pr/i|twitpic.com|instagram.com/p|yfrog.com|moby.to|sdrv.ms|instagr.am/p|img.ly)(.+|/$)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            String imgService = matcher.group(1);
            Pattern imgPtrn = Pattern.compile("http.://" + imgService + "/([_\\-\\d\\w]+)(|/$)");
            Matcher imgMatcher = imgPtrn.matcher(url);

            if (imgMatcher.matches()) {
                String imgCode = imgMatcher.group(1);

                if (imgService.equals("twitpic.com")) {
                    result = "http://twitpic.com/show/large/" + imgCode;
                } else if (imgService.equals("yfrog.com")) {
                    result = "http://yfrog.com/" + imgCode + ":medium";
                } else if (imgService.equals("instagr.am/p") || imgService.equals("instagram.com/p")) {
                    result = "http://instagr.am/p/" + imgCode + "/media/?size=l";
                } else if (imgService.equals("img.ly")) {
                    result = "http://img.ly/show/full/" + imgCode;
                } else if (imgService.equals("sdrv.ms")) {
                    result = "https://apis.live.net/v5.0/skydrive/get_item_preview?type=normal&url=" + "http://sdrv.ms/" + imgCode;
                } else if (imgService.equals("moby.to")) {
                    result = "http://moby.to/" + imgCode + ":view";
                } else if (imgService.equals("d.pr/i")) {
                    result = "http://d.pr/i/" + imgCode + "/medium";
                }
            }
        }

        if (TextUtils.isEmpty(result)) {
            String temp = url.toLowerCase();
            if (temp.endsWith(".png") || temp.endsWith(".jpg") || temp.endsWith(".jpeg"))
                result = url;
        }

        if (TextUtils.isEmpty(result)) {
            if (url.contains("lockerz.com/")
                    || url.contains("plixi.com/")) {
                result = "http://api.plixi.com/api/tpapi.svc/imagefromurl?url="
                        + url + "&size=big";
            }
        }

        return result;
    }

    public static String getSourceName(String source) {
        return source.replaceAll("\\<.*?>", "");
    }
}
