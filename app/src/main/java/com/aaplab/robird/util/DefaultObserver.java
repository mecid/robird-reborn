package com.aaplab.robird.util;

import rx.Observer;
import timber.log.Timber;

/**
 * Created by majid on 07.05.15.
 */
public abstract class DefaultObserver<T> implements Observer<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Timber.w(e, "");
    }

    @Override
    public void onNext(T t) {

    }
}
