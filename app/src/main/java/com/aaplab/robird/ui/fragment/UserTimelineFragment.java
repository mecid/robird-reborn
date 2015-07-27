package com.aaplab.robird.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.UserModel;
import com.aaplab.robird.ui.activity.UserProfileActivity;
import com.aaplab.robird.util.DefaultObserver;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.Paging;

/**
 * Created by majid on 19.07.15.
 */
public class UserTimelineFragment extends BaseTimelineFragment {
    public static UserTimelineFragment create(Account account, String screenName, @UserModel.TimelineType int type) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putParcelable("account", account);
        bundle.putString(UserProfileActivity.SCREEN_NAME, screenName);

        UserTimelineFragment fragment = new UserTimelineFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private UserModel mUserModel;
    private String mScreenName;
    private int mType;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mScreenName = getArguments().getString(UserProfileActivity.SCREEN_NAME);
        mUserModel = new UserModel(mAccount, mScreenName);
        mType = getArguments().getInt("type");

        if (savedInstanceState == null) {
            mRefreshLayout.setRefreshing(true);
            mSubscriptions.add(
                    mUserModel
                            .tweets(mType, new Paging().count(50))
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
                mUserModel
                        .tweets(mType, new Paging(findFirstVisibleTweetId()))
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
                mUserModel
                        .tweets(mType, new Paging().maxId(mTweets.get(mTweets.size() - 1).tweetId()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
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
