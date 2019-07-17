package com.pnrhunter.mvp.utils.activity;

import io.reactivex.Completable;

public interface MessageDisplayer {
    void showMessage(String message);
    Completable showCompletableMessage(String message);
}
