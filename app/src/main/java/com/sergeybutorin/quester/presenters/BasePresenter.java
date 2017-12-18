package com.sergeybutorin.quester.presenters;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Created by sergeybutorin on 18/12/2017.
 */

public abstract class BasePresenter<V> {

    private WeakReference<V> view;

    public void bindView(@NonNull V view) {
        this.view = new WeakReference<>(view);
    }

    public void unbindView() {
        this.view = null;
    }

    protected V view() {
        if (view == null) {
            return null;
        } else {
            return view.get();
        }
    }
}