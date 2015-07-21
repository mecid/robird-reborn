package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.UserModel;
import com.aaplab.robird.ui.fragment.UserFriendsFragment;
import com.aaplab.robird.ui.fragment.UserTimelineFragment;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.PaletteTransformation;
import com.aaplab.robird.util.RoundTransformation;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import twitter4j.Relationship;
import twitter4j.User;

/**
 * Created by majid on 18.07.15.
 */
public class UserProfileActivity extends BaseActivity {
    public static final String SCREEN_NAME = "screen_name";

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;

    @Bind(R.id.user_background)
    ImageView mUserBackgroundImageView;

    @Bind(R.id.avatar)
    ImageView mAvatarImageView;

    @Bind(R.id.full_name)
    TextView mFullNameTextView;

    @Bind(R.id.screen_name)
    TextView mScreenNameTextView;

    @Bind(R.id.bio)
    TextView mBioTextView;

    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tabs)
    TabLayout mTabs;

    private Relationship mRelationship;
    private UserModel mUserModel;
    private String mScreenName;
    private Account mAccount;
    private User mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAccount = getIntent().getParcelableExtra("account");
        mScreenName = getIntent().getStringExtra(SCREEN_NAME);

        if (TextUtils.isEmpty(mScreenName)) {
            mScreenName = getIntent().getData().getQueryParameter("username").substring(1);
            mAccount = new AccountModel().accounts().toBlocking().first().get(0);
        }

        mUserModel = new UserModel(mAccount);
        mViewPager.setAdapter(new UserProfilePagerAdapter(getSupportFragmentManager()));

        mSubscriptions.add(
                Observable.zip(
                        mUserModel.user(mScreenName),
                        mUserModel.relationship(mScreenName),
                        new Func2<User, Relationship, UserAndRelationship>() {
                            @Override
                            public UserAndRelationship call(User user, Relationship relationship) {
                                return new UserAndRelationship(user, relationship);
                            }
                        }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<UserAndRelationship>() {
                            @Override
                            public void onNext(UserAndRelationship userAndRelationship) {
                                super.onNext(userAndRelationship);
                                setupUserDetails(userAndRelationship);
                                invalidateOptionsMenu();
                            }
                        })
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile, menu);
        if (mRelationship != null) {
            boolean isFollowing = mRelationship.isSourceFollowingTarget();
            boolean isBlocking = mRelationship.isSourceBlockingTarget();

            menu.findItem(R.id.menu_follow)
                    .setIcon(isFollowing ? R.drawable.ic_account_remove : R.drawable.ic_person_add)
                    .setTitle(isFollowing ? R.string.unfollow : R.string.follow);

            menu.findItem(R.id.menu_block)
                    .setTitle(isBlocking ? R.string.unblock : R.string.block);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_follow) {
            mSubscriptions.add(
                    mUserModel
                            .follow(mScreenName)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Relationship>() {
                                @Override
                                public void onNext(Relationship relationship) {
                                    super.onNext(relationship);
                                    mRelationship = relationship;
                                    invalidateOptionsMenu();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_block) {
            mSubscriptions.add(
                    mUserModel
                            .block(mScreenName)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Relationship>() {
                                @Override
                                public void onNext(Relationship relationship) {
                                    super.onNext(relationship);
                                    mRelationship = relationship;
                                    invalidateOptionsMenu();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_spam) {
            mSubscriptions.add(
                    mUserModel
                            .report(mScreenName)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Relationship>() {
                                @Override
                                public void onNext(Relationship relationship) {
                                    super.onNext(relationship);
                                    mRelationship = relationship;
                                    invalidateOptionsMenu();
                                }
                            })
            );
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupUserDetails(UserAndRelationship userAndRelationship) {
        mRelationship = userAndRelationship.relationship;
        mUser = userAndRelationship.user;

        mTabs.addTab(mTabs.newTab().setText(String.format("%d %s", mUser.getStatusesCount(), getString(R.string.tweets))));
        mTabs.addTab(mTabs.newTab().setText(String.format("%d %s", mUser.getFriendsCount(), getString(R.string.friends))));
        mTabs.addTab(mTabs.newTab().setText(String.format("%d %s", mUser.getFollowersCount(), getString(R.string.followers))));
        mTabs.addTab(mTabs.newTab().setText(String.format("%d %s", mUser.getFavouritesCount(), getString(R.string.favorites))));
        mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));

        mFullNameTextView.setText(mUser.getName());
        mScreenNameTextView.setText("@" + mUser.getScreenName());

        mBioTextView.setText(
                String.format("%s. %s %s %s",
                        mRelationship.isTargetFollowingSource() ?
                                getString(R.string.following_you) :
                                getString(R.string.not_following_you),
                        mUser.getDescription(),
                        mUser.getURLEntity().getExpandedURL(),
                        mUser.getLocation()
                ).trim()
        );
        mBioTextView.setVisibility(TextUtils.isEmpty(mBioTextView.getText()) ? View.GONE : View.VISIBLE);

        Picasso.with(getApplicationContext())
                .load(mUser.getOriginalProfileImageURL())
                .fit().centerCrop()
                .transform(new RoundTransformation())
                .into(mAvatarImageView);

        Picasso.with(getApplicationContext())
                .load(mUser.getProfileBannerMobileRetinaURL())
                .transform(PaletteTransformation.instance())
                .fit().centerCrop()
                .into(mUserBackgroundImageView,
                        new PaletteTransformation.PaletteCallback(mUserBackgroundImageView) {
                            @Override
                            protected void onSuccess(Palette palette) {
                                int muted = palette.getMutedColor(R.color.primary);
                                int darkMuted = palette.getDarkMutedColor(R.color.primaryDark);

                                mCollapsingToolbar.setStatusBarScrimColor(darkMuted);
                                mCollapsingToolbar.setContentScrimColor(muted);
                                mBioTextView.setBackgroundColor(muted);
                                mTabs.setBackgroundColor(muted);
                            }

                            @Override
                            public void onError() {

                            }
                        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        ActivityCompat.finishAfterTransition(this);
        return true;
    }

    public static void start(Activity activity, Account account, String screenName) {
        Intent intent = new Intent(activity, UserProfileActivity.class);
        intent.putExtra("account", account);
        intent.putExtra(SCREEN_NAME, screenName);
        ActivityCompat.startActivity(activity, intent, null);
    }

    private static final class UserAndRelationship {
        final Relationship relationship;
        final User user;

        public UserAndRelationship(User user, Relationship relationship) {
            this.user = user;
            this.relationship = relationship;
        }
    }

    private final class UserProfilePagerAdapter extends FragmentStatePagerAdapter {

        public UserProfilePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return UserTimelineFragment.create(mAccount, mScreenName, UserModel.TWEETS);

            if (position == 1)
                return UserFriendsFragment.create(mAccount, mScreenName, UserModel.FRIENDS);

            if (position == 2)
                return UserFriendsFragment.create(mAccount, mScreenName, UserModel.FOLLOWERS);

            if (position == 3)
                return UserTimelineFragment.create(mAccount, mScreenName, UserModel.FAVORITES);

            throw new IllegalArgumentException("There is no fragment for position: " + position);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
