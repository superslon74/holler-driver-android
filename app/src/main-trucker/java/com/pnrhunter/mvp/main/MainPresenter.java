package com.pnrhunter.mvp.main;

import android.content.Context;

import com.pnrhunter.mvp.authorization.AuthenticationInterface;
import com.pnrhunter.mvp.utils.activity.Finishable;
import com.pnrhunter.mvp.utils.activity.MessageDisplayer;
import com.pnrhunter.mvp.utils.activity.SpinnerShower;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.server.objects.User;
import com.pnrhunter.mvp.utils.server.objects.order.OrderResponse;
import com.pnrhunter.mvp.utils.server.objects.order.Payment;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import java.util.Date;

import javax.inject.Inject;

public class MainPresenter {

    protected Context context;
    protected View view;
    protected AbstractRouter router;
    protected AuthenticationInterface auth;

    public MainPresenter(Context context, View view, AbstractRouter router, AuthenticationInterface auth) {
        this.context = context;
        this.view = view;
        this.router = router;
        this.auth = auth;
    }

    public void goOffline() {
    }

    public void goOnline() {
    }

    public User getUser() {
        //TODO: test
        User test = new User();
        test.firstName= "Abstract";
        test.lastName="Human";
        test.avatar = "https://pp.userapi.com/c627131/v627131182/2e57b/M7JF1VaQyVc.jpg";
        return test;
    }

    public void dispatchOrders() {
        //select request
        RequestedOrderResponse testRequest = new RequestedOrderResponse();
        testRequest.id="111111111";
        testRequest.requestId="222222222";
        testRequest.providerId="333333333";
        testRequest.timeToRespond=20;
        OrderResponse testOrder = new OrderResponse();
        testOrder.sAddress = "Nikolaev";
        testOrder.dAddress = "Kyiv";
        testOrder.weight = 200;
        testOrder.startedAt = new Date().toString();
        testOrder.paymentMode = "cash";
        testRequest.order=testOrder;

        view.showOrder(testRequest);
    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable{
        void showOrder(RequestedOrderResponse order);
    }
}
