package dev.felnull.itts.core.savedata.impl;

import java.util.concurrent.atomic.AtomicInteger;

final class ErrorCounter {

    /**
     * 許容するエラーの数
     */
    private static final int MAX_NB_ERROR = 10;

    /**
     * カウンター
     */
    private final AtomicInteger counter = new AtomicInteger();

    private final Runnable errorCallback;

    ErrorCounter(Runnable errorCallback) {
        this.errorCallback = errorCallback;
    }


    void inc() {
        if (counter.incrementAndGet() == MAX_NB_ERROR) {
            errorCallback.run();
        }
    }
}
