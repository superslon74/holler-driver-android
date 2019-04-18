package com.holler.app.di.app.modules;

import android.content.Context;
import android.content.Intent;

import com.holler.app.activity.ActivityEmail;
import com.holler.app.activity.ActivitySocialLogin;
import com.holler.app.activity.MainActivity;
import com.holler.app.activity.RegisterActivity;
import com.holler.app.mvp.login.LoginView;
import com.holler.app.mvp.welcome.WelcomeView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RouterModule {


    @Singleton
    @Provides
    public RouterModule.Router provideRouter(Context context) {
        return new RouterModule.Router(context);
    }

    public class Router {
        private Context context;

        public Router(Context context) {
            this.context = context;
        }

        public void goToWelcomeScreen(){
            Intent i = new Intent(context, WelcomeView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        public void goToMainScreen(){
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

        public void goToLoginScreen(){
            Intent i = new Intent(context, LoginView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }

        public void goToRegisterScreen() {
            Intent i = new Intent(context, RegisterActivity.class);
            i.putExtra("signup", true).putExtra("viewpager", "yes");
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }

        public void gotoSocialLogin(){
            Intent i = new Intent(context, ActivitySocialLogin.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }

    }

}
