package com.holler.app.di.app;

import android.app.Instrumentation;
import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.runner.AndroidJUnitRunner;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.DaggerAppComponent;
import com.holler.app.di.app.DaggerTestAppComponent;
import com.holler.app.di.app.TestApp;
import com.holler.app.di.app.TestAppComponent;
import com.holler.app.di.app.TestAppModule;
import com.holler.app.di.app.modules.AppModule;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.OrderModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.di.app.modules.SharedPreferencesModule;
import com.holler.app.di.app.modules.UserModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.holler.app.mvp.main.UserModel;
import com.orhanobut.logger.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;

@LargeTest
public class CreateOrderFromDriverTest {

    private static final String userEmail = "a@a.com";
    private static final String userPassword = "1aaaaaaaa";

    @Inject protected Context context;
    @Inject protected UserModel userModel;
    @Inject protected RetrofitModule.ServerAPI serverAPI;

    @Before
    public void setup() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context context = instrumentation.getContext();

        TestAppComponent component = DaggerTestAppComponent
                .builder()
                .testAppModule(new TestAppModule(context))
                .retrofitModule(new RetrofitModule())
                .sharedPreferencesModule(new SharedPreferencesModule())
                .deviceInfoModule(new DeviceInfoModule())
                .userStorageModule(new UserStorageModule())
                .routerModule(new RouterModule())
                .userModule(new UserModule())
                .orderModule(new OrderModule())
                .build();

        component.inject(context);
        component.inject(this);
    }



    @Test
    public void createAndSendOrder() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        Logger.d("CREATE ORDER TEST!");
        Stack<RetrofitModule.ServerAPI.CreateOrderRequestBody> orders = new Stack<>();
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.953094","31.995188"));
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.954094","31.995188"));
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.955094","31.995188"));
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.956094","31.995188"));
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.957094","31.995188"));
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.958094","31.995188"));
        orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.959094","31.995188"));


        loginUser()
                .doOnComplete(() -> {
                    Observable
                            .interval(5, TimeUnit.SECONDS)
                            .doOnNext(time -> {
                                try{
                                RetrofitModule.ServerAPI.CreateOrderRequestBody order = orders.pop();
                                serverAPI.createOrder(userModel.getAuthHeader(), order)
                                        .doFinally(() -> {
                                            Logger.d("ORDER SENT " + order.startLatitude+ " - " + order.startLongitude);
                                        })
                                        .subscribe();
                                }catch (EmptyStackException e){
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.953094","31.995188"));
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.954094","31.995188"));
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.955094","31.995188"));
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.956094","31.995188"));
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.957094","31.995188"));
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.958094","31.995188"));
                                    orders.push(new RetrofitModule.ServerAPI.CreateOrderRequestBody("46.959094","31.995188"));
                                }
                            })
                            .doOnError(throwable -> {
                                signal.countDown();
                            })
                            .subscribe();

                })
                .doOnError(throwable -> {
                    signal.countDown();
                })
                .subscribe();

        signal.await();// wait for callback
    }

    private Completable loginUser(){
        return Completable.create(emitter -> {
            userModel
                    .login(userEmail, userPassword)
                    .doOnNext(isLoggedIn -> {
                        if(isLoggedIn){
                            emitter.onComplete();
                        }else{
                            emitter.onError(new Throwable("Authentication error"));
                        }
                    })
                    .doOnError(throwable -> {
                        emitter.onError(new Throwable("Authentication error"));
                    })
                    .subscribe();
        });
    }

}
