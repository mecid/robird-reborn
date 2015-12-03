package com.aaplab.robird.ui.activity;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.AccountUpdateService;
import com.aaplab.robird.Analytics;
import com.aaplab.robird.R;
import com.aaplab.robird.TimelineUpdateService;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.BillingModel;
import com.aaplab.robird.data.model.PrefsModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.DirectsFragment;
import com.aaplab.robird.ui.fragment.TimelineFragment;
import com.aaplab.robird.ui.fragment.TrendsFragment;
import com.aaplab.robird.ui.fragment.UserListsFragment;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.NavigationUtils;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icicle;
import jonathanfinerty.once.Once;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by majid on 07.05.15.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.navigation)
    NavigationView mNavigationView;

    @Bind(R.id.pager)
    ViewPager mPager;

    @Icicle
    int mSelectedNavigationPosition;

    @Icicle
    int mSelectedAccountId;

    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView mAddAccountImageView;
    private ImageView mBackgroundImageView;
    private TextView mScreenNameTextView;
    private TextView mFullNameTextView;
    private ImageView[] avatars = new ImageView[3];

    private AccountModel mAccountModel;
    private BillingModel mBillingModel;
    private List<Account> mAccounts;
    private PrefsModel mPrefsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        final View headerView = mNavigationView.inflateHeaderView(R.layout.navigation_header);
        avatars[0] = ButterKnife.findById(headerView, R.id.avatar);
        avatars[1] = ButterKnife.findById(headerView, R.id.avatar2);
        avatars[2] = ButterKnife.findById(headerView, R.id.avatar3);
        mBackgroundImageView = ButterKnife.findById(headerView, R.id.user_background);
        mAddAccountImageView = ButterKnife.findById(headerView, R.id.add_account_button);
        mScreenNameTextView = ButterKnife.findById(headerView, R.id.screen_name);
        mFullNameTextView = ButterKnife.findById(headerView, R.id.full_name);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mFloatingActionButton.setOnClickListener(this);
        mAddAccountImageView.setOnClickListener(this);
        avatars[0].setOnClickListener(this);
        avatars[1].setOnClickListener(this);
        avatars[2].setOnClickListener(this);

        mPrefsModel = new PrefsModel();
        mBillingModel = new BillingModel(this);
        mAccountModel = new AccountModel();
        mSubscriptions.add(
                Observable
                        .just(getIntent().<Account>getParcelableExtra("account"))
                        .flatMap(new Func1<Account, Observable<Integer>>() {
                            @Override
                            public Observable<Integer> call(Account account) {
                                if (account == null)
                                    return Observable.just(0);

                                mSelectedNavigationPosition = 1; //position: mentions
                                return mAccountModel.activate(account);
                            }
                        })
                        .flatMap(new Func1<Integer, Observable<List<Account>>>() {
                            @Override
                            public Observable<List<Account>> call(Integer integer) {
                                return mAccountModel.accounts();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultObserver<List<Account>>() {
                            @Override
                            public void onNext(List<Account> accounts) {
                                super.onNext(accounts);
                                setupNavigation(accounts);
                            }
                        })
        );

        final ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
        if (reader.isShareIntent()) {
            handleShareIntent(reader);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!Once.beenDone(Once.THIS_APP_VERSION, "update")) {
            final GcmNetworkManager manager = GcmNetworkManager.getInstance(this);
            if (mPrefsModel.isBackgroundUpdateServiceEnabled())
                manager.schedule(TimelineUpdateService.create(mPrefsModel.backgroundUpdateInterval() / 1000));
            manager.schedule(AccountUpdateService.create());
            Once.markDone("update");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() != R.id.navigation_item_settings) {
            selectNavigation(menuItem);
            if (mSelectedNavigationPosition != menuItem.getOrder() ||
                    mSelectedAccountId != mAccounts.get(0).id()) {
                mSelectedAccountId = mAccounts.get(0).id();
                mSelectedNavigationPosition = menuItem.getOrder();
                mPager.post(new Runnable() {
                    @Override
                    public void run() {
                        mPager.setCurrentItem(menuItem.getOrder(), false);
                    }
                });
            }
        } else {
            ActivityCompat.startActivity(this, new Intent(this, SettingsActivity.class), null);
        }

        return true;
    }

    public void selectNavigation(MenuItem menuItem) {
        setTitle(menuItem.getTitle());
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onClick(View v) {
        if (v == mAddAccountImageView) {
            Analytics.event(Analytics.ADD_ACCOUNT);
            if (mAccounts.size() < 2) {
                if (mBillingModel.isPurchased(BillingModel.SECOND_ACCOUNT_PRODUCT_ID)) {
                    startSignIn();
                } else {
                    unlockNextAccount(BillingModel.SECOND_ACCOUNT_PRODUCT_ID);
                }
            } else if (mAccounts.size() < 3) {
                if (mBillingModel.isPurchased(BillingModel.THIRD_ACCOUNT_PRODUCT_ID)) {
                    startSignIn();
                } else {
                    unlockNextAccount(BillingModel.THIRD_ACCOUNT_PRODUCT_ID);
                }
            }
        } else if (avatars[0] == v) {
            Account selectedAccount = mAccounts.get(0);
            UserProfileActivity.start(this, selectedAccount, selectedAccount.screenName());
        } else if (avatars[1] == v) {
            Account selectedAccount = mAccounts.get(1);
            activate(selectedAccount);
        } else if (avatars[2] == v) {
            Account selectedAccount = mAccounts.get(2);
            activate(selectedAccount);
        } else if (mFloatingActionButton == v) {
            Analytics.event(ComposeFragment.TAG_COMPOSE);
            ComposeFragment.create(mAccounts.get(0))
                    .show(getSupportFragmentManager(), ComposeFragment.TAG_COMPOSE);
        }
    }

    private void setupNavigation(List<Account> accounts) {
        mAccounts = accounts;
        Account activeAccount = mAccounts.get(0);
        mAddAccountImageView.setVisibility(mAccounts.size() < 3 ? View.VISIBLE : View.GONE);

        mScreenNameTextView.setText(TextUtils.concat("@", activeAccount.screenName()));
        mFullNameTextView.setText(activeAccount.fullName());

        Picasso.with(this)
                .load(activeAccount.userBackground())
                .into(mBackgroundImageView);

        for (int i = 0; i < mAccounts.size(); ++i) {
            Account account = mAccounts.get(i);
            Picasso.with(this)
                    .load(account.avatar())
                    .into(avatars[i]);
            avatars[i].setVisibility(View.VISIBLE);
        }

        mPager.setAdapter(new NavigationPagerAdapter(getSupportFragmentManager()));
        onNavigationItemSelected(mNavigationView.getMenu().getItem(mSelectedNavigationPosition));
        mNavigationView.setNavigationItemSelectedListener(this);
        mPager.clearOnPageChangeListeners();
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectNavigation(mNavigationView.getMenu().getItem(position));
            }
        });
    }

    private final class NavigationPagerAdapter extends FragmentStatePagerAdapter {

        public NavigationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return TimelineFragment.create(mAccounts.get(0), TimelineModel.HOME_ID);
            } else if (position == 1) {
                return TimelineFragment.create(mAccounts.get(0), TimelineModel.MENTIONS_ID);
            } else if (position == 2) {
                return TimelineFragment.create(mAccounts.get(0), TimelineModel.RETWEETS_ID);
            } else if (position == 3) {
                return TimelineFragment.create(mAccounts.get(0), TimelineModel.FAVORITES_ID);
            } else if (position == 4) {
                return DirectsFragment.create(mAccounts.get(0));
            } else if (position == 5) {
                return UserListsFragment.create(mAccounts.get(0));
            } else if (position == 6) {
                return TrendsFragment.create(mAccounts.get(0));
            }

            throw new IllegalArgumentException("There is no fragment for position: " + position);
        }

        @Override
        public int getCount() {
            return 7;
        }
    }

    private void activate(final Account selectedAccount) {
        mSubscriptions.add(
                mAccountModel
                        .activate(selectedAccount)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe()
        );
    }

    private void unlockNextAccount(final String productId) {
        mSubscriptions.add(
                mBillingModel
                        .purchase(productId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DefaultObserver<String>() {
                            @Override
                            public void onNext(String s) {
                                super.onNext(s);
                                if (TextUtils.equals(s, productId))
                                    Snackbar.make(findViewById(R.id.coordinator),
                                            R.string.purchased, Snackbar.LENGTH_LONG).show();
                            }
                        })
        );
    }

    private void handleShareIntent(final ShareCompat.IntentReader reader) {
        Analytics.event(Analytics.SHARE_VIA_ROBIRD);
        if (reader.getStreamCount() > 0) {
            final int count = reader.getStreamCount() > 4 ? 4 : reader.getStreamCount();
            ComposeFragment
                    .share(new ArrayList<Uri>() {
                        {
                            for (int i = 0; i < count; ++i)
                                add(reader.getStream(i));
                        }
                    })
                    .show(getSupportFragmentManager(), ComposeFragment.TAG_SHARE);
        } else {
            final StringBuilder shareBuilder = new StringBuilder();

            if (!TextUtils.isEmpty(reader.getSubject()))
                shareBuilder.append(reader.getSubject()).append(" ");

            shareBuilder.append(reader.getText());

            ComposeFragment
                    .share(shareBuilder.toString())
                    .show(getSupportFragmentManager(), ComposeFragment.TAG_SHARE);
        }
    }

    private void startSignIn() {
        NavigationUtils.changeDefaultActivityToSignIn(this, true);
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mBillingModel.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Analytics.event(Analytics.SEARCH);
        SearchActivity.start(this, mAccounts.get(0), query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected boolean isTransparent() {
        return true;
    }

    @Override
    protected void onDestroy() {
        mBillingModel.onDestroy();
        super.onDestroy();
    }
}
