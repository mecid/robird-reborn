package com.aaplab.robird.ui.activity;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.TimelineFragment;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.NavigationUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icicle;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by majid on 07.05.15.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

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
        avatars[1].setOnClickListener(this);
        avatars[2].setOnClickListener(this);

        mAccountModel = new AccountModel();
        mSubscriptions.add(
                mAccountModel
                        .accounts()
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
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        if (menuItem.getItemId() != R.id.navigation_item_settings) {
            Timber.d("on navigation item selected: %s", menuItem.getTitle());
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
                                .replace(R.id.content,
                                        TimelineFragment.create(mAccounts.get(0), menuItem.getOrder()))
                                .commit();
                    }
                }, 200);
            }
        } else {
            //TODO start settings activity
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mAddAccountImageView) {
            if (mAccounts.size() < 3) {
                NavigationUtils.changeDefaultActivityToSignIn(this, true);
                startActivity(new Intent(this, SignInActivity.class));
                finish();
            } else {
                Timber.d("max account count must be 3");
            }
        } else if (avatars[1] == v) {
            Account selectedAccount = mAccounts.get(1);
            activate(selectedAccount);
        } else if (avatars[2] == v) {
            Account selectedAccount = mAccounts.get(2);
            activate(selectedAccount);
        } else if (mFloatingActionButton == v) {
            ComposeFragment.create(mAccounts.get(0))
                    .show(getSupportFragmentManager(), ComposeFragment.TAG_COMPOSE);
        }
    }

    private void setupNavigation(List<Account> accounts) {
        mAccounts = accounts;
        Account activeAccount = mAccounts.get(0);

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

    private void activate(final Account selectedAccount) {
        mSubscriptions.add(
                mAccountModel
                        .activate(selectedAccount)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe()
        );
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
}
