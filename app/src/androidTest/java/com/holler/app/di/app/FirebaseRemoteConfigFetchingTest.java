package com.holler.app.di.app;

import androidx.test.filters.SmallTest;

import com.holler.app.utils.UpdateChecker;
import com.orhanobut.logger.Logger;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@SmallTest
public class FirebaseRemoteConfigFetchingTest {

    @Test
    public void getConfig() throws InterruptedException {
//        CountDownLatch signal = new CountDownLatch(1);
//        new UpdateChecker()
//                .checkForNewVersion()
//                .doOnNext(updateNeeded -> {
//                    Logger.d("IS UPDATE NEEDED " + updateNeeded);
//                })
//                .subscribe();
//
//
//        signal.await();
    }
}
