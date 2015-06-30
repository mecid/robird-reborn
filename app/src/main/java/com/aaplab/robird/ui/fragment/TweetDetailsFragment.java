package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;

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
        args.putSerializable("account", account);
        args.putSerializable("tweet", tweet);

        TweetDetailsFragment fragment = new TweetDetailsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private TweetDetailsAdapter mTweetDetailsAdapter;
    private TweetModel mTweetModel;
    private Account mAccount;
    private Tweet mTweet;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAccount = (Account) getArguments().getSerializable("account");
        mTweet = (Tweet) getArguments().getSerializable("tweet");
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
                                mTweetDetailsAdapter.showDetails(status);
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
                                mTweetDetailsAdapter.showConversation(tweets);
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
    public void onRefresh() {

    }
}
