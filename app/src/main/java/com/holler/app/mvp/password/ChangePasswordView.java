package com.holler.app.mvp.password;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    @BindView(R.id.pa_button_next)
    public View buttonNext;

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

    @Override
    public void onKeyboardShown() {
        super.onKeyboardShown();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animation.setStartOffset(200);
        buttonNext.setAnimation(animation);
    }

    @Override
    public void onKeyboardHidden() {
        super.onKeyboardHidden();
    }
//
//    @OnClick(R.id.pa_back_button)
//    public void back(){
//        presenter.goToForgotPasswordView();
//    }

    @Override
    public void onFinish() {
        finish();
    }
}
