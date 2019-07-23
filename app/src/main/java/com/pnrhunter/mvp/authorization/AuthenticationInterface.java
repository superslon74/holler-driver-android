package com.pnrhunter.mvp.authorization;

import io.reactivex.Observable;

public interface AuthenticationInterface {
    boolean isLoggedIn();

    Observable<Boolean> login();

    Observable<Boolean> login(String login, String password);

    Observable<Boolean> checkEmailExists(String email);

    Observable<Object> register(RegistrationPresenter.RegistrationPendingCredentials credentials);

    String getAuthHeader();
}
