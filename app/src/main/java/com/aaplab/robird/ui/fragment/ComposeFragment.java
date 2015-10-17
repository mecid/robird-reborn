package com.aaplab.robird.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.aaplab.robird.Analytics;
import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.data.entity.Contact;
import com.aaplab.robird.data.entity.Tweet;
import com.aaplab.robird.data.model.AccountModel;
import com.aaplab.robird.data.model.ComposeModel;
import com.aaplab.robird.data.model.ContactModel;
import com.aaplab.robird.data.model.DirectsModel;
import com.aaplab.robird.ui.activity.BaseActivity;
import com.aaplab.robird.ui.adapter.UsernameCompleteAdapter;
import com.aaplab.robird.util.DefaultObserver;
import com.aaplab.robird.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.twitter.Validator;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.Icicle;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.UploadedMedia;

/**
 * Created by majid on 30.07.15.
 */
public class ComposeFragment extends DialogFragment implements Toolbar.OnMenuItemClickListener, TextWatcher {

    public static final String TAG_COMPOSE = "Compose";
    public static final String TAG_DIRECT = "Direct";
    public static final String TAG_QUOTE = "Quote";
    public static final String TAG_REPLY = "Reply";
    public static final String TAG_SHARE = "Share";

    private static final int SELECT_PICTURE = 1743;

    public static ComposeFragment create(Account account) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);

        ComposeFragment fragment = new ComposeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeFragment reply(Account account, Tweet tweet) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        args.putParcelable("tweet", tweet);

        ComposeFragment fragment = new ComposeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeFragment direct(Account account, String username) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        args.putString("username", username);

        ComposeFragment fragment = new ComposeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static ComposeFragment share(String text) {
        Bundle args = new Bundle();
        args.putString("text", text);

        ComposeFragment fragment = new ComposeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ComposeFragment share(ArrayList<Uri> images) {
        Bundle args = new Bundle();
        args.putParcelableArrayList("images", images);

        ComposeFragment fragment = new ComposeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.avatar)
    ImageView mAvatarImageView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.text)
    MultiAutoCompleteTextView mEditText;

    @Bind(R.id.screen_name)
    TextView mScreenNameTextView;

    @Bind(R.id.full_name)
    TextView mFullNameTextView;

    @Bind({R.id.image1, R.id.image2, R.id.image3, R.id.image4})
    ImageView[] mImageViews;

    @Bind(R.id.images)
    LinearLayout mImagesLayout;

    @Icicle
    ArrayList<Uri> mAttachedImages;

    private Tweet mTweet;
    private Account mAccount;
    private String mUserName;
    private Validator mTweetValidator;
    private ContactModel mContactModel;
    private ComposeModel mComposeModel;
    private DirectsModel mDirectsModel;
    private WeakReference<BaseActivity> mActivityWeak;
    private Uri mCameraImageUri;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        mActivityWeak = new WeakReference<>((BaseActivity) getActivity());
        mAttachedImages = mAttachedImages == null ? new ArrayList<Uri>() : mAttachedImages;
        mAccount = getArguments().getParcelable("account");
        mAccount = mAccount != null ? mAccount : new AccountModel().accounts().toBlocking().first().get(0);
        mTweet = getArguments().getParcelable("tweet");
        mUserName = getArguments().getString("username");
        mComposeModel = new ComposeModel(mAccount);
        mDirectsModel = new DirectsModel(mAccount);
        mContactModel = new ContactModel(mAccount);
        mTweetValidator = new Validator();

        mScreenNameTextView.setText("@" + mAccount.screenName());
        mFullNameTextView.setText(mAccount.fullName());

        Glide.with(this)
                .load(mAccount.avatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mAvatarImageView);

        mToolbar.inflateMenu(R.menu.compose);
        mToolbar.setOnMenuItemClickListener(this);

        if (TextUtils.isEmpty(mUserName)) {
            mEditText.addTextChangedListener(this);
            mEditText.setText(getArguments().getString("text"));
            mEditText.setSelection(TextUtils.equals(getTag(), TAG_QUOTE) ? 0 : mEditText.length());
            mActivityWeak.get().compositeSubscription(
                    mContactModel
                            .contacts()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DefaultObserver<List<Contact>>() {
                                @Override
                                public void onNext(List<Contact> contacts) {
                                    super.onNext(contacts);
                                    mEditText.setTokenizer(new UsernameCompleteAdapter.SpaceTokenizer());
                                    mEditText.setAdapter(new UsernameCompleteAdapter(getActivity(), contacts));
                                }
                            })
            );
        } else {
            mToolbar.getMenu().findItem(R.id.menu_schedule).setVisible(false);
            mToolbar.getMenu().findItem(R.id.menu_camera).setVisible(false);
        }

        if (mTweet != null) {
            mEditText.setText(
                    String.format("@%s %s", mTweet.username(), mTweet.mentions())
                            .replaceAll("@" + mAccount.screenName(), "")
                            .trim().concat(" ")
            );
            mEditText.setSelection(mEditText.length());
        }

        final ArrayList<Uri> images = getArguments().getParcelableArrayList("images");
        if (mAttachedImages.isEmpty() && images != null)
            mAttachedImages.addAll(images);
        previewImages();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_compose, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_send) {
            Analytics.event(Analytics.SEND);
            if (TextUtils.isEmpty(mUserName)) {
                if (getTweetLength() == 0) {
                    Toast.makeText(getActivity(), R.string.type_something, Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (getTweetLength() > Validator.MAX_TWEET_LENGTH) {
                    Toast.makeText(getActivity(), R.string.tweet_is_too_long, Toast.LENGTH_SHORT).show();
                    return true;
                }

                final StatusUpdate statusUpdate = new StatusUpdate(mEditText.getText().toString());
                statusUpdate.setInReplyToStatusId(mTweet != null ? mTweet.tweetId() : -1L);

                if (mAttachedImages.isEmpty()) {
                    mActivityWeak.get().compositeSubscription(
                            mComposeModel
                                    .tweet(statusUpdate)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new DefaultObserver<Status>() {
                                        @Override
                                        public void onNext(Status status) {
                                            super.onNext(status);
                                            Snackbar.make(mActivityWeak.get().findViewById(R.id.coordinator),
                                                    R.string.successfully_tweeted, Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                    );
                } else {
                    mActivityWeak.get().compositeSubscription(
                            mComposeModel
                                    .upload(mAttachedImages)
                                    .buffer(mAttachedImages.size())
                                    .flatMap(new Func1<List<UploadedMedia>, Observable<Status>>() {
                                        @Override
                                        public Observable<Status> call(List<UploadedMedia> uploadedMedias) {
                                            long ids[] = new long[uploadedMedias.size()];

                                            for (int i = 0; i < uploadedMedias.size(); ++i)
                                                ids[i] = uploadedMedias.get(i).getMediaId();

                                            statusUpdate.setMediaIds(ids);
                                            return mComposeModel.tweet(statusUpdate);
                                        }
                                    })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new DefaultObserver<Status>() {
                                        @Override
                                        public void onNext(Status status) {
                                            super.onNext(status);
                                            Snackbar.make(mActivityWeak.get().findViewById(R.id.coordinator),
                                                    R.string.successfully_tweeted, Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                    );
                }

                dismiss();
            } else {
                mActivityWeak.get().compositeSubscription(
                        mDirectsModel
                                .send(mUserName, mEditText.getText().toString())
                                .flatMap(new Func1<DirectMessage, Observable<Integer>>() {
                                    @Override
                                    public Observable<Integer> call(DirectMessage directMessage) {
                                        return mDirectsModel.update();
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                );

                dismiss();
            }
        } else if (item.getItemId() == R.id.menu_camera) {
            Analytics.event(Analytics.ATTACH_IMAGE);
            // you can add only 4 images
            if (mAttachedImages.size() < 4) {
                try {
                    Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pickIntent.setType("image/*");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        pickIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    }

                    mCameraImageUri = Uri.fromFile(ImageUtils.createNewImageFile());
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);

                    Intent chooserIntent = Intent.createChooser(pickIntent, getString(R.string.app_name));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

                    startActivityForResult(chooserIntent, SELECT_PICTURE);
                } catch (IOException e) {
                    Timber.e(e, "Exception while starting image picker in compose screen.");
                }
            }
        } else if (item.getItemId() == R.id.menu_at) {
            mEditText.append("@");
        } else if (item.getItemId() == R.id.menu_hashtag) {
            mEditText.append("#");
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    //Camera
                    mAttachedImages.add(mCameraImageUri);
                } else {
                    Uri selectedImageUri = data.getData();
                    mAttachedImages.add(selectedImageUri);
                }

                previewImages();
                onTextChanged(mEditText.getText(), 0, 0, 0);
            }
        }
    }

    private int getTweetLength() {
        int length = mTweetValidator.getTweetLength(mEditText.getText().toString());
        return mAttachedImages.isEmpty() ? length : length + mTweetValidator.getShortUrlLengthHttps();
    }

    private void previewImages() {
        mImagesLayout.setVisibility(mAttachedImages.isEmpty() ? View.GONE : View.VISIBLE);

        for (ImageView imageView : mImageViews) {
            imageView.setImageDrawable(null);
        }

        for (int i = 0; i < mAttachedImages.size(); ++i) {
            Glide.with(this)
                    .load(mAttachedImages.get(i))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mImageViews[i]);

            final int finalI = i;
            mImageViews[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mAttachedImages.remove(finalI);
                    previewImages();
                    return true;
                }
            });
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mToolbar.setSubtitle(String.format("%d / 140", getTweetLength()));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
