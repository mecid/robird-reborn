package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.ui.fragment.TweetSearchFragment;
import com.aaplab.robird.ui.fragment.UserSearchFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 10.08.15.
 */
public class SearchActivity extends BaseActivity {

    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabs;

    private Account mAccount;
    private String mQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mAccount = getIntent().getParcelableExtra("account");
        mQuery = getIntent().getStringExtra("query");

        if (TextUtils.isEmpty(mQuery)) {
            mAccount = new AccountModel().accounts().toBlocking().first().get(0);
            mQuery = getIntent().getDataString();
            mQuery = mQuery.substring(mQuery.indexOf("#"));
        }

        setTitle(mQuery);

        final SearchPagerAdapter adapter = new SearchPagerAdapter(getSupportFragmentManager());
        mTabs.setTabsFromPagerAdapter(adapter);
        mTabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs));
        mViewPager.setAdapter(adapter);
    }

    public static void start(Activity activity, Account account, String query) {
        Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("query", query);
        ActivityCompat.startActivity(activity, intent, null);
    }

    public final class SearchPagerAdapter extends FragmentStatePagerAdapter {

        public SearchPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ?
                    getString(R.string.tweets) :
                    getString(R.string.users);
        }

        @Override
        public Fragment getItem(int position) {
            return position == 0 ?
                    TweetSearchFragment.create(mAccount, mQuery) :
                    UserSearchFragment.create(mAccount, mQuery);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
