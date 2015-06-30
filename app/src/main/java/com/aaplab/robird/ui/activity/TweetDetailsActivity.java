package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.ui.fragment.TweetDetailsFragment;

/**
 * Created by majid on 21.06.15.
 */
public class TweetDetailsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            final Account account = (Account) getIntent().getSerializableExtra("account");
            final Tweet tweet = (Tweet) getIntent().getSerializableExtra("tweet");

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container,
                            TweetDetailsFragment.create(account, tweet))
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        ActivityCompat.finishAfterTransition(this);
        return true;
    }

    public static void start(Activity activity, Account account, Tweet tweet) {
        Intent intent = new Intent(activity, TweetDetailsActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("tweet", tweet);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
