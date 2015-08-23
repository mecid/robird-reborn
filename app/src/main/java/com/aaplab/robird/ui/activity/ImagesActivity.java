package com.aaplab.robird.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.aaplab.robird.R;
import com.aaplab.robird.ui.fragment.ImageFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by majid on 23.08.15.
 */
public class ImagesActivity extends BaseActivity {

    @Bind(R.id.pager)
    ViewPager mPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        final String[] images = getIntent().getStringArrayExtra("images");
        mPager.setAdapter(new ImagePageAdapter(images));
    }

    private final class ImagePageAdapter extends FragmentStatePagerAdapter {

        private String[] images;

        public ImagePageAdapter(String[] images) {
            super(getSupportFragmentManager());
            this.images = images;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFragment.create(images[position]);
        }

        @Override
        public int getCount() {
            return images.length;
        }
    }

    public static void start(Activity activity, String[] images) {
        Intent intent = new Intent(activity, ImagesActivity.class);
        intent.putExtra("images", images);
        ActivityCompat.startActivity(activity, intent, null);
    }
}
