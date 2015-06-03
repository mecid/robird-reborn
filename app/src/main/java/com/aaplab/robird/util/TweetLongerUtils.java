package com.aaplab.robird.util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by majid on 01.06.15.
 */
public final class TweetLongerUtils {
    public static String detectTwitLonger(String url) {
        Pattern pattern = Pattern.compile("http://tl.gd/(.+[a-zA-Z0-9])(|/$)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            return readTweetLongedText(matcher.group(1));
        }

        return null;
    }

    private static String readTweetLongedText(String id) {
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new BufferedInputStream(
                    new URL("http://www.twitlonger.com/api_read/" + id).openStream()));
            while (scanner.hasNext())
                sb.append(scanner.nextLine());
            scanner.close();
            return new TwitLongerResponse(sb.toString()).content;
        } catch (IOException e) {
            Timber.w("", e);
        }

        return sb.toString();
    }

    private static class TwitLongerResponse {
        private String content;
        private String error;
        private String id;
        private String link;
        private String shortLink;

        public TwitLongerResponse(String source) {
            this.content = getTag("content", source);
            this.error = getTag("error", source);
            this.id = getTag("id", source);
            this.link = getTag("link", source);
            this.shortLink = getTag("short", source);
        }

        private String getTag(String tag, String source) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xmlPullParser = factory.newPullParser();
                xmlPullParser.setInput(new StringReader(source));

                xmlPullParser.next();
                int eventType = xmlPullParser.getEventType();

                String content = null;
                boolean shouldRead = false;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xmlPullParser.getName().equals(tag)) {
                                shouldRead = true;
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (shouldRead) {
                                content = xmlPullParser.getText();
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            shouldRead = false;
                            break;
                    }
                    eventType = xmlPullParser.next();
                }
                return content;
            } catch (IOException | XmlPullParserException e) {
                Timber.w("", e);
            }
            return null;
        }
    }
}
