package com.holler.app.utils;

import io.reactivex.Completable;

public interface MessageDisplayer {
    void showMessage(String message);
    Completable showCompletableMessage(String message);
}
