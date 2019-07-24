package com.pnrhunter.mvp.authorization;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class TestAuthenticationModel implements AuthenticationInterface {
    private Context context;

    public TestAuthenticationModel(Context c) {
        context = c;
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public Observable<Boolean> login() {
        return Observable
                .timer(3, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    return Observable.just(false);
                });
    }

    @Override
    public Observable<Boolean> login(String login, String password) {
        return Observable
                .timer(3, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    return Observable.just(true);
                });
    }

    @Override
    public Observable<Boolean> checkEmailExists(String email) {
        return Observable
                .timer(3, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    return Observable.just(true);
                });
    }

    @Override
    public Observable<Object> register(RegistrationPresenter.RegistrationPendingCredentials credentials) {
        return Observable
                .timer(3, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    return Observable.just(new Object());
                });
    }

    @Override
    public String getAuthHeader() {
        return "TEST AUTH HEADER";
    }
}
