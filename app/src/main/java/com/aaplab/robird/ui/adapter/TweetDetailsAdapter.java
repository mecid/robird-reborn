package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.google.common.collect.Lists;

import java.util.List;

import butterknife.ButterKnife;
import twitter4j.Status;

/**
 * Created by majid on 30.06.15.
 */
public class TweetDetailsAdapter extends TweetAdapter {

    public static final int TWEET_DETAILS_TYPE = 1;
    public static final int TWEET_ITEM_TYPE = 0;

    private Status mDetailedStatus;

    public TweetDetailsAdapter(Activity activity, Account account, Tweet tweet) {
        super(activity, account, Lists.newArrayList(tweet));
    }

    @Override
    public TweetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TWEET_DETAILS_TYPE)
            return new TweetDetailsHolder(LayoutInflater.from(mActivity).inflate(R.layout.tweet_details_item, parent, false));
        else
            return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(TweetHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (getItemViewType(position) == TWEET_DETAILS_TYPE) {
            TweetDetailsHolder tweetDetailsHolder = (TweetDetailsHolder) holder;

            if (mDetailedStatus != null) {
                tweetDetailsHolder.favoritesCountTextView.setText(mDetailedStatus.getFavoriteCount() + " ");
                tweetDetailsHolder.retweetsCountTextView.setText(mDetailedStatus.getRetweetCount() + " ");
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TWEET_DETAILS_TYPE : TWEET_ITEM_TYPE;
    }

    public void showDetails(Status status) {
        mDetailedStatus = status;
        notifyDataSetChanged();
    }

    public void showConversation(List<Tweet> tweets) {
        mTweets.addAll(tweets);
        notifyDataSetChanged();
    }

    static final class TweetDetailsHolder extends TweetAdapter.TweetHolder {

        TextView favoritesCountTextView;
        TextView retweetsCountTextView;

        public TweetDetailsHolder(View itemView) {
            super(itemView);

            favoritesCountTextView = ButterKnife.findById(itemView, R.id.favorite_count);
            retweetsCountTextView = ButterKnife.findById(itemView, R.id.retweet_count);
        }
    }
}
