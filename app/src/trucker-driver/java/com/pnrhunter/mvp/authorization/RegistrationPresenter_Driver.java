package com.pnrhunter.mvp.authorization;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pnrhunter.mvp.utils.DeviceInfo;
import com.pnrhunter.mvp.utils.Validator;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.router.TestRouter;

import io.reactivex.Observable;

public class RegistrationPresenter_Driver extends RegistrationPresenter{
    public RegistrationPresenter_Driver(Context context, View view, AbstractRouter router, AuthenticationInterface auth, DeviceInfo deviceInfo, Validator validator) {
        super(context, view, router, auth, deviceInfo, validator);
    }

    @Override
    protected void doRequest() {
        Logger.d("request from DRIVER RegistrationPresenter");
        auth
                .checkEmailExists(credentials.email)
                .doOnSubscribe(disposable -> {
                    view.hideKeyboard();
                    view.showSpinner();
                })
                .flatMap(emailNotExist -> {
                    return super.verifyByPhone()
                            .doOnSubscribe(disposable -> {
                                view.hideSpinner();
                            })
                            .doFinally(() -> {
                                view.showSpinner();
                            });
                })//verify phone number
                .flatMap(aBoolean -> {
                    return auth
                            .register(credentials)
                            .doOnSubscribe(disposable -> {
                                view.showSpinner();
                            });
                })//send registration data
                .flatMap(timerFinished -> {
                    return auth
                            .login(credentials.email,credentials.password);
                })//login
                .flatMap(loggedIn -> {
                    router.goTo(TestRouter.ROUTE_DOCUMENTS);
                    return Observable.empty();
                })//go to documents
                .doOnError(throwable -> view.showMessage(throwable.getMessage()))
                .doFinally(() -> view.hideSpinner())
                .subscribe();
    }
}
