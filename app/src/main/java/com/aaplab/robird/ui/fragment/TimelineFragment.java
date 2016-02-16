package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import butterknife.ButterKnife;
import icepick.Icicle;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;


/**
 * Created by majid on 19.01.15.
 */
public class TimelineFragment extends BaseTimelineFragment {

    public static TimelineFragment create(Account account, long timelineId) {
        Bundle args = new Bundle();
        args.putLong("timeline_id", timelineId);
        args.putParcelable("account", account);

        TimelineFragment fragment = new TimelineFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /* This variable will be used only
    in cases when streaming is enabled*/
    @Icicle
    boolean mIsTimelineSyncedInitially;

    private TimelineModel mTimelineModel;
    private PrefsModel mPrefsModel;
    private TextView mUnreadCountTextView;
    private long mLastUnread;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTimelineModel = new TimelineModel(mAccount,
                getArguments().getLong("timeline_id", TimelineModel.HOME_ID));
        mPrefsModel = new PrefsModel();
        mLastUnread = mTimelineModel.lastUnread();
        mRecyclerView.addOnScrollListener(new UnreadCounterScrollListener());
        setHasOptionsMenu(true);

        mSubscriptions.add(
                mTimelineModel
                        .timeline()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<List<Tweet>>() {
                            @Override
                            public void onNext(List<Tweet> tweets) {
                                super.onNext(tweets);
                                setupTimeline(tweets);
                                ActivityCompat.invalidateOptionsMenu(getActivity());
                            }
                        })
        );

        // Refresh only once before starting streaming
        if (mPrefsModel.isTwitterStreamingEnabled()) {
            if (!mIsTimelineSyncedInitially) {
                setRefreshing(true);
                onRefresh();
            } else {
                mRefreshLayout.setEnabled(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.timeline, menu);
        MenuItem unreadMenuItem = menu.findItem(R.id.menu_unread);
        MenuItem refreshMenuItem = menu.findItem(R.id.menu_refresh);

        final View actionView = MenuItemCompat.getActionView(unreadMenuItem);
        final int unreadCount = findPosition(mLastUnread);

        mUnreadCountTextView = ButterKnife.findById(actionView, R.id.unread);
        mUnreadCountTextView.setText("" + (unreadCount == -1 ? 0 : unreadCount));
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayoutManager.scrollToPosition(0);
            }
        });

        // hide menu item if streaming is enabled
        refreshMenuItem.setVisible(!mPrefsModel.isTwitterStreamingEnabled());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            setRefreshing(true);
            onRefresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimelineModel.saveTimelinePosition(findFirstVisibleTweetId());
        mTimelineModel.saveLastUnread(mLastUnread);
    }

    @Override
    public void onRefresh() {
        mSubscriptions.add(
                mTimelineModel
                        .update()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<Integer>() {
                            @Override
                            public void onNext(Integer newTweetCount) {
                                super.onNext(newTweetCount);

                                setRefreshing(false);

                                // mark timeline as refreshed
                                mIsTimelineSyncedInitially = true;

                                // disable pull-to-refresh if streaming is enabled
                                mRefreshLayout.setEnabled(!mPrefsModel.isTwitterStreamingEnabled());
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                setRefreshing(false);
                                if (e instanceof TwitterException) {
                                    final TwitterException twitterException = (TwitterException) e;
                                    if (twitterException.exceededRateLimitation()) {
                                        final RateLimitStatus status = twitterException.getRateLimitStatus();
                                        Snackbar.make(
                                                getActivity().findViewById(R.id.coordinator),
                                                getString(R.string.rate_limit, status.getSecondsUntilReset()),
                                                Snackbar.LENGTH_LONG
                                        ).show();
                                    }
                                }
                            }
                        })
        );
    }

    @Override
    public void startBottomLoading() {
        mSubscriptions.add(
                mTimelineModel
                        .old()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<Integer>() {
                            @Override
                            public void onNext(Integer newTweetCount) {
                                super.onNext(newTweetCount);
                                stopBottoLoading(newTweetCount > 1);
                            }
                        })
        );
    }

    private void setupTimeline(List<Tweet> tweets) {
        long id = findFirstVisibleTweetId();
        int top = getFirstVisibleTweetTop();

        mTweets.clear();
        mTweets.addAll(tweets);
        mAdapter.notifyDataSetChanged();

        if (id != -1) {
            setTimelinePosition(id, top);
        } else {
            setTimelinePosition(mTimelineModel.timelinePosition(), 0);
        }
    }

    private final class UnreadCounterScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            final int firstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();

            if (firstVisiblePosition >= 0 && mUnreadCountTextView != null) {
                final Tweet tweet = mTweets.get(firstVisiblePosition);

                if (tweet.tweetId() >= mLastUnread) {
                    mUnreadCountTextView.setText("" + firstVisiblePosition);
                    mLastUnread = tweet.tweetId();
                }
            }
        }
    }
}
