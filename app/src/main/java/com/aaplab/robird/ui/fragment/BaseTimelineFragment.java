package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.ui.adapter.TweetAdapter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

import icepick.Icicle;

/**
 * Created by majid on 26.01.15.
 */
public abstract class BaseTimelineFragment extends BaseSwipeToRefreshRecyclerFragment {

    @Icicle
    ArrayList<Tweet> mTweets;

    @Icicle
    long mFirstVisibleTweetPositionId;

    protected Account mAccount;
    protected TweetAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = getArguments().getParcelable("account");
        mTweets = mTweets == null ? new ArrayList<Tweet>() : mTweets;
        mAdapter = new TweetAdapter(getActivity(), mAccount, mTweets);
        mRecyclerView.setAdapter(mAdapter);
        setTimelinePosition(mFirstVisibleTweetPositionId, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mFirstVisibleTweetPositionId = findFirstVisibleTweetId();
        super.onSaveInstanceState(outState);
    }

    protected void appendTweetsToTop(List<Tweet> newTweets) {
        long id = findFirstVisibleTweetId();
        int top = getFirstVisibleTweetTop();

        mTweets.addAll(0, newTweets);
        mAdapter.notifyDataSetChanged();

        setTimelinePosition(id, top);
    }

    protected void appendTweetsToBottom(List<Tweet> newTweets) {
        if (newTweets.size() > 0 && mTweets.size() > 0) {
            mTweets.remove(mTweets.size() - 1);
            mTweets.addAll(newTweets);
            mAdapter.notifyDataSetChanged();
        }
    }

    protected int getFirstVisibleTweetTop() {
        View view = mRecyclerView.getChildAt(0);
        return view != null ? view.getTop() : 0;
    }

    protected long findFirstVisibleTweetId() {
        int i = mLayoutManager.findFirstVisibleItemPosition();
        return i != -1 ? mTweets.get(i).tweetId() : -1;
    }

    protected void setTimelinePosition(final long tweetId, int top) {
        mLayoutManager.scrollToPositionWithOffset(findPosition(tweetId), top);
    }

    protected int findPosition(final long tweetId) {
        return Iterables.indexOf(mTweets, new Predicate<Tweet>() {
            @Override
            public boolean apply(Tweet input) {
                return tweetId == input.tweetId();
            }
        });
    }
}
