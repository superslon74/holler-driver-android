package com.pnrhunter.mvp.login;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.pnrhunter.HollerApplication;
import com.pnrhunter.R;
import com.pnrhunter.di.app.AppComponent;
import com.pnrhunter.di.app.components.DaggerLoginComponent;
import com.pnrhunter.di.app.components.login.modules.LoginModule;
import com.pnrhunter.utils.CustomActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class PasswordView extends CustomActivity implements LoginPresenter.View {

    @Inject
    public LoginPresenter presenter;

    @BindView(R.id.la_password)
    public TextView passwordView;
    @BindView(R.id.la_footer)
    public View footer;
    @BindView(R.id.la_button_next)
    public View buttonNext;

    private void setupComponent() {
        ButterKnife.bind(this);
        AppComponent appComponent = (AppComponent) HollerApplication.getInstance().component();
        DaggerLoginComponent.builder()
                .appComponent(appComponent)
                .loginModule(new LoginModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);
        setupComponent();

        passwordView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                goToMain();
            }
            return false;
        });
    }

    @Override
    public void onKeyboardShown() {
        super.onKeyboardShown();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animation.setStartOffset(200);
        buttonNext.setAnimation(animation);
        footer.setVisibility(View.GONE);

//        ViewGroup.LayoutParams layoutParams = ((ViewGroup)buttonNext).getLayoutParams();
//        ((ViewGroup.MarginLayoutParams)layoutParams).bottomMargin = 200;
    }

    @Override
    public void onKeyboardHidden() {
        super.onKeyboardHidden();
        footer.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
        footer.setVisibility(View.VISIBLE);
//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)((ViewGroup)buttonNext).getLayoutParams();
//        layoutParams.bottomMargin = 200;

    }

//    @OnClick(R.id.h_button_back)
//    public void goToEmail() {
//        String password = passwordView.getText().toString();
//        presenter.goToEmailView(new LoginPresenter.PendingCredentials(null, password));
//    }

    @OnClick(R.id.la_button_next)
    public void goToMain() {
        String password = passwordView.getText().toString();
        presenter.goToMainView(new LoginPresenter.PendingCredentials(null, password));
    }

    @OnClick(R.id.la_forgot_password)
    public void goToForgotPassword(){
        presenter.goToForgotPasswordView();
    }

    @Override
    public void setupFields(LoginPresenter.PendingCredentials credentials) {
        passwordView.setText(credentials.getPassword());
    }

}
