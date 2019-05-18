package com.holler.app.mvp.main;

import android.content.Context;
import android.net.wifi.aware.WifiAwareSession;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.User;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.register.RegisterPresenter;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class UserModel {
    private static final String LOG_TAG = "UserModel";

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

    public User getProfileData(){
        return userStorage.getUser();
    }

    public boolean isLoggedIn() {
        return userStorage.getLoggedIn();
    }

    public void clearState() {
        currentStatus=new Status();
    }

    public Subject<Boolean> updateProfile(User profile) {
        Subject<Boolean> source = PublishSubject.create();

        MultipartBody.Part fileData;
        if(profile.avatar!=null){
            File photo = new File(profile.avatar);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), photo);
            String filename = photo.getName();
            fileData = MultipartBody.Part.createFormData("avatar", filename, requestFile);
        }else{
            fileData = MultipartBody.Part.createFormData("avatar","");
        }

        serverAPI
                .updateProfile(
                        getAuthHeader(),
                        profile.firstName,
                        profile.lastName,
                        profile.email,
                        profile.mobile,
                        profile.gender,
                        fileData
                )
                .flatMap(jsonObject -> {
                    return serverAPI.getUserProfile(getAuthHeader())
                            .doOnSuccess(user1 -> {
                                userStorage.putUser(user1);
                            });
                })
                .doOnSuccess(jsonObject -> {
                    source.onNext(true);
                    source.onComplete();
                })
                .doOnError(throwable -> {
                    source.onError(throwable);
                    source.onComplete();
                })
                .subscribe();



        return source;
    }

    public static class Status {
        public enum AccountStatus {UNKNOWN, DISAPPROVED, NEW, APPROVED, BLOCKED}

        public enum ServiceStatus {UNKNOWN, OFFLINE, ONLINE, RIDING}

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

            this.account = extractAccountStatus(statusData);
            this.service = extractServiceStatus(statusData);
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

    public static Status.AccountStatus extractAccountStatus(RetrofitModule.ServerAPI.CheckStatusResponse statusData){
        Status.AccountStatus newAccountStatus;
        switch (statusData.accountStatus) {
            case "approved":
                newAccountStatus = Status.AccountStatus.APPROVED;
                break;
            case "new":
                newAccountStatus = Status.AccountStatus.NEW;
                break;
            case "onboarding":
                newAccountStatus = Status.AccountStatus.DISAPPROVED;
                break;
            case "banned":
                newAccountStatus = Status.AccountStatus.BLOCKED;
                break;
            default:
                newAccountStatus = Status.AccountStatus.UNKNOWN;
        }
        return newAccountStatus;
    }

    public static Status.ServiceStatus extractServiceStatus(RetrofitModule.ServerAPI.CheckStatusResponse statusData){
        Status.ServiceStatus newServiceStatus;
        switch (statusData.serviceStatus) {
            case "active":
                newServiceStatus = Status.ServiceStatus.ONLINE;
                break;
            case "offline":
                newServiceStatus = Status.ServiceStatus.OFFLINE;
                break;
            case "riding":
                newServiceStatus = Status.ServiceStatus.RIDING;
                break;
            default:
                newServiceStatus = Status.ServiceStatus.UNKNOWN;
        }
        return newServiceStatus;
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
        Crashlytics.log(Log.DEBUG, LOG_TAG, "Login: starting (email: "+email+"; password: "+password+")");
        Observable
                .timer(0, TimeUnit.SECONDS)
                .flatMap(aLong -> {
                    return getFirebaseToken();
                })
                .flatMap(firebaseToken -> {
                    Crashlytics.setString("TOKEN firebase", firebaseToken);
                    deviceInfo.deviceToken = firebaseToken;
                    return serverAPI.getAccessToken(
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
                                Crashlytics.log(Log.DEBUG, LOG_TAG, "Login: access token received");
                                userStorage.setLoggedIn("true");
                                userStorage.setAccessToken(accessTokenResponseBody.token);
                                Crashlytics.setString("TOKEN access", accessTokenResponseBody.token);
                            })
                            .toObservable();
                })
                .flatMap(accessToken -> {
                    return serverAPI.getUserProfile(getAuthHeader())
                            .doOnSuccess(user1 -> {
                                Crashlytics.log(Log.DEBUG, LOG_TAG, "Login: profile received");
                                Crashlytics.setString("USER name", user1.firstName+" "+user1.lastName);
                                Crashlytics.setString("USER email", user1.email);
                                userStorage.putUser(user1);
                                source.onNext(true);
                                source.onComplete();
                            })
                            .toObservable();
                })
                .doOnError(throwable -> {
                    Crashlytics.log(Log.DEBUG, LOG_TAG, "Login: ERROR - "+throwable.getMessage());
                    source.onError(ParsedThrowable.parse(throwable));
                    source.onComplete();
                })
                .subscribe();


        return source;
    }

    private Subject<String> getFirebaseToken(){
        Crashlytics.log(Log.DEBUG, LOG_TAG, "Get firebase token: starting process");

        Subject<String> result = PublishSubject.create();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Logger.w("getInstanceId failed", task.getException());
                            Crashlytics.log(Log.DEBUG, LOG_TAG, "Get firebase token: unsuccessful");
                            result.onNext("Could not get firebase token");
                            result.onComplete();
                            return;
                        }
                        String token = task.getResult().getToken();
                        Crashlytics.log(Log.DEBUG, LOG_TAG, "Get firebase token: successful");

                        result.onNext(token);
                        result.onComplete();
                    }
                });

        return result;
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
        user.lastName = credentials.lastName;
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
