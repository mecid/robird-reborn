package com.aaplab.robird.ui.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.aaplab.robird.R;
import com.aaplab.robird.inject.Inject;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by majid on 23.08.15.
 */
public class ImageFragment extends BaseFragment implements View.OnTouchListener, ImageViewTouch.OnImageViewTouchSingleTapListener {

    private static final int PERMISSION_REQUEST_CODE = 118;

    public static ImageFragment create(String image) {
        Bundle args = new Bundle();
        args.putString("image", image);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private ImageViewTouch mImageView;
    private String mImage;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImage = getArguments().getString("image");

        Picasso.with(getActivity())
                .load(mImage)
                .into(mImageView);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.image, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveToGallery();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveToGallery();
        }
    }

    private void saveToGallery() {
        Picasso.with(getActivity()).load(mImage).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                MediaStore.Images.Media.insertImage(Inject.contentResolver(), bitmap, "", "");
                Snackbar.make(getActivity().findViewById(R.id.coordinator),
                        R.string.image_saved, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mImageView = new ImageViewTouch(getActivity(), null);
        mImageView.setOnTouchListener(this);
        mImageView.setSingleTapListener(this);
        mImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return mImageView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mImageView.getParent().requestDisallowInterceptTouchEvent(mImageView.getScale() > 1f);
        return false;
    }

    @Override
    public void onSingleTapConfirmed() {
        ActivityCompat.finishAfterTransition(getActivity());
    }
}
