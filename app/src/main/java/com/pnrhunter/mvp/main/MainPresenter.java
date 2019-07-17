package com.pnrhunter.mvp.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;

import com.pnrhunter.R;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.utils.Finishable;
import com.pnrhunter.utils.FiniteStateMachine;
import com.pnrhunter.utils.GPSTracker;
import com.pnrhunter.utils.MessageDisplayer;
import com.pnrhunter.utils.SpinnerShower;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;


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
                    orderModel.updateRequestInSearching(checkStatusResponse.requestsInSearching);
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
            String errorMessage = ((Context)view).getString(R.string.error_no_location);
            this.gpsTrackerServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    emitter.onNext((GPSTracker.GPSTrackerBinder) service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    ServiceConnectionError e = new ServiceConnectionError(errorMessage);
                    emitter.onError(e);
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    ServiceConnectionError e = new ServiceConnectionError(errorMessage);
                    emitter.onError(e);
                }

                @Override
                public void onNullBinding(ComponentName name) {
                    ServiceConnectionError e = new ServiceConnectionError(errorMessage);
                    emitter.onError(e);
                }
            };

            context.bindService(gpsTrackerBinding, this.gpsTrackerServiceConnection, Context.BIND_IMPORTANT);
        });
    }

    public void goToTripsScreen() {
        router.goToTripsScreen();
        view.finish();
    }

    public void getApplicationUrl() {
        serverAPI
                .getApplicationLink()
                .doOnSuccess(response -> {
                    view.sendShareIntent(response.link);
                })
                .subscribe();
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

    private Observable<RetrofitModule.ServerAPI.CheckStatusResponse> statusRequesting() {

        return Observable
                .interval(1, TimeUnit.SECONDS)
                .flatMap(time -> {
                    Location location = gpsTrackerService.getLocation();
                    String authHeader = userModel.getAuthHeader();
                    String latitude = "" + location.getLatitude();
                    String longitude = "" + location.getLongitude();

                    return Observable.<RetrofitModule.ServerAPI.CheckStatusResponse>create(emitter -> {
                        RetrofitModule.ServerAPI.CheckStatusResponse emptyResponse = new RetrofitModule.ServerAPI.CheckStatusResponse();
                        try {
                            serverAPI
                                    .checkStatus(authHeader, latitude, longitude)
                                    .doOnSuccess(checkStatusResponse -> {
                                        emitter.onNext(checkStatusResponse);
                                    })
                                    .onErrorReturnItem(emptyResponse)
                                    .subscribe();
                        }catch (Exception e){
                            emitter.onNext(emptyResponse);
                        }
                    });
                });

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

    public Observable<Boolean> createAndSendOrder(){

        if(gpsTrackerService == null){
            return Observable.<Boolean>create(emitter -> {
                createGpsTrackerServiceConnection()
                        .flatMap(service -> {
                            gpsTrackerService = (GPSTracker.GPSTrackerBinder)service;
                            gpsTrackerService.startTracking();
                            return createAndSendOrder();
                        })
                        .flatMap(isCreated -> {
                            emitter.onNext(isCreated);
                            emitter.onComplete();
                            return Observable.empty();
                        })
                        .doOnError(throwable -> {
                            emitter.onNext(false);
                            emitter.onComplete();
                            view.showMessage(throwable.getMessage());
                        })
                        .subscribe();
            });
        }


        Location location = gpsTrackerService.getLocation();
        if (location ==null){
            view.showMessage(context.getString(R.string.error_no_location));
            return Observable.just(false);
        }
        String latitude = ""+location.getLatitude();
        String longitude = ""+location.getLongitude();

        String messageSuccess = context.getString(R.string.successfully_created_order);
        String messageError = context.getString(R.string.error_creating_order);

        return Observable.<Boolean>create(emitter -> {
            serverAPI
                    .createOrder(userModel.getAuthHeader(),
                            new RetrofitModule.ServerAPI.CreateOrderRequestBody(latitude,longitude))
                    .doOnSubscribe(disposable -> view.showSpinner())
                    .doFinally(() -> view.hideSpinner())
                    .doOnSuccess(creteOrderResponse -> {
                        view.showMessage(creteOrderResponse.message);
                        if(creteOrderResponse.isSuccessfullyCreated()){
                            emitter.onNext(true);
                            emitter.onComplete();
                        }else{
                            emitter.onNext(false);
                            emitter.onComplete();
                        }

                    })
                    .doOnError(throwable -> {
                        view.showMessage(messageError);
                        emitter.onNext(false);
                        emitter.onComplete();
                    })
                    .subscribe();
        });


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

        void sendShareIntent(String url);

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
