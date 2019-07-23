package com.pnrhunter.di;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.pnrhunter.mvp.authorization.AuthenticationInterface;
import com.pnrhunter.mvp.authorization.EmailLoginPresenter;
import com.pnrhunter.mvp.authorization.EmailView;
import com.pnrhunter.mvp.authorization.LoginPresenter;
import com.pnrhunter.mvp.authorization.PasswordLoginPresenter;
import com.pnrhunter.mvp.authorization.PasswordView;
import com.pnrhunter.mvp.authorization.RegistrationPresenter;
import com.pnrhunter.mvp.authorization.RegistrationPresenter_Driver;
import com.pnrhunter.mvp.authorization.RegistrationViewTest;
import com.pnrhunter.mvp.authorization.TestAuthenticationModel;
import com.pnrhunter.mvp.documents.DocumentsPresenter;
import com.pnrhunter.mvp.documents.DocumentsView;
import com.pnrhunter.mvp.documents.WaitingForApprovalView;
import com.pnrhunter.mvp.main.MainPresenter;
import com.pnrhunter.mvp.main.MainView;
import com.pnrhunter.mvp.main.MapFragment;
import com.pnrhunter.mvp.main.OrderContainerFragment;
import com.pnrhunter.mvp.main.RequestOrderFragment;
import com.pnrhunter.mvp.splash.SplashPresenter;
import com.pnrhunter.mvp.splash.SplashView;
import com.pnrhunter.mvp.utils.DeviceInfo;
import com.pnrhunter.mvp.utils.Validator;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.activity.FloatingViewSwitcherInterface;
import com.pnrhunter.mvp.utils.activity.FragmentHeaderBig;
import com.pnrhunter.mvp.utils.activity.PermissionChecker;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.server.ServerConfigurationInterface;
import com.pnrhunter.mvp.utils.server.objects.Document;
import com.pnrhunter.mvp.utils.router.DriverRouter;
import com.pnrhunter.mvp.utils.server.ServerAPI_Documents;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;
import com.pnrhunter.mvp.welcome.WelcomeView;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class TruckerDriverDI_Test {

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

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                LoginEmailModule.class
        })
        abstract EmailView bindEmailView();

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                LoginPasswordModule.class
        })
        abstract PasswordView bindPasswordView();

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                RegistrationModule.class
        })
        abstract RegistrationViewTest bindRegistrationView();

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                DocumentsModule.class
        })
        abstract DocumentsView bindDocumentsView();

        @ActivityScope
        @ContributesAndroidInjector(modules = {
                ExtendedActivityModule.class,
                WaitingForApprovalModule.class
        })
        abstract WaitingForApprovalView bindWaitingView();

//        @ActivityScope
//        @ContributesAndroidInjector(modules = {
//                ExtendedActivityModule.class,
//                LoginModule.class,
//        })
//        abstract PasswordView bindPasswordView();
    }

    @Module
    public static abstract class WaitingForApprovalModule{
        @Binds
        abstract ExtendedActivity provideEmailView(WaitingForApprovalView v);
    }

    @Module
    public static abstract class DocumentsModule{
        @FragmentScope
        @ContributesAndroidInjector
        abstract FragmentHeaderBig provideHeaderFragment();

        @ActivityScope
        @Provides
        public static DocumentsPresenter providePresenter(Context context,
                                                          DocumentsView view,
                                                          AbstractRouter router,
                                                          AuthenticationInterface auth,
                                                          DeviceInfo deviceInfo,
                                                          ServerAPI_Documents api){
            return new DocumentsPresenter(context, view, router, deviceInfo, api, auth);
        }

        @Binds
        abstract ExtendedActivity provideEmailView(DocumentsView v);
    }

    @Module
    public static abstract class RegistrationModule{
        @FragmentScope
        @ContributesAndroidInjector
        abstract FragmentHeaderBig provideHeaderFragment();

        @ActivityScope
        @Provides
        public static RegistrationPresenter providePresenter(Context context,
                                                             RegistrationViewTest view,
                                                             AbstractRouter router,
                                                             AuthenticationInterface auth,
                                                             DeviceInfo deviceInfo,
                                                             Validator validator){
            return new RegistrationPresenter_Driver(context, view, router, auth, deviceInfo, validator);
        }

        @Binds
        abstract ExtendedActivity provideEmailView(RegistrationViewTest v);
    }

    @Module
    public static abstract class LoginEmailModule{
        @FragmentScope
        @ContributesAndroidInjector
        abstract FragmentHeaderBig provideHeaderFragment();

        @ActivityScope
        @Provides
        public static EmailLoginPresenter providePresenter(EmailView view, Context c, AbstractRouter r, Validator v, AuthenticationInterface a, LoginPresenter.PendingCredentials p){
            LoginPresenter lp = new LoginPresenter(c,r,v,a);
            lp.setCredentials(p);
            lp.setView(view);
            return lp;
        }

        @Binds
        abstract ExtendedActivity provideEmailView(EmailView v);
    }

    @Module
    public static abstract class LoginPasswordModule{
        @FragmentScope
        @ContributesAndroidInjector
        abstract FragmentHeaderBig provideHeaderFragment();

        @ActivityScope
        @Provides
        public static PasswordLoginPresenter providePresenter(PasswordView view, Context c, AbstractRouter r, Validator v, AuthenticationInterface a, LoginPresenter.PendingCredentials p){
            LoginPresenter lp = new LoginPresenter(c,r,v,a);
            lp.setCredentials(p);
            lp.setView(view);
            return lp;
        }

        @Binds
        abstract ExtendedActivity providePasswordView(PasswordView v);
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


        @FragmentScope
        @ContributesAndroidInjector
        abstract MapFragment provideMapFragment();

        @FragmentScope
        @ContributesAndroidInjector
        abstract OrderContainerFragment provideOrderContainerFragment();

        @FragmentScope
        @ContributesAndroidInjector
        abstract RequestOrderFragment provideRequestOrderFragment();

        @Provides
        public static MainPresenter providePresenter(Context context,
                                                     MainView view,
                                                     AbstractRouter router,
                                                     AuthenticationInterface auth){
            return new MainPresenter(context,view,router,auth);
        }

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
        public Validator provideValidator(Context context){
            return new Validator(context);
        }

        @ApplicationScope
        @Provides
        public AuthenticationInterface provideAuthModel(Context c){
            return new TestAuthenticationModel(c);
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

        @ApplicationScope
        @Provides
        public LoginPresenter.PendingCredentials providePendingCredentials(){
            return new LoginPresenter.PendingCredentials("","");
        }

        @ApplicationScope
        @Provides
        public ServerAPI_Documents serverApiForDocuments(){
            return new ServerAPI_Documents() {
                @Override
                public Single<List<Document>> getRequiredDocuments(String header, String devicetype, String deviceId, String deviceToken) {
                    List<Document> testDocuments = new ArrayList<>();
                    testDocuments.add(new Document("1","Passport",null,null));
                    testDocuments.add(new Document("2","VISA","https://ichef.bbci.co.uk/news/660/cpsprodpb/16FD3/production/_105236149_img_2026.jpg",null));
                    return Single
                            .timer(3, TimeUnit.SECONDS)
                            .flatMap(aLong -> {
                                return Single.just(testDocuments);
                    });
                }

                @Override
                public Single<Document> sendDocument(String header, RequestBody devicetype, RequestBody deviceId, RequestBody deviceToken, String documentId, MultipartBody.Part document) {
                    String testImg;
                    switch (documentId){
                        case "1":testImg="https://24tv.ua/resources/photos/news/610x344_DIR/201903/1122287.jpg?201903071800";break;
                        case "2":testImg="https://imgclf.112.ua/original/2018/01/19/330146.jpg?timestamp=1516371215";break;
                        default: testImg="";
                    }
                    Document testDocument = new Document(documentId, "Changed name",testImg,null);
                    return Single
                            .timer(3, TimeUnit.SECONDS)
                            .flatMap(aLong -> {
                                return Single.just(testDocument);
                            });
                }
            };
        }

        @ApplicationScope
        @Provides
        public DeviceInfo provideDeviceInfo(Context context){
            ContentResolver resolver = context.getContentResolver();
            DeviceInfo info = new DeviceInfo();
            info.setType(DeviceInfo.TYPE_ANDROID);
            try {
                String id = android.provider.Settings.Secure.getString(resolver, android.provider.Settings.Secure.ANDROID_ID);
                info.setId(id);
            } catch (Exception e) {
                info.setId(DeviceInfo.ID_NOT_FOUND);
            }

            try {
                //TODO: install firebase app
                String token = FirebaseInstanceId.getInstance().getToken(info.getId(), "aaaaa");
                info.setToken(token);
            } catch (IOException | RuntimeException e) {
                info.setToken(DeviceInfo.TOKEN_NOT_FOUND);
            }
            return info;
        }
    }

    @Scope
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface LoginScope{};

    @Scope
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface FragmentScope{};

    @Scope
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface ApplicationScope{};

    @Scope
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface ActivityScope{};


}
