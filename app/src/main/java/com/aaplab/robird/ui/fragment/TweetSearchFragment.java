package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.SearchModel;
import com.aaplab.robird.util.DefaultObserver;
import com.google.common.collect.Iterables;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Query;

/**
 * Created by majid on 10.08.15.
 */
public class TweetSearchFragment extends BaseTimelineFragment {
    public static TweetSearchFragment create(Account account, String query) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        args.putString("query", query);

        TweetSearchFragment fragment = new TweetSearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private SearchModel mSearchModel;
    private String mQuery;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mQuery = getArguments().getString("query");
        mSearchModel = new SearchModel(mAccount);

        if (savedInstanceState == null) {
            mRefreshLayout.setRefreshing(true);
            mSubscriptions.add(
                    mSearchModel
                            .tweets(new Query(mQuery))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<List<Tweet>>() {
                                @Override
                                public void onNext(List<Tweet> tweets) {
                                    super.onNext(tweets);
                                    appendTweetsToTop(tweets);
                                    mRefreshLayout.setRefreshing(false);
                                }
                            })
            );
        }
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mSubscriptions.add(
                mSearchModel
                        .tweets(new Query(mQuery).sinceId(findFirstVisibleTweetId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<Tweet>>() {
                            @Override
                            public void onNext(List<Tweet> tweets) {
                                super.onNext(tweets);
                                appendTweetsToTop(tweets);
                                mRefreshLayout.setRefreshing(false);
                            }
                        })
        );
    }

    @Override
    public void startBottomLoading() {
        mSubscriptions.add(
                mSearchModel
                        .tweets(new Query(mQuery).maxId(Iterables.getLast(mTweets).tweetId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<List<Tweet>>() {
                            @Override
                            public void onNext(List<Tweet> tweets) {
                                super.onNext(tweets);
                                appendTweetsToBottom(tweets);
                                stopBottoLoading(tweets.size() > 1);
                            }
                        })
        );
    }
}
