package com.pnrhunter.mvp.splash;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pnrhunter.mvp.authentication.AuthenticationInterface;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.router.DriverRouter;

public class SplashPresenter {

    private Context context;
    private AuthenticationInterface auth;
    private AbstractRouter router;

    public SplashPresenter(Context context, AbstractRouter router, AuthenticationInterface auth) {
        this.context = context;
        this.auth = auth;
        this.router = router;
    }


    public void checkLoggedIn() {
        if(!auth.isLoggedIn()){
            router.goTo(DriverRouter.ROUTE_WELCOME );
        }else{
            router.goTo(DriverRouter.ROUTE_MAIN );
        }
    }
}
