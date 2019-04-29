package com.holler.app.mvp.main;

import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.orhanobut.logger.Logger;

import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

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


    public static class Status{
        public enum AccountStatus {UNKNOWN, DISAPPROVED, NEW, APPROVED, BLOCKED}
        public enum ServiceStatus {UNKNOWN, OFFLINE, ONLINE}

        public AccountStatus account;
        public ServiceStatus service;

        public Status(AccountStatus account, ServiceStatus service) {
            this.account = account;
            this.service = service;
        }

        public Status(){
            this(AccountStatus.UNKNOWN, ServiceStatus.UNKNOWN);
        }

        public Status(RetrofitModule.ServerAPI.CheckStatusResponse statusData){
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
            try{
                Status to = (Status)obj;
                return to.service==this.service && to.account==this.account;
            }catch (ClassCastException e){
                return false;
            }
        }
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
                .sendStatus(getAuthHeader(),RetrofitModule.ServerAPI.STATUS_OFFLINE)
                .doOnSuccess(jsonObject -> {
                    if(currentStatus.service!= Status.ServiceStatus.OFFLINE){
                        currentStatus = new Status(currentStatus.account, Status.ServiceStatus.OFFLINE);
                        statusSource.onNext(currentStatus);
                    }
                    Logger.i("On send status success");
                })
                .subscribe();

    }

    public void goOnline(){
        serverAPI
                .sendStatus(getAuthHeader(),RetrofitModule.ServerAPI.STATUS_ONLINE)
                .doOnSuccess(jsonObject -> {
                    if(currentStatus.service!= Status.ServiceStatus.ONLINE){
                        currentStatus = new Status(currentStatus.account, Status.ServiceStatus.ONLINE);
                        statusSource.onNext(currentStatus);
                    }
                    Logger.i("On send status success");
                })
                .subscribe();
    }

    public Subject<Boolean> logout(){
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
                    source.onNext(false);
                    source.onComplete();
                })
                .subscribe();


        return source;
    }

    public Subject<Boolean> login(){
        User user = userStorage.getUser();
        return login(user.email, user.password);
    }

    public Subject<Boolean> login(String email, String password){
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
                    source.onSubscribe(disposable);
                })
                .doOnSuccess(accessTokenResponseBody -> {
                    userStorage.setLoggedIn("true");
                    userStorage.setAccessToken(accessTokenResponseBody.token);
                });

        Single<User> userProfileSingle
                = serverAPI.getUserProfile(getAuthHeader())
                .doOnSuccess(newUser -> {
                    userStorage.putUser(newUser);
                });

        accessTokenSingle
                .doOnSuccess(accessTokenResponseBody -> {
                    userProfileSingle
                            .doOnSuccess(user1 -> {
                                source.onNext(true);
                                source.onComplete();
                            })
                            .doOnError(throwable -> {
                                source.onNext(false);
                                source.onComplete();
                            })
                            .subscribeOn(Schedulers.io())
                            .subscribe();
                })
                .doOnError(throwable -> {
                    source.onNext(false);
                    source.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();

        return source;
    }


    public String getAuthHeader() {
        return "Bearer "+userStorage.getAccessToken();
    }
}
