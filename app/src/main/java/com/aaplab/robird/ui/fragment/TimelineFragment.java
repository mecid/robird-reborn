package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


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

    private TimelineModel mTimelineModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTimelineModel = new TimelineModel(mAccount,
                getArguments().getLong("timeline_id", TimelineModel.HOME_ID));

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
                            }
                        })
        );
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimelineModel.saveTimelinePosition(findFirstVisibleTweetId());
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
}
