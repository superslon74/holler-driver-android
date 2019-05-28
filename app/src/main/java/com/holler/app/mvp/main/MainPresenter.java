package com.holler.app.mvp.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;

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
import java.util.List;
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
    private ServiceConnection gpsTrackerServiceConnection;

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

        createGpsTrackerServiceConnection()
                .flatMap(service -> {
                    gpsTrackerService = (GPSTracker.GPSTrackerBinder)service;
                    gpsTrackerService.startTracking();
                    return statusRequesting();
                })
                .flatMap(checkStatusResponse -> {
//                    Logger.d(checkStatusResponse.toString());
                    userModel.updateStatus(checkStatusResponse);
                    orderModel.updateRequestOrder(checkStatusResponse.requests);
                    processStatus(checkStatusResponse);
                    return Observable.empty();
                })
                .doOnError(throwable -> {
                    view.showMessage(throwable.getMessage());
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

    private Observable<GPSTracker.GPSTrackerBinder> createGpsTrackerServiceConnection(){
        return Observable.<GPSTracker.GPSTrackerBinder>create(emitter -> {
            Intent gpsTrackerBinding = new Intent(context, GPSTracker.class);

            this.gpsTrackerServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    emitter.onNext((GPSTracker.GPSTrackerBinder) service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    emitter.onError(new ServiceConnectionError("Can't enable location tracking"));
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    emitter.onError(new ServiceConnectionError("Can't enable location tracking"));
                }

                @Override
                public void onNullBinding(ComponentName name) {
                    emitter.onError(new ServiceConnectionError("Can't enable location tracking"));
                }
            };

            context.bindService(gpsTrackerBinding, this.gpsTrackerServiceConnection, Context.BIND_IMPORTANT);
        });
    }

    public static class ServiceConnectionError extends Throwable{
        public ServiceConnectionError(String message) {
            super(message);
        }
    }

    public void onViewDestroyed(){
        if (gpsTrackerServiceConnection!=null)
            context.unbindService(gpsTrackerServiceConnection);
    }

    RetrofitModule.ServerAPI.RequestedOrderResponse currentRequest = null;
    private void processStatus(RetrofitModule.ServerAPI.CheckStatusResponse response){
        currentAccountStatus = UserModel.extractAccountStatus(response);
        currentServiceStatus = UserModel.extractServiceStatus(response);
        currentOrderStatus = OrderModel.extractOrderStatus(response);
        currentRequest = OrderModel.extractRequest(response);


        account.onEnter();
    }

    private UserModel.Status.AccountStatus currentAccountStatus;
    private UserModel.Status.ServiceStatus currentServiceStatus;
    private OrderModel.Status currentOrderStatus;

    private void initStates() {

        FiniteStateMachine.StateOwner<OrderModel.Status> order =
                new FiniteStateMachine.StateOwner<OrderModel.Status>(
                        new HashMap<OrderModel.Status, FiniteStateMachine.State>() {{
                            put(OrderModel.Status.SEARCHING, () -> {
                                view.setSearchingState(orderModel.createOrderFromRequest(currentRequest));
//                                Logger.w("enter order SEARCHING");
                            });
                            put(OrderModel.Status.STARTED, () -> {
                                view.setStartedState(orderModel.createOrderFromRequest(currentRequest));
//                                Logger.w("enter order STARTED");
                            });
                            put(OrderModel.Status.COMPLETED, () -> {
                                view.setCompletedState(orderModel.createOrderFromRequest(currentRequest));
//                                Logger.w("enter order COMPLETED");
                            });
                            put(OrderModel.Status.RATE, () -> {
                                view.setRateState(orderModel.createOrderFromRequest(currentRequest));
//                                Logger.w("enter order RATE");
                            });
                        }}) {
                    @Override
                    public void onPrepare() {
//                        Logger.w("prepare service RIDING");
                        currentState=null;
                        view.setRidingState();
                    }

                    @Override
                    public void onEnter() {
//                        Logger.w("enter service RIDING");
                        processStatus(currentOrderStatus);
                    }
                };

        FiniteStateMachine.StateOwner<UserModel.Status.ServiceStatus> service =
                new FiniteStateMachine.StateOwner<UserModel.Status.ServiceStatus>(
                        new HashMap<UserModel.Status.ServiceStatus, FiniteStateMachine.State>() {{
                            put(UserModel.Status.ServiceStatus.ONLINE, () -> {
                                view.setOnlineState();
//                                Logger.w("enter service ONLINE");
                            });
                            put(UserModel.Status.ServiceStatus.OFFLINE, () -> {
                                view.setOfflineState();
//                                Logger.w("enter service OFFLINE");
                            });
                            put(UserModel.Status.ServiceStatus.RIDING, order);
                        }}) {
                    @Override
                    public void onPrepare() {
//                        Logger.w("prepare account APPROVED");
                        view.setApprovedState();
                        currentState = null;
                    }

                    @Override
                    public void onEnter() {
//                        Logger.w("enter account APPROVED");
                        processStatus(currentServiceStatus);
                    }
                };

        account =
                new FiniteStateMachine.StateOwner<UserModel.Status.AccountStatus>(
                        new HashMap<UserModel.Status.AccountStatus, FiniteStateMachine.State>() {{
                            put(UserModel.Status.AccountStatus.NEW, () -> {
                                router.goToDocumentsScreen();
//                                Logger.w("enter account NEW");
                            });
                            put(UserModel.Status.AccountStatus.DISAPPROVED, () -> {
                                router.goToDocumentsScreen();
//                                Logger.w("enter account DISAPPROVED");
                            });
                            put(UserModel.Status.AccountStatus.BLOCKED, () -> {
                                view.setBlockedState();
//                                Logger.w("enter account BLOCKED");
                            });
                            put(UserModel.Status.AccountStatus.APPROVED, service);
                        }}) {
                    @Override
                    public void onPrepare() {
                        Logger.w("prepare state machine");
                    }

                    @Override
                    public void onEnter() {
//                        Logger.w("enter state machine");
                        processStatus(currentAccountStatus);
                    }
                };
    }


    private Subject<RetrofitModule.ServerAPI.CheckStatusResponse> statusRequesting() {
        final Subject<RetrofitModule.ServerAPI.CheckStatusResponse> subject = UnicastSubject.create();

        Flowable
                .interval(1, TimeUnit.SECONDS)
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
        if (location ==null){
            view.showMessage(context.getString(R.string.error_no_location));
            return;
        }
        String latitude = ""+location.getLatitude();
        String longitude = ""+location.getLongitude();

        String messageSuccess = context.getString(R.string.successfully_created_order);
        String messageError = context.getString(R.string.error_creating_order);

        serverAPI
                .createOrder(userModel.getAuthHeader(),
                        new RetrofitModule.ServerAPI.CreateOrderRequestBody(latitude,longitude))
                .doOnSubscribe(disposable -> view.showSpinner())
                .doFinally(() -> view.hideSpinner())
                .doOnSuccess(creteOrderResponse -> {
                    view.showMessage(creteOrderResponse.message);
                })
                .doOnError(throwable -> view.showMessage(messageError))
                .subscribe();

    }

    public void resetState() {
        currentAccountStatus = null;
        currentServiceStatus = null;
        currentOrderStatus = null;
        account.resetState();
    }

    public void goToEditProfileScreen() {
        router.goToEditProfileScreen();
        view.finish();
    }

    public void goToDocumentsScreen() {
        serverAPI
                .getRequiredDocuments(userModel.getAuthHeader(),"la","bla","lalala")
                .toObservable()
                .flatMap(documents -> {
                    if(checkDocumentsRequired(documents)){
                        router.goToDocumentsScreen();
                        view.finish();
                    }else{
                        view.showMessage(context.getString(R.string.error_all_documents_exists));
                    }
                    return Observable.empty();
                })
                .subscribe();

    }

    private boolean checkDocumentsRequired(List<RetrofitModule.ServerAPI.Document> documents){
        for(RetrofitModule.ServerAPI.Document d: documents){
            if(d.isRequired()) return true;
        }
        return false;
    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {

        void onStatusChanged(UserModel.Status newStatus);
        void onOrderChanged(OrderModel.Order order);

        void setBlockedState();

        void setOnlineState();

        void setOfflineState();

        void setSearchingState(OrderModel.Order order);

        void setStartedState(OrderModel.Order order);

        void setCompletedState(OrderModel.Order order);

        void setRateState(OrderModel.Order order);

        void setApprovedState();

        void setRidingState();
    }


}
