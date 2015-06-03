/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aaplab.robird.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

/**
 * A lightweight wrapper around {@link ContentResolver} which allows for continuously observing
 * the result of a query.
 */
public final class SqlBriteContentProvider {
    public static SqlBriteContentProvider create(@NonNull ContentResolver contentResolver) {
        return new SqlBriteContentProvider(contentResolver);
    }

    private final Handler contentObserverHandler = new Handler(Looper.getMainLooper());
    private final ContentResolver contentResolver;

    private SqlBriteContentProvider(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    /**
     * Create an observable which will notify subscribers with a {@linkplain Query query} for
     * execution. Subscribers are responsible for <b>always</b> closing {@link Cursor} instance
     * returned from the {@link Query}.
     * <p/>
     * Subscribers will receive an immediate notification for initial data as well as subsequent
     * notifications for when the supplied {@code uri}'s data changes. Unsubscribe when you no longer
     * want updates to a query.
     * <p/>
     * <b>Warning:</b> this method does not perform the query! Only by subscribing to the returned
     * {@link Observable} will the operation occur.
     *
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     * @see ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)
     */
    public Observable<Query> query(@NonNull final Uri uri, @Nullable final String[] projection,
                                   @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable
                                   final String sortOrder, final boolean notifyForDescendents) {
        final Query query = new Query() {
            @Override
            public Cursor run() {
                return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
            }
        };
        return Observable.create(new Observable.OnSubscribe<Query>() {
            @Override
            public void call(final Subscriber<? super Query> subscriber) {
                final ContentObserver observer = new ContentObserver(contentObserverHandler) {
                    @Override
                    public void onChange(boolean selfChange) {
                        Timber.d("QUERY\n  uri: %s\n  projection: %s\n  selection: %s\n  selectionArgs: %s\n  "
                                        + "sortOrder: %s\n  notifyForDescendents: %s", uri,
                                Arrays.toString(projection), selection, Arrays.toString(selectionArgs), sortOrder,
                                notifyForDescendents);
                        subscriber.onNext(query);
                    }
                };
                contentResolver.registerContentObserver(uri, notifyForDescendents, observer);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        contentResolver.unregisterContentObserver(observer);
                    }
                }));
            }
        }).startWith(query);
    }

    public Observable<Uri> insert(@NonNull final Uri uri, @NonNull final ContentValues values) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                subscriber.onNext(contentResolver.insert(uri, values));
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Integer> update(@NonNull final Uri uri, @NonNull final ContentValues values,
                                      @Nullable final String where, @Nullable final String[] args) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(contentResolver.update(uri, values, where, args));
                subscriber.onCompleted();
            }
        });
    }

    public Observable<Integer> delete(@NonNull final Uri uri, @Nullable final String where,
                                      @Nullable final String[] args) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(contentResolver.delete(uri, where, args));
                subscriber.onCompleted();
            }
        });
    }

    /**
     * An executable query.
     */
    public interface Query {
        /**
         * Execute the query on the underlying database and return the resulting cursor.
         */
        Cursor run();
    }
}