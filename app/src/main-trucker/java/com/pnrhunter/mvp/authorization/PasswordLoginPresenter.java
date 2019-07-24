package com.pnrhunter.mvp.authorization;

public interface PasswordLoginPresenter {
    void goToForgotPasswordView();
    void goToMainView(LoginPresenter.PendingCredentials credentials);
    void onViewReady();
}
