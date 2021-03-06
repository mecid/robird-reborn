package com.aaplab.robird.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.aaplab.robird.Config;
import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.ContactModel;
import com.aaplab.robird.data.model.DirectsModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.data.model.UserListsModel;
import com.aaplab.robird.data.model.UserModel;
import com.aaplab.robird.data.provider.contract.AccountContract;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.NavigationUtils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func7;
import rx.schedulers.Schedulers;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by majid on 07.05.15.
 */
public class SignInActivity extends BaseActivity {
    public static final String CALLBACK = "robird://auth";

    private Account mAccount;
    private Twitter mTwitter;
    private RequestToken mRequestToken;
    private AccountModel mAccountModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAccountModel = new AccountModel();

        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(Config.TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(Config.TWITTER_CONSUMER_SECRET);
        mTwitter = new TwitterFactory(builder.build()).getInstance();

        mSubscriptions.add(
                requestToken()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<RequestToken>() {
                            @Override
                            public void onNext(RequestToken requestToken) {
                                super.onNext(requestToken);
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(requestToken.getAuthorizationURL())));
                                mRequestToken = requestToken;
                            }
                        }));
    }

    @Override
    public void onBackPressed() {
        if (getContentResolver().query(AccountContract.CONTENT_URI, null, null, null, null).getCount() > 0) {
            NavigationUtils.changeDefaultActivityToSignIn(getApplicationContext(), false);
            ActivityCompat.startActivity(SignInActivity.this,
                    new Intent(SignInActivity.this, HomeActivity.class), null);
        }

        super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null && uri.toString().startsWith(CALLBACK)) {
            final String verifier = uri.getQueryParameter("oauth_verifier");
            mSubscriptions.add(
                    parseToken(verifier)
                            .flatMap(new Func1<AccessToken, Observable<Account>>() {
                                @Override
                                public Observable<Account> call(AccessToken accessToken) {
                                    return saveAccount(accessToken);
                                }
                            })
                            .flatMap(new Func1<Account, Observable<Integer>>() {
                                @Override
                                public Observable<Integer> call(Account account) {
                                    return cacheData(account);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Integer>() {
                                @Override
                                public void onCompleted() {
                                    super.onCompleted();
                                    NavigationUtils.changeDefaultActivityToSignIn(getApplicationContext(), false);
                                    ActivityCompat.startActivity(SignInActivity.this,
                                            new Intent(SignInActivity.this, HomeActivity.class), null);
                                    ActivityCompat.finishAfterTransition(SignInActivity.this);
                                }
                            })
            );
        }
    }

    private Observable<RequestToken> requestToken() {
        return Observable.create(new Observable.OnSubscribe<RequestToken>() {
            @Override
            public void call(Subscriber<? super RequestToken> subscriber) {
                try {
                    subscriber.onNext(mTwitter.getOAuthRequestToken(CALLBACK));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private Observable<AccessToken> parseToken(final String verifier) {
        return Observable.create(new Observable.OnSubscribe<AccessToken>() {
            @Override
            public void call(Subscriber<? super AccessToken> subscriber) {
                try {
                    subscriber.onNext(mTwitter.getOAuthAccessToken(mRequestToken, verifier));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private Observable<Account> saveAccount(AccessToken accessToken) {
        return mAccountModel
                .add(accessToken)
                .flatMap(new Func1<Account, Observable<User>>() {
                    @Override
                    public Observable<User> call(Account account) {
                        mAccount = account;
                        return new UserModel(mAccount, account.screenName()).user();
                    }
                })
                .flatMap(new Func1<User, Observable<Account>>() {
                    @Override
                    public Observable<Account> call(User user) {
                        return mAccountModel.update(
                                mAccount.withMeta(
                                        user.getName(),
                                        user.getScreenName(),
                                        user.getOriginalProfileImageURL(),
                                        user.getProfileBannerMobileRetinaURL()
                                ));
                    }
                });
    }

    private Observable<Integer> cacheData(final Account account) {
        return Observable.zip(
                new DirectsModel(account).update(),
                new TimelineModel(account, TimelineModel.HOME_ID).update(),
                new TimelineModel(account, TimelineModel.MENTIONS_ID).update(),
                new TimelineModel(account, TimelineModel.FAVORITES_ID).update(),
                new TimelineModel(account, TimelineModel.RETWEETS_ID).update(),
                new UserListsModel(account).update(),
                new ContactModel(account).update(),
                new Func7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer, Integer integer2, Integer integer3, Integer integer4, Integer integer5, Integer integer6, Integer integer7) {
                        return integer + integer2 + integer3 + integer4 + integer5 + integer6 + integer7;
                    }
                }
        )
                .flatMap(new Func1<Integer, Observable<UserList>>() {
                    @Override
                    public Observable<UserList> call(Integer integer) {
                        return Observable.from(new UserListsModel(account).lists().toBlocking().first());
                    }
                })
                .flatMap(new Func1<UserList, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(UserList userList) {
                        return new TimelineModel(account, userList.listId()).update();
                    }
                });
    }
}
