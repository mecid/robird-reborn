package com.aaplab.robird.ui.activity;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
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

import com.aaplab.robird.Analytics;
import com.aaplab.robird.R;
import com.aaplab.robird.UpdateReceiver;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.BillingModel;
import com.aaplab.robird.data.model.TimelineModel;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.DirectsFragment;
import com.aaplab.robird.ui.fragment.TimelineFragment;
import com.aaplab.robird.ui.fragment.TrendsFragment;
import com.aaplab.robird.ui.fragment.UserListsFragment;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.NavigationUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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

    @Bind({R.id.avatar, R.id.avatar2, R.id.avatar3})
    ImageView[] avatars;

    @Bind(R.id.user_background)
    ImageView mBackgroundImageView;

    @Bind(R.id.screen_name)
    TextView mScreenNameTextView;

    @Bind(R.id.full_name)
    TextView mFullNameTextView;

    @Bind(R.id.add_account_button)
    ImageView mAddAccountImageView;

    @Bind(R.id.navigation)
    NavigationView mNavigationView;

    @Icicle
    int mSelectedNavigationMenuId;

    @Icicle
    int mSelectedAccountId;

    private final Handler mNavigationHandler = new Handler();
    private ActionBarDrawerToggle mDrawerToggle;
    private AccountModel mAccountModel;
    private BillingModel mBillingModel;
    private List<Account> mAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mFloatingActionButton.setOnClickListener(this);
        mAddAccountImageView.setOnClickListener(this);
        avatars[0].setOnClickListener(this);
        avatars[1].setOnClickListener(this);
        avatars[2].setOnClickListener(this);

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

                                mSelectedNavigationMenuId = R.id.navigation_item_mentions;
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
        if (!Once.beenDone(Once.THIS_APP_VERSION, "update_receiver")) {
            sendBroadcast(new Intent(this, UpdateReceiver.class));
            Once.markDone("update_receiver");
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
            setTitle(menuItem.getTitle());
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();

            if (mSelectedNavigationMenuId != menuItem.getItemId() ||
                    mSelectedAccountId != mAccounts.get(0).id()) {

                mSelectedNavigationMenuId = menuItem.getItemId();
                mSelectedAccountId = mAccounts.get(0).id();

                mNavigationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.content, fragmentForNavigationItem(menuItem))
                                .commitAllowingStateLoss();
                    }
                }, 200);
            }
        } else {
            ActivityCompat.startActivity(this, new Intent(this, SettingsActivity.class), null);
        }

        return true;
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

        mScreenNameTextView.setText("@" + activeAccount.screenName());
        mFullNameTextView.setText(activeAccount.fullName());

        Glide.with(this)
                .load(activeAccount.userBackground())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mBackgroundImageView);

        for (int i = 0; i < mAccounts.size(); ++i) {
            Account account = mAccounts.get(i);
            Glide.with(this)
                    .load(account.avatar())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(avatars[i]);
            avatars[i].setVisibility(View.VISIBLE);
        }

        MenuItem navigationItem = mNavigationView.getMenu().findItem(mSelectedNavigationMenuId);
        onNavigationItemSelected(navigationItem == null ?
                mNavigationView.getMenu().findItem(R.id.navigation_item_home) : navigationItem);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private Fragment fragmentForNavigationItem(MenuItem navigationMenuItem) {
        if (navigationMenuItem.getItemId() == R.id.navigation_item_home) {
            return TimelineFragment.create(mAccounts.get(0), TimelineModel.HOME_ID);
        } else if (navigationMenuItem.getItemId() == R.id.navigation_item_mentions) {
            return TimelineFragment.create(mAccounts.get(0), TimelineModel.MENTIONS_ID);
        } else if (navigationMenuItem.getItemId() == R.id.navigation_item_retweets) {
            return TimelineFragment.create(mAccounts.get(0), TimelineModel.RETWEETS_ID);
        } else if (navigationMenuItem.getItemId() == R.id.navigation_item_favorites) {
            return TimelineFragment.create(mAccounts.get(0), TimelineModel.FAVORITES_ID);
        } else if (navigationMenuItem.getItemId() == R.id.navigation_item_lists) {
            return UserListsFragment.create(mAccounts.get(0));
        } else if (navigationMenuItem.getItemId() == R.id.navigation_item_directs) {
            return DirectsFragment.create(mAccounts.get(0));
        } else if (navigationMenuItem.getItemId() == R.id.navigation_item_world) {
            return TrendsFragment.create(mAccounts.get(0));
        }

        throw new IllegalArgumentException("There is no fragment for " + navigationMenuItem.getTitle());
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
            ComposeFragment
                    .share(reader.getText().toString())
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
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
