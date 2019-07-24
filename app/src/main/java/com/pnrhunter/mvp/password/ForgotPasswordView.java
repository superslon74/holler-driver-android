package com.pnrhunter.mvp.password;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.pnrhunter.HollerApplication;
import com.pnrhunter.R;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.DaggerChangePasswordComponent;
import com.pnrhunter.di.app.components.password.modules.PasswordModule;
import com.pnrhunter.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ForgotPasswordView extends CustomActivity implements ChangePasswordPresenter.View{

    @Inject
    public ChangePasswordPresenter presenter;

    @BindView(R.id.pa_email)
    public EditText emailInput;

    @BindView(R.id.pa_button_next)
    public View buttonNext;

    private void setupComponent() {
        AppComponent appComponent = (AppComponent) HollerApplication.getInstance().component();
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

//    @OnClick(R.id.pa_back_button)
//    public void back(){
//        presenter.goToLoginPasswordView();
//    }


    @Override
    public void onFinish() {
        finish();
    }
}
