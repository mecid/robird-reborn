package com.aaplab.robird.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aaplab.robird.Analytics;
import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.TweetModel;
import com.aaplab.robird.ui.adapter.TweetDetailsAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.ArrayList;
import java.util.List;

import icepick.Icicle;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import twitter4j.Status;

/**
 * Created by majid on 21.06.15.
 */
public class TweetDetailsFragment extends BaseSwipeToRefreshRecyclerFragment {

    public static TweetDetailsFragment create(Account account, Tweet tweet) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        args.putParcelable("tweet", tweet);

        TweetDetailsFragment fragment = new TweetDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Icicle
    ArrayList<Tweet> mConversation;

    @Icicle
    ArrayList<Tweet> mReplies;

    @Icicle
    Status mDetailedStatus;

    private TweetDetailsAdapter mTweetDetailsAdapter;
    private TweetModel mTweetModel;
    private Account mAccount;
    private Tweet mTweet;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = getArguments().getParcelable("account");
        mTweet = getArguments().getParcelable("tweet");
        mTweetModel = new TweetModel(mAccount, mTweet.tweetId());
        mTweetDetailsAdapter = new TweetDetailsAdapter(getActivity(), mAccount, mTweet);
        mRecyclerView.setAdapter(mTweetDetailsAdapter);
        mRefreshLayout.setEnabled(false);
        setHasOptionsMenu(true);

        if (mConversation == null || mDetailedStatus == null || mReplies == null) {
            mSubscriptions.add(
                    Observable.zip(
                            mTweetModel.detailedStatus(),
                            mTweetModel.conversation(),
                            mTweetModel.replies(),
                            new Func3<Status, List<Tweet>, List<Tweet>, TweetDetailsHolder>() {
                                @Override
                                public TweetDetailsHolder call(Status status, List<Tweet> conversation, List<Tweet> replies) {
                                    return new TweetDetailsHolder(status, conversation, replies);
                                }
                            }
                    )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<TweetDetailsHolder>() {
                                @Override
                                public void onNext(TweetDetailsHolder tweetDetailsHolder) {
                                    super.onNext(tweetDetailsHolder);

                                    mDetailedStatus = tweetDetailsHolder.detailedStatus;
                                    mTweetDetailsAdapter.addDetails(mDetailedStatus);

                                    mConversation = new ArrayList<>(tweetDetailsHolder.conversation);
                                    mTweetDetailsAdapter.addConversation(mConversation);

                                    mReplies = new ArrayList<>(tweetDetailsHolder.replies);
                                    mTweetDetailsAdapter.addReplies(mReplies);

                                    int position = mLayoutManager.getItemCount() - mConversation.size();
                                    mLayoutManager.scrollToPosition(position - 1);
                                }
                            })
            );
        } else {
            mTweetDetailsAdapter.addDetails(mDetailedStatus);
            mTweetDetailsAdapter.addConversation(mConversation);
            mTweetDetailsAdapter.addReplies(mReplies);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tweet_details, menu);

        if (TextUtils.equals(mAccount.screenName(), mTweet.username())) {
            menu.findItem(R.id.menu_retweet).setVisible(false);
            menu.findItem(R.id.menu_remove).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_quote) {
            Analytics.event(Analytics.QUOTE);

            ComposeFragment
                    .share(String.format("https://twitter.com/%s/status/%d", mTweet.username(), mTweet.tweetId()))
                    .show(getFragmentManager(), ComposeFragment.TAG_QUOTE);

        } else if (item.getItemId() == R.id.menu_retweet) {
            Analytics.event(Analytics.RETWEET);
            mSubscriptions.add(
                    mTweetModel
                            .retweet()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<Status>() {
                                @Override
                                public void onNext(Status status) {
                                    super.onNext(status);
                                    Snackbar.make(
                                            getActivity().findViewById(R.id.coordinator),
                                            R.string.successfully_retweeted,
                                            Snackbar.LENGTH_SHORT
                                    ).show();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_star) {
            Analytics.event(Analytics.FAVORITE);
            mSubscriptions.add(
                    mTweetModel
                            .favorite()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<Status>() {
                                @Override
                                public void onNext(Status status) {
                                    super.onNext(status);
                                    Snackbar.make(
                                            getActivity().findViewById(R.id.coordinator),
                                            status.isFavorited() ? R.string.successfully_favorited : R.string.successfully_unfavorited,
                                            Snackbar.LENGTH_SHORT
                                    ).show();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_copy) {
            Analytics.event(Analytics.COPY);
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("", mTweet.text()));
            Snackbar.make(getActivity().findViewById(R.id.coordinator),
                    R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_share) {
            Analytics.event(Analytics.SHARE);
            Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                    .setType("text/plain")
                    .setText(mTweet.text())
                    .getIntent();

            if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(shareIntent);
            }
        } else if (item.getItemId() == R.id.menu_remove) {
            Analytics.event(Analytics.DELETE);
            mSubscriptions.add(
                    mTweetModel
                            .delete()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<Status>() {
                                @Override
                                public void onNext(Status status) {
                                    super.onNext(status);
                                    Snackbar.make(
                                            getActivity().findViewById(R.id.coordinator),
                                            R.string.successfully_deleted,
                                            Snackbar.LENGTH_SHORT
                                    ).show();
                                }
                            })
            );
        }

        return super.onOptionsItemSelected(item);
    }

    private static final class TweetDetailsHolder {
        public Status detailedStatus;
        public List<Tweet> conversation;
        public List<Tweet> replies;

        public TweetDetailsHolder(Status detailedStatus, List<Tweet> conversation, List<Tweet> replies) {
            this.detailedStatus = detailedStatus;
            this.conversation = conversation;
            this.replies = replies;
        }
    }
}
