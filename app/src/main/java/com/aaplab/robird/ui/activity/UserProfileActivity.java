package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
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
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.Analytics;
import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.UserListsModel;
import com.aaplab.robird.data.model.UserModel;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.UserFriendsFragment;
import com.aaplab.robird.ui.fragment.UserTimelineFragment;
import com.aaplab.robird.util.DefaultObserver;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icicle;
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

    @Icicle
    User mUser;

    @Icicle
    Relationship mRelationship;

    private UserListsModel mUserListsModel;
    private UserModel mUserModel;
    private String mScreenName;
    private Account mAccount;

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

        mUserListsModel = new UserListsModel(mAccount);
        mUserModel = new UserModel(mAccount, mScreenName);
        mViewPager.setAdapter(new UserProfilePagerAdapter(getSupportFragmentManager()));

        if (savedInstanceState == null || mUser == null || mRelationship == null) {
            mSubscriptions.add(
                    Observable.zip(
                            mUserModel.user(),
                            mUserModel.relationship(),
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
                                    supportInvalidateOptionsMenu();
                                }
                            })
            );
        } else {
            setupUserDetails(new UserAndRelationship(mUser, mRelationship));
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_profile, menu);

        if (mUser != null && TextUtils.equals(mAccount.screenName(), mUser.getScreenName())) {
            menu.findItem(R.id.menu_direct).setVisible(false);
            menu.findItem(R.id.menu_reply).setVisible(false);
            menu.findItem(R.id.menu_spam).setVisible(false);
            menu.findItem(R.id.menu_block).setVisible(false);
            menu.findItem(R.id.menu_follow).setVisible(false);
        }

        List<UserList> userLists = mUserListsModel.lists().toBlocking().first();
        if (!userLists.isEmpty()) {
            final SubMenu listSubMenu = menu.addSubMenu(R.string.add_to_list);
            for (UserList userList : userLists) {
                listSubMenu.add(0, (int) userList.listId(), Menu.NONE, userList.name());
            }
        }

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
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_follow) {
            Analytics.event(Analytics.FOLLOW);
            mSubscriptions.add(
                    mUserModel
                            .follow()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Relationship>() {
                                @Override
                                public void onNext(Relationship relationship) {
                                    super.onNext(relationship);
                                    mRelationship = relationship;
                                    supportInvalidateOptionsMenu();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_block) {
            Analytics.event(Analytics.BLOCK);
            mSubscriptions.add(
                    mUserModel
                            .block()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Relationship>() {
                                @Override
                                public void onNext(Relationship relationship) {
                                    super.onNext(relationship);
                                    mRelationship = relationship;
                                    supportInvalidateOptionsMenu();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_spam) {
            Analytics.event(Analytics.SPAM);
            mSubscriptions.add(
                    mUserModel
                            .report()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<Relationship>() {
                                @Override
                                public void onNext(Relationship relationship) {
                                    super.onNext(relationship);
                                    mRelationship = relationship;
                                    supportInvalidateOptionsMenu();
                                }
                            })
            );
        } else if (item.getItemId() == R.id.menu_direct) {
            Analytics.event(ComposeFragment.TAG_DIRECT);
            ComposeFragment.direct(mAccount, mUser.getScreenName())
                    .show(getSupportFragmentManager(), ComposeFragment.TAG_DIRECT);
        } else if (item.getItemId() == R.id.menu_reply) {
            Analytics.event(ComposeFragment.TAG_REPLY);
            ComposeFragment.share(String.format("@%s ", mUser.getScreenName()))
                    .show(getSupportFragmentManager(), ComposeFragment.TAG_REPLY);
        } else {
            Analytics.event(Analytics.ADD_TO_LIST);
            mSubscriptions.add(
                    mUserListsModel
                            .addUser(item.getItemId(), mUser.getId())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new DefaultObserver<twitter4j.UserList>() {
                                @Override
                                public void onNext(twitter4j.UserList userList) {
                                    super.onNext(userList);
                                    Snackbar.make(
                                            findViewById(R.id.coordinator),
                                            getString(R.string.user_added_to_list, mUser.getScreenName(), item.getTitle()),
                                            Snackbar.LENGTH_SHORT
                                    ).show();
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

        final StringBuilder bioBuilder = new StringBuilder();
        if (!TextUtils.equals(mAccount.screenName(), mUser.getScreenName()))
            bioBuilder.append(
                    mRelationship.isTargetFollowingSource() ?
                            getString(R.string.following_you) :
                            getString(R.string.not_following_you)
            ).append(". ");

        bioBuilder.append(
                String.format("%s %s %s",
                        mUser.getDescription(),
                        mUser.getURLEntity().getExpandedURL(),
                        mUser.getLocation()
                ));

        mBioTextView.setText(bioBuilder.toString().trim());
        mBioTextView.setVisibility(TextUtils.isEmpty(mBioTextView.getText()) ? View.GONE : View.VISIBLE);

        Glide.with(this)
                .load(mUser.getOriginalProfileImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mAvatarImageView);

        Glide.with(this)
                .load(mUser.getProfileBannerMobileRetinaURL())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(mUserBackgroundImageView) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);

                        final Palette palette = Palette.from(resource).generate();
                        final int muted = palette.getMutedColor(getResources().getColor(R.color.primary));
                        final int darkMuted = palette.getDarkMutedColor(getResources().getColor(R.color.primaryDark));

                        mCollapsingToolbar.setStatusBarScrimColor(darkMuted);
                        mCollapsingToolbar.setContentScrimColor(muted);
                        mBioTextView.setBackgroundColor(muted);
                        mTabs.setBackgroundColor(muted);
                    }
                });
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
