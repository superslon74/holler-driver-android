package com.holler.app.mvp.login;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;

import com.holler.app.di.app.components.DaggerLoginComponent;
import com.holler.app.di.app.components.login.modules.LoginModule;
import com.holler.app.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EmailView extends CustomActivity implements LoginPresenter.View{

    @Inject public LoginPresenter presenter;

    @BindView(R.id.la_email)
    public TextView emailView;


    private void setupComponent(){
        ButterKnife.bind(this);
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
        DaggerLoginComponent.builder()
                .appComponent(appComponent)
                .loginModule(new LoginModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        setupComponent();

        emailView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                goToPassword();
            }
            return false;
        });
    }

    @OnClick(R.id.la_back_button)
    public void goToWelcome(){
        presenter.goToWelcomeView();
    }

    @OnClick(R.id.la_button_next)
    public void goToPassword(){
        String email = emailView.getText().toString();
        presenter.goToPasswordView(new LoginPresenter.PendingCredentials(email,null));
    }

    @Override
    public void setupFields(LoginPresenter.PendingCredentials credentials) {
        emailView.setText(credentials.getEmail());
    }

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void onLoadingStarted() {
        onMessage("Loading...");

    }

    @Override
    public void onLoadingFinished() {

    }
}
