package com.aaplab.robird.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.TweetModel;
import com.aaplab.robird.ui.adapter.TweetDetailsAdapter;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
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

    private TweetDetailsAdapter mTweetDetailsAdapter;
    private TweetModel mTweetModel;
    private Tweet mTweet;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Account mAccount = getArguments().getParcelable("account");
        mTweet = getArguments().getParcelable("tweet");
        mTweetModel = new TweetModel(mAccount, mTweet);
        mTweetDetailsAdapter = new TweetDetailsAdapter(getActivity(), mAccount, mTweet);
        mRecyclerView.setAdapter(mTweetDetailsAdapter);
        mRefreshLayout.setEnabled(false);
        setHasOptionsMenu(true);

        mSubscriptions.add(
                mTweetModel
                        .tweet()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<Status>() {
                            @Override
                            public void onNext(Status status) {
                                super.onNext(status);
                                mTweetDetailsAdapter.addDetails(status);
                            }
                        })
        );

        mSubscriptions.add(
                mTweetModel
                        .conversation()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<List<Tweet>>() {
                            @Override
                            public void onNext(List<Tweet> tweets) {
                                super.onNext(tweets);
                                mTweetDetailsAdapter.addConversation(tweets);
                            }
                        })
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tweet_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_retweet) {
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
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("", mTweet.text()));
            Snackbar.make(getActivity().findViewById(R.id.coordinator),
                    R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mTweet.text());
            startActivity(Intent.createChooser(shareIntent, ""));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

    }
}
