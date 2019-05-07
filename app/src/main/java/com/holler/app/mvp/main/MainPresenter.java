package com.holler.app.mvp.main;

import android.content.Context;
import android.location.Location;
import com.holler.app.R;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.utils.Finishable;
import com.holler.app.utils.FiniteStateMachine;
import com.holler.app.utils.GPSTracker;
import com.holler.app.utils.MessageDisplayer;
import com.holler.app.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;



public class MainPresenter {

    private Context context;
    private RouterModule.Router router;
    private View view;
    private RetrofitModule.ServerAPI serverAPI;
    public UserModel userModel;
    public OrderModel orderModel;

    private GPSTracker.GPSTrackerBinder gpsTrackerService;
    private FiniteStateMachine.StateOwner<UserModel.Status.AccountStatus> account;

    public MainPresenter(Context context,
                         RouterModule.Router router,
                         View view,
                         RetrofitModule.ServerAPI serverAPI,
                         UserModel userModel,
                         OrderModel orderModel) {

        this.context = context;
        this.router = router;
        this.view = view;
        this.serverAPI = serverAPI;
        this.userModel = userModel;
        this.orderModel = orderModel;

        GPSTracker.serviceConnection(context)
                .flatMap(service -> {
                    gpsTrackerService = (GPSTracker.GPSTrackerBinder)service;
                    return statusRequesting();
                })
                .flatMap(checkStatusResponse -> {
                    Logger.d(checkStatusResponse.toString());
                    userModel.updateStatus(checkStatusResponse);
                    orderModel.updateRequestOrder(checkStatusResponse.requests);
                    processStatus(checkStatusResponse);
                    return Observable.empty();
                })
                .doOnError(throwable -> {
//                    Logger.e("shit2",throwable);
                })
                .subscribe();

        userModel
                .statusSource
                .doOnNext(newStatus -> view.onStatusChanged(newStatus))
                .subscribe();

        orderModel
                .orderSource
                .doOnNext(order -> {
                    view.onOrderChanged(order);
                })
                .subscribe();

        view.onStatusChanged(userModel.getCurrentStatus());

        initStates();

    }

    private void processStatus(RetrofitModule.ServerAPI.CheckStatusResponse response){
        currentAccountStatus = UserModel.extractAccountStatus(response);
        currentServiceStatus = UserModel.extractServiceStatus(response);
        currentOrderStatus = OrderModel.extractOrderStatus(response);

        account.onEnter();
    }

    private UserModel.Status.AccountStatus currentAccountStatus;
    private UserModel.Status.ServiceStatus currentServiceStatus;
    private OrderModel.Status currentOrderStatus;

    private void initStates() {

        FiniteStateMachine.StateOwner<OrderModel.Status> order =
                new FiniteStateMachine.StateOwner(
                        new HashMap<OrderModel.Status, FiniteStateMachine.State>() {{
                            put(OrderModel.Status.SEARCHING, () -> {
                                Logger.w("enter order SEARCHING");
                            });
                            put(OrderModel.Status.STARTED, () -> {
                                Logger.w("enter order STARTED");
                            });
                            put(OrderModel.Status.COMPLETED, () -> {
                                Logger.w("enter order COMPLETED");
                            });
                            put(OrderModel.Status.RATE, () -> {
                                Logger.w("enter order RATE");
                            });
                        }}) {
                    @Override
                    public void onPrepare() {
                        Logger.w("prepare service RIDING");
                    }

                    @Override
                    public void onEnter() {
                        Logger.w("enter service RIDING");
                        processStatus(currentOrderStatus);
                    }
                };

        FiniteStateMachine.StateOwner<UserModel.Status.ServiceStatus> service =
                new FiniteStateMachine.StateOwner(
                        new HashMap<UserModel.Status.ServiceStatus, FiniteStateMachine.State>() {{
                            put(UserModel.Status.ServiceStatus.ONLINE, () -> {
                                Logger.w("enter service ONLINE");
                            });
                            put(UserModel.Status.ServiceStatus.OFFLINE, () -> {
                                Logger.w("enter service OFFLINE");
                            });
                            put(UserModel.Status.ServiceStatus.RIDING, order);
                        }}) {
                    @Override
                    public void onPrepare() {
                        Logger.w("prepare account APPROVED");
                    }

                    @Override
                    public void onEnter() {
                        Logger.w("enter account APPROVED");
                        processStatus(currentServiceStatus);
                    }
                };

        account =
                new FiniteStateMachine.StateOwner(
                        new HashMap<UserModel.Status.AccountStatus, FiniteStateMachine.State>() {{
                            put(UserModel.Status.AccountStatus.NEW, () -> {
                                Logger.w("enter account NEW");
                            });
                            put(UserModel.Status.AccountStatus.DISAPPROVED, () -> {
                                Logger.w("enter account DISAPPROVED");
                            });
                            put(UserModel.Status.AccountStatus.BLOCKED, () -> {
                                Logger.w("enter account BLOCKED");
                            });
                            put(UserModel.Status.AccountStatus.APPROVED, service);
                        }}) {
                    @Override
                    public void onPrepare() {
                        Logger.w("prepare state machine");
                    }

                    @Override
                    public void onEnter() {
                        Logger.w("enter state machine");
                        processStatus(currentAccountStatus);
                    }
                };



    }


    private Subject<RetrofitModule.ServerAPI.CheckStatusResponse> statusRequesting() {
        final Subject<RetrofitModule.ServerAPI.CheckStatusResponse> subject = UnicastSubject.create();

        Flowable
                .interval(3, TimeUnit.SECONDS)
                .flatMapSingle(time -> {
                    Location location = gpsTrackerService.getLocation();
                    String authHeader = userModel.getAuthHeader();
                    String latitude = "" + location.getLatitude();
                    String longitude = "" + location.getLongitude();

                    return serverAPI
                            .checkStatus(authHeader, latitude, longitude)
                            .doOnSuccess(subject::onNext)
                            .onErrorReturnItem(new RetrofitModule.ServerAPI.CheckStatusResponse());
                })
                .subscribe();

        return subject;
    }

    public void goOffline() {
        userModel.goOffline();
    }

    public void goOnline() {
        userModel.goOnline();
    }

    public ObservableSource<Boolean> logout() {
        return userModel
                .logout()
                .doOnSubscribe(disposable -> {
                    view.showSpinner();
                })
                .doOnComplete(() -> {
                    view.hideSpinner();
                });
    }

    public void goToWelcomeScreen() {
        router.goToWelcomeScreen();
        view.finish();
    }

    public void createAndSendOrder(){

        Location location = gpsTrackerService.getLocation();
        String latitude = ""+location.getLatitude();
        String longitude = ""+location.getLongitude();

        String messageSuccess = context.getString(R.string.successfully_created_order);
        String messageError = context.getString(R.string.error_creating_order);

        serverAPI
                .createOrder(userModel.getAuthHeader(), new RetrofitModule.ServerAPI.CreateOrderRequestBody(latitude,longitude))
                .doOnSubscribe(disposable -> view.showSpinner())
                .doFinally(() -> view.hideSpinner())
                .doOnSuccess(creteOrderResponse -> {
                    if(creteOrderResponse.isSuccessfullyCreated()){
                        view.onMessage(creteOrderResponse.message);
                    }else{
                        view.onMessage(creteOrderResponse.message);
                    }
                })
                .doOnError(throwable -> view.onMessage(messageError))
                .subscribe();

    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {

        void onStatusChanged(UserModel.Status newStatus);
        void onOrderChanged(OrderModel.Order order);
    }




}
