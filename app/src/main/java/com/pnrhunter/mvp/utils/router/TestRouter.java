package com.pnrhunter.mvp.utils.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class TestRouter extends AbstractRouter{
    public static Class<? extends Activity> ROUTE_WELCOME = AbstractRouter.ROUTE_MAIN;

    public static Class<? extends Activity> ROUTE_WAITING_FOR_APPROVAL = DriverRouter.ROUTE_WAITING_FOR_APPROVAL;
    public static Class<? extends Activity> ROUTE_DOCUMENTS = DriverRouter.ROUTE_DOCUMENTS;


    public TestRouter(Context context) {
        super(context);
    }

    @Override
    public boolean checkTransitionAllowedTo(Class<? extends Activity> activity) {
        return true;
    }

    @Override
    public void addExtra(Intent i, Class<? extends Activity> activity) {

    }
}