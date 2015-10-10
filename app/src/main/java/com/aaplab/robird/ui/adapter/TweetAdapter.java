package com.aaplab.robird.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.ui.activity.ImagesActivity;
import com.aaplab.robird.ui.activity.TweetDetailsActivity;
import com.aaplab.robird.ui.activity.UserProfileActivity;
import com.aaplab.robird.util.LinkUtils;
import com.aaplab.robird.util.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 19.01.15.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetHolder> {
    protected final PrefsModel mPrefsModel = new PrefsModel();

    protected List<Tweet> mTweets;
    protected Activity mActivity;
    protected Account mAccount;

    protected boolean mAbsoluteTime;
    protected boolean mShowClientName;
    protected boolean mIsMediaHidden;
    protected boolean mIsAvatarHidden;
    protected boolean mHighlightLinks;
    protected int mFontSize;

    public TweetAdapter(Activity activity, Account account, List<Tweet> tweets) {
        mActivity = activity;
        mAccount = account;
        mTweets = tweets;
        readPrefs();
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
        Glide.clear(holder.mediaImageView);
        bindFonts(holder, position);

        holder.textView.setText(tweet.text());
        holder.usernameTextView.setText("@" + tweet.username());
        holder.fullNameTextView.setText(tweet.fullname());

        StringBuilder information = new StringBuilder();

        if (!TextUtils.isEmpty(tweet.retweetedBy()))
            information.append(tweet.retweetedBy()).append(" ");

        if (mAbsoluteTime)
            information.append(DateUtils.formatDateTime(mActivity, tweet.createdAt(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));
        else
            information.append(DateUtils.getRelativeTimeSpanString(tweet.createdAt()));

        if (mShowClientName)
            information.append(" via ").append(tweet.source());

        holder.infoTextView.setText(information.toString());
        holder.retweetImageView.setVisibility(TextUtils.isEmpty(tweet.retweetedBy()) ? View.GONE : View.VISIBLE);

        if (mHighlightLinks) {
            LinkUtils.activate(mActivity, holder.textView);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TweetDetailsActivity.start(mActivity, mAccount, tweet);
                }
            });
        }

        if (!TextUtils.isEmpty(tweet.media()) && !mIsMediaHidden) {
            final String[] media = tweet.media().split("\\+\\+\\+\\+\\+");

            holder.mediaCountTextView.setVisibility(media.length > 1 ? View.VISIBLE : View.GONE);
            holder.mediaCountTextView.setText("" + media.length);
            holder.mediaImageView.setVisibility(View.VISIBLE);

            Glide.with(mActivity)
                    .load(media[0])
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.mediaImageView);

            holder.mediaImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImagesActivity.start(mActivity, media);
                }
            });
        } else {
            holder.mediaCountTextView.setVisibility(View.GONE);
            holder.mediaImageView.setVisibility(View.GONE);
        }

        if (!mIsAvatarHidden) {
            holder.avatarImageView.setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(tweet.avatar())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setVisibility(View.GONE);
        }

        holder.avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.start(mActivity, mAccount, tweet.username());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TweetDetailsActivity.start(mActivity, mAccount, tweet);
            }
        });
    }

    protected void bindFonts(TweetHolder holder, int position) {
        holder.usernameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSize);
        holder.fullNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSize);
        holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSize);
    }

    protected void readPrefs() {
        mFontSize = mPrefsModel.fontSize();
        mAbsoluteTime = mPrefsModel.showAbsoluteTime();
        mHighlightLinks = mPrefsModel.highlightTimelineLinks();
        mShowClientName = mPrefsModel.showClientNameInTimeline();
        mIsAvatarHidden = mPrefsModel.hideAvatarOnMobileConnection() && NetworkUtils.isMobile(mActivity);
        mIsMediaHidden = mPrefsModel.hideMediaOnMobileConnection() && NetworkUtils.isMobile(mActivity);
    }

    static class TweetHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.avatar)
        ImageView avatarImageView;

        @Bind(R.id.screen_name)
        TextView usernameTextView;

        @Bind(R.id.full_name)
        TextView fullNameTextView;

        @Bind(R.id.media)
        ImageView mediaImageView;

        @Bind(R.id.count)
        TextView mediaCountTextView;

        @Bind(R.id.retweet)
        ImageView retweetImageView;

        @Bind(R.id.info)
        TextView infoTextView;

        @Bind(R.id.text)
        TextView textView;

        public TweetHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
