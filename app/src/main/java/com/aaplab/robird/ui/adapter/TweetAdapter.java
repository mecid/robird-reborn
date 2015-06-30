package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.ui.activity.TweetDetailsActivity;
import com.aaplab.robird.util.RoundTransformation;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by majid on 19.01.15.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetHolder> {
    public static final String IMAGE_LOADING_TAG = "tweet";

    protected List<Tweet> mTweets;
    protected Activity mActivity;
    protected Account mAccount;

    public TweetAdapter(Activity activity, Account account, List<Tweet> tweets) {
        super();
        mActivity = activity;
        mAccount = account;
        mTweets = tweets;
    }

    @Override
    public TweetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TweetHolder(LayoutInflater.from(mActivity).inflate(R.layout.tweet_item, parent, false));
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    @Override
    public void onBindViewHolder(TweetHolder holder, int position) {
        final Tweet tweet = mTweets.get(position);

        holder.textView.setText(tweet.text);
        holder.usernameTextView.setText("@" + tweet.username);
        holder.fullNameTextView.setText(tweet.fullname);

        StringBuilder information = new StringBuilder();

        if (!TextUtils.isEmpty(tweet.retweetedBy))
            information.append(tweet.retweetedBy).append(" ");

        information.append(DateUtils.getRelativeTimeSpanString(tweet.createdAt));
        information.append(" ").append(tweet.source);

        holder.infoTextView.setText(information.toString());
        holder.retweetImageView.setVisibility(TextUtils.isEmpty(tweet.retweetedBy) ? View.GONE : View.VISIBLE);
        holder.mediaImageView.setVisibility(TextUtils.isEmpty(tweet.media) ? View.GONE : View.VISIBLE);

        if (holder.mediaImageView.getVisibility() == View.VISIBLE) {
            String[] media = tweet.media.split("\\+\\+\\+");

            Picasso.with(mActivity)
                    .load(media[0])
                    .tag(IMAGE_LOADING_TAG)
                    .centerCrop().fit()
                    .into(holder.mediaImageView);
        }

        Picasso.with(mActivity)
                .load(tweet.avatar)
                .tag(IMAGE_LOADING_TAG)
                .centerCrop().fit()
                .transform(new RoundTransformation())
                .into(holder.avatarImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TweetDetailsActivity.start(mActivity, mAccount, tweet);
            }
        });
    }

    static class TweetHolder extends RecyclerView.ViewHolder {

        ImageView avatarImageView;
        TextView usernameTextView;
        TextView fullNameTextView;
        ImageView mediaImageView;
        ImageView retweetImageView;
        TextView infoTextView;
        TextView textView;

        public TweetHolder(View itemView) {
            super(itemView);

            avatarImageView = ButterKnife.findById(itemView, R.id.avatar);
            usernameTextView = ButterKnife.findById(itemView, R.id.screen_name);
            fullNameTextView = ButterKnife.findById(itemView, R.id.full_name);
            mediaImageView = ButterKnife.findById(itemView, R.id.media);
            retweetImageView = ButterKnife.findById(itemView, R.id.retweet);
            infoTextView = ButterKnife.findById(itemView, R.id.info);
            textView = ButterKnife.findById(itemView, R.id.text);
        }
    }
}
