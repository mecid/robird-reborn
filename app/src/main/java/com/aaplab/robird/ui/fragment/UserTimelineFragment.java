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
    public static final String TYPE = "type";

    public static UserTimelineFragment create(Account account, String screenName, @UserModel.Type int type) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("account", account);
        bundle.putString(UserProfileActivity.SCREEN_NAME, screenName);
        bundle.putInt(TYPE, type);

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
        mType = getArguments().getInt(TYPE);
        mUserModel = new UserModel(mAccount);
        mRefreshLayout.setRefreshing(true);

        mUserModel
                .tweets(mScreenName, mType, new Paging().count(50))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<List<Tweet>>() {
                    @Override
                    public void onNext(List<Tweet> tweets) {
                        super.onNext(tweets);
                        appendTweetsToTop(tweets);
                        mRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mUserModel
                .tweets(mScreenName, mType, new Paging(findFirstVisibleTweetId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<List<Tweet>>() {
                    @Override
                    public void onNext(List<Tweet> tweets) {
                        super.onNext(tweets);
                        appendTweetsToTop(tweets);
                        mRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void startBottomLoading() {
        mUserModel
                .tweets(mScreenName, mType,
                        new Paging().maxId(mTweets.get(mTweets.size() - 1).tweetId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultObserver<List<Tweet>>() {
                    @Override
                    public void onNext(List<Tweet> tweets) {
                        super.onNext(tweets);
                        appendTweetsToBottom(tweets);
                        stopBottoLoading(tweets.size() > 1);
                    }
                });
    }
}
