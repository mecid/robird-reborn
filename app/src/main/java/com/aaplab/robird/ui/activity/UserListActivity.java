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
import com.aaplab.robird.data.entity.UserList;
import com.aaplab.robird.ui.fragment.ComposeFragment;
import com.aaplab.robird.ui.fragment.TimelineFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 17.08.15.
 */
public class UserListActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.fab)
    FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_with_fab);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        final Account account = getIntent().getParcelableExtra("account");
        final UserList userList = getIntent().getParcelableExtra("list");

        setTitle(userList.name());
        mFloatingActionButton.setOnClickListener(this);
        mFloatingActionButton.setImageResource(R.drawable.ic_edit);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,
                            TimelineFragment.create(account, userList.listId()))
                    .commit();
        }
    }

    @Override
    public void onClick(View v) {
        ComposeFragment.create((Account) getIntent().getParcelableExtra("account"))
                .show(getSupportFragmentManager(), ComposeFragment.TAG_COMPOSE);
    }

    public static void start(Activity activity, Account account, UserList userList) {
        Intent intent = new Intent(activity, UserListActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("list", userList);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
