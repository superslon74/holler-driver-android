package com.pnrhunter.mvp.authentication;

import io.reactivex.Observable;

public interface AuthenticationInterface {
    public boolean isLoggedIn();

    public Observable<Boolean> login();

}
