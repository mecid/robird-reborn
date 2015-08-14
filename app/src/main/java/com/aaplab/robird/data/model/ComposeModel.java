package com.aaplab.robird.data.model;

import android.content.ContentResolver;
import android.net.Uri;

import com.aaplab.robird.data.entity.Account;
import com.aaplab.robird.inject.Inject;
import com.aaplab.robird.util.ImageUtils;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;

/**
 * Created by majid on 01.08.15.
 */
public class ComposeModel extends BaseTwitterModel {

    private final ContentResolver contentResolver = Inject.contentResolver();

    public ComposeModel(Account account) {
        super(account);
    }

    public Observable<Status> tweet(final StatusUpdate update) {
        return Observable.create(new Observable.OnSubscribe<Status>() {
            @Override
            public void call(Subscriber<? super Status> subscriber) {
                try {
                    subscriber.onNext(mTwitter.updateStatus(update));
                    subscriber.onCompleted();
                } catch (TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<UploadedMedia> upload(final ArrayList<Uri> images) {
        return Observable.create(new Observable.OnSubscribe<UploadedMedia>() {
            @Override
            public void call(Subscriber<? super UploadedMedia> subscriber) {
                try {
                    for (Uri image : images) {
                        File imageTempFile = tempFile(image);
                        UploadedMedia uploadedMedia = mTwitter.uploadMedia(imageTempFile);
                        subscriber.onNext(uploadedMedia);
                    }
                    subscriber.onCompleted();
                } catch (IOException | TwitterException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private File tempFile(Uri uri) throws IOException {
        InputStream raw = contentResolver.openInputStream(uri);
        InputStream is = ImageUtils.sampleBitmap(raw, 600, 800);
        File tempFile = File.createTempFile("stream2file", ".tmp");
        tempFile.deleteOnExit();
        ByteStreams.copy(is, new FileOutputStream(tempFile));
        return tempFile;
    }
}