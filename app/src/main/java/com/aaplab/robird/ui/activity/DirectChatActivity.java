package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aaplab.robird.Analytics;
import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.DirectChatFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 19.08.15.
 */
public class DirectChatActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    private String mUserName;
    private Account mAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_with_fab);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        mUserName = getIntent().getStringExtra("username");
        mAccount = getIntent().getParcelableExtra("account");

        mFloatingActionButton.setImageResource(R.drawable.ic_edit);
        mFloatingActionButton.setOnClickListener(this);
        setTitle("@" + mUserName);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, DirectChatFragment.create(mAccount, mUserName))
                    .commit();
        }
    }

    public static void start(Activity activity, Account account, String username) {
        Intent intent = new Intent(activity, DirectChatActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("username", username);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @Override
    public void onClick(View view) {
        Analytics.event(ComposeFragment.TAG_DIRECT);
        ComposeFragment.direct(mAccount, mUserName)
                .show(getSupportFragmentManager(), ComposeFragment.TAG_DIRECT);
    }
}
