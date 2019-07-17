package com.pnrhunter.mvp.utils.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.pnrhunter.mvp.main.MainView;
import com.pnrhunter.mvp.splash.SplashView;
import com.pnrhunter.mvp.welcome.WelcomeView;

public class DriverRouter extends AbstractRouter{

    public static Class<? extends Activity> ROUTE_SPLASH = SplashView.class;
    public static Class<? extends Activity> ROUTE_WELCOME = WelcomeView.class;
    public static Class<? extends Activity> ROUTE_MAIN = MainView.class;
    public static Class<? extends Activity> ROUTE_LOGIN_EMAIL = MainView.class;
    public static Class<? extends Activity> ROUTE_REGISTRATION = MainView.class;
    public static Class<? extends Activity> ROUTE_LOGIN_SOCIAL = MainView.class;

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
