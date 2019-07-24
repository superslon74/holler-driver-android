package com.pnrhunter.mvp.authorization;

public interface EmailLoginPresenter {
    void onViewReady();

    void onNextPressed(String email);

    void onLinkPressed();
}
