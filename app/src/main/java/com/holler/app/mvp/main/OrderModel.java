package com.holler.app.mvp.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.holler.app.di.app.modules.DeviceInfoModule;
import com.holler.app.di.app.modules.RetrofitModule;
import com.holler.app.di.app.modules.UserStorageModule;
import com.orhanobut.logger.Logger;

import java.util.List;


import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class OrderModel {

    private RetrofitModule.ServerAPI serverAPI;
    private UserStorageModule.UserStorage userStorage;
    private DeviceInfoModule.DeviceInfo deviceInfo;
    private UserModel userModel;

    public OrderModel(RetrofitModule.ServerAPI serverAPI,
                      UserStorageModule.UserStorage userStorage,
                      DeviceInfoModule.DeviceInfo deviceInfo,
                      UserModel userModel) {

        this.serverAPI = serverAPI;
        this.userStorage = userStorage;
        this.deviceInfo = deviceInfo;
        this.userModel = userModel;

        orderSource = PublishSubject.create();
    }

    private RetrofitModule.ServerAPI.RequestedOrderResponse currentRequest;

    public Subject<Order> orderSource;

    public static Status extractOrderStatus(RetrofitModule.ServerAPI.CheckStatusResponse response){
        try{
            return extractOrderStatus(response.requests.get(0).order);
        }catch (NullPointerException | IndexOutOfBoundsException e){
            return Status.UNKNOWN;
        }
    }

    public static Status extractOrderStatus(RetrofitModule.ServerAPI.OrderResponse data) {
        if(data==null) return Status.UNKNOWN;
        Status status;
        switch (data.status) {
            case "SEARCHING":
                status = Status.SEARCHING;
                break;
            case "STARTED":
                status = Status.STARTED;
                break;
            case "ARRIVED":
                status = Status.ARRIVED;
                break;
            case "PICKEDUP":
                status = Status.PICKEDUP;
                break;
            case "DROPPED":
                status = Status.DROPPED;
                break;
            case "COMPLETED":
                status = Status.COMPLETED;
                break;
            case "RATE":
                status = Status.RATE;
                break;
            case "SCHEDULED":
                status = Status.SCHEDULED;
                break;
            default:
                status = Status.UNKNOWN;
                break;
        }
        return status;
    }

    public static RetrofitModule.ServerAPI.RequestedOrderResponse extractRequest(RetrofitModule.ServerAPI.CheckStatusResponse response) {
        if(response.requests!=null && response.requests.size()>0)
            return response.requests.get(0);

        return null;
    }

    public Order createOrderFromRequest(RetrofitModule.ServerAPI.RequestedOrderResponse currentRequest) {
        return new Order(currentRequest.order, currentRequest.timeToRespond);
    }

    public Order getCurrentOrder(){
        try {
            return new Order(currentRequest.order, currentRequest.timeToRespond);
        }catch (NullPointerException e){
            return null;
        }
    }

    public void updateRequestOrder(List<RetrofitModule.ServerAPI.RequestedOrderResponse> requests) {
        if (currentRequest == null && requests.size() > 0) {
            currentRequest = requests.get(0);
            Order newOrder = new Order(currentRequest.order, currentRequest.timeToRespond);
            orderSource.onNext(newOrder);
        } else if (currentRequest != null && requests.size() == 0) {
//            orderSource.onComplete();
            currentRequest = null;
        } else if (currentRequest != null) {
            for (RetrofitModule.ServerAPI.RequestedOrderResponse requestOrder : requests) {
                updateRequest(requestOrder);
            }
        }
    }

    public synchronized void updateRequest(RetrofitModule.ServerAPI.RequestedOrderResponse requestOrder) {
        Order newOrder = new Order(requestOrder.order, requestOrder.timeToRespond);

        if (requestOrder.equals(currentRequest)) {
            Order currentOrder = new Order(currentRequest.order, currentRequest.timeToRespond);
            if (!currentOrder.equals(newOrder)) {
                currentRequest = requestOrder;
                orderSource.onNext(newOrder);
            }

        } else {
            Logger.e("DIFFERENT REQUEST");
        }
    }

    public void clearState() {
        currentRequest=null;
    }

    class Order {
        public int timeToRespond;
        public Status status;
        public RetrofitModule.ServerAPI.OrderResponse data;

        public Order(RetrofitModule.ServerAPI.OrderResponse data, int timeToRespond) {
            this.timeToRespond = timeToRespond;
            this.data = data;
            status = extractOrderStatus(data);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            try {
                return
                        ((Order) obj).status == this.status;
            } catch (ClassCastException | NullPointerException e) {
                return false;
            }
        }

        public Single<JsonElement> accept() {
            String header = userModel.getAuthHeader();
            String orderId = this.data.id;
            Logger.w("ACCEPTING "+orderId+" "+header);
            return serverAPI.acceptOrder(header,orderId)
                    .doOnSuccess(jsonElement -> {
                        Logger.w("ACCEPTING true ");
                    })
                    .doOnError(throwable -> {
                        Throwable t = throwable;
                        Logger.e("ACCEPTING false",t);
                    });
        }

        public Single<JsonElement> reject() {
            Logger.w("REJECTING ");
            String header = userModel.getAuthHeader();
            String orderId = this.data.id;

            return serverAPI.rejectOrder(header, orderId)
                    .doOnSuccess(jsonElement -> {
                        Logger.w("REJECTING  true ");
                    })
                    .doOnError(throwable -> {
                        Throwable t = throwable;
                        Logger.e("REJECTING false",t);
                    });
        }

        public Single<JsonObject> arrived() {
            String method = "PATCH";
            String status;
            if(this.data.user!=null){
                status = "ARRIVED";
            }else{
                status = "COMPLETE";
            }
            return serverAPI
                    .updateOrder(
                            userModel.getAuthHeader(),
                            this.data.id,
                            new RetrofitModule.ServerAPI.UpdateOrderRequestBody(
                            method,
                            status,
                            null,
                            null,
                            null));
        }

        public Single<JsonObject> cancel() {
            return serverAPI.cancelOrder(
                    userModel.getAuthHeader(),
                    new RetrofitModule.ServerAPI.CancelOrderRequestBody(this.data.id));
        }

        public Single<JsonObject> rate(int rating){
            String status = "RATE";
            return serverAPI
                    .rateOrder(
                            userModel.getAuthHeader(),
                            this.data.id,
                            new RetrofitModule.ServerAPI.UpdateOrderRequestBody(
                                    null,
                                    status,
                                    rating+"",
                                    null,
                                    null));
        }
    }

    enum Status {UNKNOWN, SEARCHING, STARTED, ARRIVED, PICKEDUP, DROPPED, COMPLETED, RATE, SCHEDULED}

}
