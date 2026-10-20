package com.sanedge.pointofsale.auth.handler;

import io.grpc.stub.StreamObserver;

/** Minimal capturing {@link StreamObserver} used by the handler unit tests. */
public class TestStreamObserver<T> implements StreamObserver<T> {

    private T value;
    private Throwable error;
    private boolean completed;

    @Override
    public void onNext(T v) {
        this.value = v;
    }

    @Override
    public void onError(Throwable t) {
        this.error = t;
    }

    @Override
    public void onCompleted() {
        this.completed = true;
    }

    public T value() {
        return value;
    }

    public Throwable error() {
        return error;
    }

    public boolean isCompleted() {
        return completed;
    }
}
