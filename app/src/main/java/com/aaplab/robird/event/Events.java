package com.aaplab.robird.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by majid on 21.01.15.
 */
public final class Events {
    public static class TweetChangeEvent {

        public static final int DELETED = 0;
        public static final int RETWEETED = 1;
        public static final int UNRETWEETED = 2;
        public static final int FAVORITED = 3;
        public static final int UNFAVORITED = 4;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({DELETED, RETWEETED, FAVORITED, UNRETWEETED, UNFAVORITED})
        public @interface Action {
        }

        public long tweetId;
        public int action;

        public TweetChangeEvent(long tweetId, @Action int action) {
            this.tweetId = tweetId;
            this.action = action;
        }
    }
}
