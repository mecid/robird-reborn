package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.ui.adapter.TweetAdapter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by majid on 26.01.15.
 */
public abstract class BaseTimelineFragment extends BaseSwipeToRefreshRecyclerFragment {

    protected Account mAccount;
    private boolean mBottomLoading;
    protected TweetAdapter mAdapter;
    private boolean mKeepOnAppending = true;
    protected CopyOnWriteArrayList<Tweet> mTweets;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTweets = new CopyOnWriteArrayList<>();
        mAccount = getArguments().getParcelable("account");
        mAdapter = new TweetAdapter(getActivity(), mAccount, mTweets);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new ScrollListener());
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
        int index = Iterables.indexOf(mTweets, new Predicate<Tweet>() {
            @Override
            public boolean apply(Tweet input) {
                return tweetId == input.tweetId();
            }
        });

        mLayoutManager.scrollToPositionWithOffset(index, top);
    }

    protected void stopBottoLoading(boolean keepOnAppending) {
        mBottomLoading = false;
        mKeepOnAppending = keepOnAppending;
    }

    public abstract void startBottomLoading();

    private final class ScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            int visibleItemCount = mLayoutManager.findLastVisibleItemPosition() - firstVisibleItem;

            if (totalItemCount > 0 && mKeepOnAppending) {
                if (!mBottomLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 10)) {
                    mBottomLoading = true;
                    startBottomLoading();
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Picasso picasso = Picasso.with(recyclerView.getContext());
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                picasso.resumeTag(TweetAdapter.IMAGE_LOADING_TAG);
            } else {
                picasso.pauseTag(TweetAdapter.IMAGE_LOADING_TAG);
            }
        }
    }
}
