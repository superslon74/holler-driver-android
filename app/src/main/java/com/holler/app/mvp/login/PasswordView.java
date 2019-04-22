package com.holler.app.mvp.login;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    protected void onKeyboardShown() {
        super.onKeyboardShown();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animation.setStartOffset(200);
        buttonNext.setAnimation(animation);
        footer.setVisibility(View.GONE);
    }

    @Override
    protected void onKeyboardHidden() {
        super.onKeyboardHidden();
        footer.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
        footer.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.la_back_button)
    public void goToEmail() {
        String password = passwordView.getText().toString();
        presenter.goToEmailView(new LoginPresenter.PendingCredentials(null, password));
    }

    @OnClick(R.id.la_button_next)
    public void goToMain() {
        String password = passwordView.getText().toString();
        presenter.goToMainView(new LoginPresenter.PendingCredentials(null, password));
    }

    @Override
    public void setupFields(LoginPresenter.PendingCredentials credentials) {
        passwordView.setText(credentials.getPassword());
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
        super.showLoadingProgress();
    }

    @Override
    public void onLoadingFinished() {
        super.hideLoadingProgress();
    }
}
