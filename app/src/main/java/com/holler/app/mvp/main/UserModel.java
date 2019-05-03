package com.holler.app.mvp.main;

import android.content.Context;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.register.RegisterPresenter;
import com.orhanobut.logger.Logger;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import retrofit2.HttpException;
import retrofit2.Response;

public class UserModel {

    private RetrofitModule.ServerAPI serverAPI;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;

    public UserModel(RetrofitModule.ServerAPI serverAPI,
                     UserStorageModule.UserStorage userStorage,
                     DeviceInfoModule.DeviceInfo deviceInfo) {

        this.serverAPI = serverAPI;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
    }

    public boolean isLoggedIn() {
        return userStorage.getLoggedIn();
    }

    public static class Status {
        public enum AccountStatus {UNKNOWN, DISAPPROVED, NEW, APPROVED, BLOCKED}

        public enum ServiceStatus {UNKNOWN, OFFLINE, ONLINE}

        public AccountStatus account;
        public ServiceStatus service;

        public Status(AccountStatus account, ServiceStatus service) {
            this.account = account;
            this.service = service;
        }

        public Status() {
            this(AccountStatus.UNKNOWN, ServiceStatus.UNKNOWN);
        }

        public Status(RetrofitModule.ServerAPI.CheckStatusResponse statusData) {
            ServiceStatus newServiceStatus;
            switch (statusData.serviceStatus) {
                case "active":
                    newServiceStatus = ServiceStatus.ONLINE;
                    break;
                case "offline":
                    newServiceStatus = ServiceStatus.OFFLINE;
                    break;
                default:
                    newServiceStatus = ServiceStatus.UNKNOWN;
            }

            AccountStatus newAccountStatus;
            switch (statusData.accountStatus) {
                case "approved":
                    newAccountStatus = AccountStatus.APPROVED;
                    break;
                case "new":
                    newAccountStatus = AccountStatus.NEW;
                    break;
                case "onboarding":
                    newAccountStatus = AccountStatus.DISAPPROVED;
                    break;
                case "banned":
                    newAccountStatus = AccountStatus.BLOCKED;
                    break;
                default:
                    newAccountStatus = AccountStatus.UNKNOWN;
            }
            this.account = newAccountStatus;
            this.service = newServiceStatus;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            try {
                Status to = (Status) obj;
                return to.service == this.service && to.account == this.account;
            } catch (ClassCastException e) {
                return false;
            }
        }
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    private Status currentStatus = new Status();

    public PublishSubject<Status> statusSource = PublishSubject.create();

    public void updateStatus(RetrofitModule.ServerAPI.CheckStatusResponse statusData) {

        Status newStatus = new Status(statusData);

        if (!currentStatus.equals(newStatus)) {
            currentStatus = newStatus;
            statusSource.onNext(newStatus);
        }
    }


    public void goOffline() {
        serverAPI
                .sendStatus(getAuthHeader(), RetrofitModule.ServerAPI.STATUS_OFFLINE)
                .doOnSuccess(jsonObject -> {
                    if (currentStatus.service != Status.ServiceStatus.OFFLINE) {
                        currentStatus = new Status(currentStatus.account, Status.ServiceStatus.OFFLINE);
                        statusSource.onNext(currentStatus);
                    }
                    Logger.i("On send status success");
                })
                .doOnError(throwable -> {
                    Logger.e(throwable.getMessage());
                })
                .subscribeOn(Schedulers.io())
                .subscribe();

    }

    public void goOnline() {
        serverAPI
                .sendStatus(getAuthHeader(), RetrofitModule.ServerAPI.STATUS_ONLINE)
                .doOnSuccess(jsonObject -> {
                    if (currentStatus.service != Status.ServiceStatus.ONLINE) {
                        currentStatus = new Status(currentStatus.account, Status.ServiceStatus.ONLINE);
                        statusSource.onNext(currentStatus);
                    }
                    Logger.i("On send status success");
                })
                .doOnError(throwable -> {
                    throw ParsedThrowable.parse(throwable);
                })
                .subscribe();
    }

    public Subject<Boolean> logout() {
        final Subject<Boolean> source = PublishSubject.create();

        User storedUser = userStorage.getUser();

        serverAPI
                .logout(getAuthHeader(), new RetrofitModule.ServerAPI.LogoutRequestBody(storedUser.id))
                .doOnSuccess(jsonObject -> {
                    //TODO: remove user from storage
                    userStorage.setLoggedIn("false");
                    source.onNext(true);
                    source.onComplete();
                })
                .doOnError(throwable -> {
                    source.onError(ParsedThrowable.parse(throwable));
                    source.onComplete();
                })
                .subscribe();


        return source;
    }

    public Subject<Boolean> login() {
        User user = userStorage.getUser();
        return login(user.email, user.password);
    }

    public Subject<Boolean> login(String email, String password) {
        final Subject<Boolean> source = PublishSubject.create();

        Single<RetrofitModule.ServerAPI.AccessTokenResponseBody> accessTokenSingle
                = serverAPI.getAccessToken(
                new RetrofitModule
                        .ServerAPI
                        .AccessTokenRequestBody(
                        email,
                        password,
                        deviceInfo.deviceType,
                        deviceInfo.deviceId,
                        deviceInfo.deviceToken
                ))
                .doOnSubscribe(disposable -> {
                    User u = new User();
                    u.password = password;
                    u.email = email;
                    userStorage.putUser(u);
                    source.onSubscribe(disposable);
                })
                .doOnSuccess(accessTokenResponseBody -> {
                    userStorage.setLoggedIn("true");
                    userStorage.setAccessToken(accessTokenResponseBody.token);
                });



        accessTokenSingle
                .flatMap(
                        (Function<RetrofitModule.ServerAPI.AccessTokenResponseBody, SingleSource<?>>) accessTokenResponseBody
                                ->
                                serverAPI.getUserProfile(getAuthHeader())
                                        .doOnSuccess(user1 -> {
                                            userStorage.putUser(user1);
                                            source.onNext(true);
                                            source.onComplete();
                                        }))
                .doOnError(throwable -> {
                    source.onError(ParsedThrowable.parse(throwable));
                    source.onComplete();
                })
                .subscribe();

        return source;
    }

    public Subject<Boolean> checkEmailExists(String email) {
        Subject<Boolean> source = PublishSubject.create();

        serverAPI
                .checkEmailExists(new RetrofitModule.ServerAPI.EmailVerificationRequestBody(email))
                .doOnError(throwable -> {
                    ParsedThrowable error = ParsedThrowable.parse(throwable);
                    try {
                        if (((HttpException) error.origin).code() == 422) {
                            String message = AndarApplication.getInstance().getApplicationContext().getString(R.string.error_response_email_exists);
                            error = new ParsedThrowable(message, throwable);
                        }
                    } catch (ClassCastException e) {
                    }

                    source.onError(error);
                    source.onComplete();

                })
                .doOnSuccess(jsonObject -> {
                    source.onNext(true);
                    source.onComplete();
                })
                .subscribe();

        return source;
    }

    public Subject<Boolean> register(RegisterPresenter.RegistrationPendingCredentials credentials) {
        final Subject<Boolean> source = PublishSubject.create();

        final User user = new User();

        user.deviceType = deviceInfo.deviceType;
        user.deviceId = deviceInfo.deviceId;
        user.deviceToken = deviceInfo.deviceToken;
        user.loggedBy = "manual";
        //TODO: remove name duplication
        user.firstName = credentials.name;
        user.lastName = credentials.name;
        user.gender = credentials.gender;
        user.mobile = credentials.mobile;

        user.email = credentials.email;
        user.password = credentials.password;
        user.passwordConfirmation = credentials.passwordConfirmation;

        serverAPI
                .register(user)
                .doOnSuccess(jsonObject -> {
                    source.onNext(true);
                    source.onComplete();
                })
                .doOnError(throwable -> {
                    ParsedThrowable error = ParsedThrowable.parse(throwable);
                    try {
                        int code = ((HttpException) error.origin).code();
                        switch (code) {
                            case 422:
                                String emailExistsMessage = AndarApplication.getInstance().getApplicationContext().getString(R.string.error_response_email_exists);
                                error = new ParsedThrowable(emailExistsMessage, throwable);
                                break;

                            case 403:
                                String phoneExistsMessage = AndarApplication.getInstance().getApplicationContext().getString(R.string.error_response_phone_exists);
                                error = new ParsedThrowable(phoneExistsMessage, throwable);
                                break;
                        }
                    } catch (ClassCastException e) {
                    }

                    source.onError(error);
                    source.onComplete();
                })
                .subscribe();


        return source;
    }


    public static class ParsedThrowable extends Exception {
        public Throwable origin;

        public ParsedThrowable(String message, Throwable origin) {
            super(message);
            this.origin = origin;
        }

        public static ParsedThrowable parse(Throwable throwable) {
            return parse(throwable, AndarApplication.getInstance().getApplicationContext());
        }

        public static ParsedThrowable parse(Throwable throwable, Context context) {
            if (throwable instanceof HttpException) {
                switch (((HttpException) throwable).code()) {
                    case 400:
                    case 405:
                    case 500:
                        return new ParsedThrowable(context.getString(R.string.error_response_something_wrong), throwable);
                    case 422:
                        return new ParsedThrowable(context.getString(R.string.error_response_try_again), throwable);
                    case 503:
                        return new ParsedThrowable(context.getString(R.string.error_response_unreachable_server), throwable);
                    case 401:
                        return new ParsedThrowable(context.getString(R.string.error_response_unauthenticated), throwable);
                }
            }

            return new ParsedThrowable(context.getString(R.string.error_unexpected_error), throwable);
        }
    }


    public String getAuthHeader() {
        return "Bearer " + userStorage.getAccessToken();
    }
}
