package com.aaplab.robird.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;

import com.aaplab.robird.R;
import com.aaplab.robird.ui.fragment.SettingsFragment;

import butterknife.ButterKnife;

/**
 * Created by majid on 28.08.15.
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_with_fab);
        setSupportActionBar(ButterKnife.<Toolbar>findById(this, R.id.toolbar));
        ButterKnife.<CoordinatorLayout>findById(this, R.id.coordinator)
                .removeView(ButterKnife.findById(this, R.id.fab));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        if (savedInstanceState == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new SettingsFragment())
                    .commit();
    }
}
