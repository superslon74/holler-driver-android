package com.pnrhunter.di.app;

import android.app.Instrumentation;
import android.content.Context;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pnrhunter.di.app.DaggerTestAppComponent;
import com.pnrhunter.di.app.modules.DeviceInfoModule;
import com.pnrhunter.di.app.modules.OrderModule;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.di.app.modules.SharedPreferencesModule;
import com.pnrhunter.di.app.modules.UserModule;
import com.pnrhunter.di.app.modules.UserStorageModule;
import com.pnrhunter.mvp.main.UserModel;
import com.orhanobut.logger.Logger;

import org.junit.Before;
import org.junit.Test;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;

@LargeTest
public class CreateOrderFromDriverTest {

    private static final String userEmail = "dennis.eideland@gmail.com";
    private static final String userPassword = "dennis132";

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
