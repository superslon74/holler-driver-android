package com.pnrhunter.mvp.splash;

import android.content.Context;

import com.pnrhunter.mvp.authorization.AuthenticationInterface;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.router.TestRouter;

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
            router.goTo(TestRouter.ROUTE_WELCOME );
        }else{
            router.goTo(TestRouter.ROUTE_MAIN );
        }
    }
}
