package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.TweetDetailsFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 21.06.15.
 */
public class TweetDetailsActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    private Account mAccount;
    private Tweet mTweet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_with_fab);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mFloatingActionButton.setImageResource(R.drawable.ic_reply);
        mFloatingActionButton.setOnClickListener(this);

        if (savedInstanceState == null) {
            mAccount = getIntent().getParcelableExtra("account");
            mTweet = getIntent().getParcelableExtra("tweet");

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,
                            TweetDetailsFragment.create(mAccount, mTweet))
                    .commit();
        }
    }

    public static void start(Activity activity, Account account, Tweet tweet) {
        Intent intent = new Intent(activity, TweetDetailsActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("tweet", tweet);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @Override
    public void onClick(View v) {
        ComposeFragment.create(mAccount, mTweet)
                .show(getSupportFragmentManager(), ComposeFragment.TAG_REPLY);
    }
}
