package com.fl1ntdev.event.cancellable;

public interface Cancellable {

    boolean isCancelled();

    void cancel();

}