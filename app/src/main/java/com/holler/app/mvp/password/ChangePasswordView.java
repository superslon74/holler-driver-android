package com.holler.app.mvp.password;

import android.os.Bundle;
import android.widget.EditText;

import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerChangePasswordComponent;
import com.holler.app.di.app.components.password.modules.PasswordModule;
import com.holler.app.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePasswordView extends CustomActivity implements ChangePasswordPresenter.View{

    @Inject
    public ChangePasswordPresenter presenter;
    @BindView(R.id.pa_code)
    public EditText codeInput;
    @BindView(R.id.pa_password)
    public EditText passwordInput;
    @BindView(R.id.pa_password_confirmation)
    public EditText passwordConfirmationInput;

    private void setupComponent() {
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
        DaggerChangePasswordComponent.builder()
                .appComponent(appComponent)
                .passwordModule(new PasswordModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);
        ButterKnife.bind(this);
        setupComponent();

    }

    @OnClick(R.id.pa_button_next)
    public void next(){
        String code = codeInput.getText().toString();
        String password = passwordInput.getText().toString();
        String passwordConfirmation = passwordConfirmationInput.getText().toString();
        presenter.sendNewPassword(code,password,passwordConfirmation);
    }

    @OnClick(R.id.pa_back_button)
    public void back(){
        presenter.goToForgotPasswordView();
    }

    @Override
    public void onFinish() {
        finish();
    }
}
