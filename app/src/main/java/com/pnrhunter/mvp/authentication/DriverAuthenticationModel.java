package com.pnrhunter.mvp.authentication;

import android.content.Context;

import io.reactivex.Observable;

public class DriverAuthenticationModel implements AuthenticationInterface {
    private Context context;

    public DriverAuthenticationModel(Context c) {
        context = c;
    }

    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public Observable<Boolean> login() {
        return Observable.<Boolean>just(false);
    }
}
