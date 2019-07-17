package com.pnrhunter.di;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.pnrhunter.mvp.authentication.AuthenticationInterface;
import com.pnrhunter.mvp.authentication.DriverAuthenticationModel;
import com.pnrhunter.mvp.main.MainView;
import com.pnrhunter.mvp.splash.SplashPresenter;
import com.pnrhunter.mvp.splash.SplashView;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.activity.FloatingViewSwitcherInterface;
import com.pnrhunter.mvp.utils.activity.PermissionChecker;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.router.DriverRouter;
import com.pnrhunter.mvp.utils.server.ServerConfigurationInterface;
import com.pnrhunter.mvp.welcome.WelcomeView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.android.DaggerApplication;
import io.reactivex.Single;

public class DI {

    @ApplicationScope
    @Component(modules = {
            AndroidInjectionModule.class,
            ApplicationModule.class,
            ActivityBuilderModule.class
    })
    public interface ApplicationComponent extends AndroidInjector<DaggerApplication> {
//        @Override
        void inject(Application a);

        @Component.Builder
        interface ApplicationComponentBuilder{
            @BindsInstance
            ApplicationComponentBuilder bindApplicationContext(Context a);
            ApplicationComponent build();
        }
    }

    @Module
    public static abstract class ActivityBuilderModule{
        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                SplashModule.class
        })
        abstract SplashView bindSplashView();

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                WelcomeModule.class
        })
        abstract WelcomeView bindWelcomeView();

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                MainModule.class
        })
        abstract MainView bindMainView();
    }

    @Module
    public static abstract class SplashModule{
        @Provides
        public static SplashPresenter providePresenter(Context context, AbstractRouter router, AuthenticationInterface auth){
            return new SplashPresenter(context, router, auth);
        }

        @Binds
        abstract ExtendedActivity provideSplashView(SplashView v);
    }

    @Module
    public static abstract class WelcomeModule{

        @Binds
        abstract ExtendedActivity provideSplashView(WelcomeView v);
    }

    @Module
    public static abstract class MainModule{

        @Binds
        abstract ExtendedActivity provideSplashView(MainView v);
    }

    @Module
    public static class ExtendedActivityModule{
        @Provides
        public FloatingViewSwitcherInterface provideFloatingView(){
            return new FloatingViewSwitcherInterface() {
                @Override
                public void onActivityCountIncreased() {

                }

                @Override
                public void onActivityCountDecreased() {

                }
            };
        }

        @Provides
        public PermissionChecker providePermissionChecker(ExtendedActivity activity){
            return new PermissionChecker(activity);
        }
    }

    @Module
    public static class ApplicationModule{
        @ApplicationScope
        @Provides
        public AbstractRouter provideRouter(Context context){
            return new DriverRouter(context);
        }

        @ApplicationScope
        @Provides
        public AuthenticationInterface provideAuthModel(Context c){
            return new DriverAuthenticationModel(c);
        }

        @ApplicationScope
        @Provides
        public ServerConfigurationInterface provideServerConfigurationSource(){
            return new ServerConfigurationInterface() {
                @Override
                public Single<Boolean> checkSocialLoginIsEnabled() {
                    return Single.just(false);
                }
            };
        }

    }

    @Scope
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface ApplicationScope{};

    @Scope
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface ActivityScope{};
}
