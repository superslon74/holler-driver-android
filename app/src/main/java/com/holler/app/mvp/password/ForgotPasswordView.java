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

public class ForgotPasswordView extends CustomActivity implements ChangePasswordPresenter.View{

    @Inject
    public ChangePasswordPresenter presenter;

    @BindView(R.id.pa_email)
    public EditText emailInput;

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
        setContentView(R.layout.activity_password_forgot);
        ButterKnife.bind(this);
        setupComponent();

//        emailView.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
////                goToPassword();
//            }
//            return false;
//        });
    }

    @OnClick(R.id.pa_button_next)
    public void next(){
        String email = emailInput.getText().toString();
        presenter.sendCodeOnMailAddress(email);
    }

//    @OnClick(R.id.pa_back_button)
//    public void back(){
//        presenter.goToLoginPasswordView();
//    }


    @Override
    public void onFinish() {
        finish();
    }
}
