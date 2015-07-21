package com.aaplab.robird.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.aaplab.robird.Config;
import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.UserModel;
import com.aaplab.robird.data.provider.contract.AccountContract;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.NavigationUtils;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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

    private Twitter mTwitter;
    private RequestToken mRequestToken;
    private AccountModel mAccountModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAccountModel = new AccountModel();

        mSubscriptions.add(requestToken()
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
                                    return mAccountModel.add(accessToken);
                                }
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Account>() {
                                @Override
                                public void onNext(final Account account) {
                                    super.onNext(account);
                                    UserModel userModel = new UserModel(account, account.screenName());
                                    userModel.user()
                                            .flatMap(new Func1<User, Observable<Account>>() {
                                                @Override
                                                public Observable<Account> call(User user) {
                                                    return mAccountModel.update(
                                                            account.withMeta(
                                                                    user.getName(),
                                                                    user.getScreenName(),
                                                                    user.getOriginalProfileImageURL(),
                                                                    user.getProfileBannerMobileRetinaURL()
                                                            ));
                                                }
                                            })
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new DefaultObserver<Account>() {
                                                @Override
                                                public void onNext(Account account) {
                                                    super.onNext(account);
                                                    NavigationUtils.changeDefaultActivityToSignIn(getApplicationContext(), false);
                                                    ActivityCompat.startActivity(SignInActivity.this,
                                                            new Intent(SignInActivity.this, HomeActivity.class), null);
                                                    ActivityCompat.finishAfterTransition(SignInActivity.this);
                                                }
                                            });
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
                    final ConfigurationBuilder builder = new ConfigurationBuilder();
                    builder.setOAuthConsumerKey(Config.TWITTER_CONSUMER_KEY);
                    builder.setOAuthConsumerSecret(Config.TWITTER_CONSUMER_SECRET);
                    mTwitter = new TwitterFactory(builder.build()).getInstance();
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
}
