package com.pnrhunter.mvp.utils.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.pnrhunter.mvp.documents.DocumentsView;
import com.pnrhunter.mvp.documents.WaitingForApprovalView;
import com.pnrhunter.mvp.main.MainView;

public class DriverRouter extends AbstractRouter{

    public static Class<? extends Activity> ROUTE_WAITING_FOR_APPROVAL = WaitingForApprovalView.class;
    public static Class<? extends Activity> ROUTE_DOCUMENTS = DocumentsView.class;

    public DriverRouter(Context context) {
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
