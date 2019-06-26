package com.pnrhunter.di.app;

import androidx.test.filters.LargeTest;

import com.orhanobut.logger.Logger;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;

@LargeTest
public class RxIntervalErrorCaseTest {

    private static final long TIME_LIMIT = 4;
    private static final long DEFAULT_TIME = 10;

    @Test
    public void runChain() throws InterruptedException {
        CountDownLatch signal = new CountDownLatch(1);
        Observable
                .interval(1, TimeUnit.SECONDS)
                .flatMap(time -> {
                    return Observable.<Long>create(emitter -> {
                        try {
                            wrapTime(time)
                                    .doOnNext(result -> {
                                        emitter.onNext(result);
                                    })
                                    .subscribe();
                        }catch (TimeoutException e){
                            emitter.onNext(DEFAULT_TIME);
                        }
                    });
                })

                .flatMap(limitedTime -> {
                    Logger.i("TIME" + limitedTime);
                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    Logger.e(throwable.getMessage(), throwable);
                })
                .subscribe();
        signal.await();
    }

    private Observable<Long> wrapTime(long time) throws TimeoutException {
        if (time > TIME_LIMIT) {
            throw new TimeoutException("Time out");
        }
        return Observable.just(time);
    }

}
