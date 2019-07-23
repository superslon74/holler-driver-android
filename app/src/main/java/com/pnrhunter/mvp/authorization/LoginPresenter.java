package com.pnrhunter.mvp.authorization;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.pnrhunter.mvp.utils.Validator;
import com.pnrhunter.mvp.utils.activity.Finishable;
import com.pnrhunter.mvp.utils.activity.MessageDisplayer;
import com.pnrhunter.mvp.utils.activity.SpinnerShower;
import com.pnrhunter.mvp.utils.router.AbstractRouter;

public class LoginPresenter implements EmailLoginPresenter, PasswordLoginPresenter {
    private Context context;
    private AbstractRouter router;
    private Validator validator;
    private AuthenticationInterface auth;
    private PendingCredentials credentials;


    public LoginPresenter(Context context, AbstractRouter router, Validator validator, AuthenticationInterface auth) {
        this.context = context;
        this.validator = validator;
        this.router = router;
        this.auth = auth;
        this.credentials = new PendingCredentials("","");
    }

    private View view;

    public void setView(View view){
        this.view = view;
    }

    public void goToPasswordView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        Throwable validationResult = validator.validateEmail(credentials.email);
        if(validationResult==null){
            router.goTo(AbstractRouter.ROUTE_LOGIN_PASSWORD);
            view.finish();
        }else{
            view.showMessage(validationResult.getMessage());
        }
    }

    public void goToEmailView(PendingCredentials newCredentials){
        credentials.merge(newCredentials);
        view.finish();
        router.goTo(AbstractRouter.ROUTE_LOGIN_EMAIL);
    }

    public void goToMainView(PendingCredentials newCredentials){
        this.credentials.merge(newCredentials);
        Throwable validationResult = validator.validatePassword(credentials.password, credentials.password);
        if(validationResult==null){
            signIn(credentials.getEmail(), credentials.getPassword());
        }else{
            view.showMessage(validationResult.getMessage());
        }
    }

    public void signIn(String email, String password) {

        auth
                .login(email, password)
                .doOnSubscribe(disposable -> view.showSpinner())
                .doOnNext(isLoggedIn -> {
                    if(isLoggedIn) {
                        router.goTo(AbstractRouter.ROUTE_MAIN);
                        credentials.clear();
                    }
                    else
                        Logger.e("Login Presenter user not logged in without error");
                })
                .doFinally(() -> view.hideSpinner())
                .doOnError(throwable -> view.showMessage(((Exception)throwable).getMessage()))
                .subscribe();

    }

    public void goToWelcomeView() {
        credentials.clear();
        view.finish();
        router.goTo(AbstractRouter.ROUTE_WELCOME);
    }

    public void goToRegisterView() {
        router.goTo(AbstractRouter.ROUTE_REGISTRATION);
        view.finish();
    }

    @Override
    public void onViewReady() {
        this.view.setupFields(this.credentials);
    }

    @Override
    public void onNextPressed(String email) {
        credentials.merge(new PendingCredentials(email,null));
        goToForgotPasswordView();
    }

    @Override
    public void onLinkPressed() {
        goToRegisterView();
    }

    public void goToForgotPasswordView() {
        router.goTo(AbstractRouter.ROUTE_LOGIN_PASSWORD);
        view.finish();
    }

    public void setCredentials(PendingCredentials p) {
        this.credentials = p;
    }

    public static class PendingCredentials{
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public PendingCredentials(String email, String password){
            this.email = email;
            this.password = password;
        }

        public void clear(){
            this.email=null;
            this.password=null;
        }

        public void merge(PendingCredentials newCredentials){
            if(newCredentials.email!=null)
                this.email = newCredentials.email;
            if(newCredentials.password!=null)
                this.password = newCredentials.password;
        }
    }

    public interface View extends SpinnerShower, MessageDisplayer, Finishable {
        void setupFields(PendingCredentials credentials);
    }
}
