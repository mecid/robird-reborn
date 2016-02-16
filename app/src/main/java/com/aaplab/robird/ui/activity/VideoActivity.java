package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aaplab.robird.R;
import com.aaplab.robird.ui.fragment.VideoFragment;

/**
 * Created by majid on 14.02.16.
 */
public class VideoActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_with_fab);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.fab).setVisibility(View.GONE);

        if (savedInstanceState == null) {
            final String video = getIntent().getStringExtra("video");

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, VideoFragment.create(video))
                    .commit();
        }
    }

    public static void start(Activity activity, String video) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("video", video);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
