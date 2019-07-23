package com.pnrhunter.mvp.authorization;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmailView extends ExtendedActivity implements LoginPresenter.View {

    @Inject
    protected EmailLoginPresenter presenter;

    @BindView(R.id.la_email)
    protected TextView emailView;
    @BindView(R.id.la_footer)
    protected View footer;
    @BindView(R.id.la_button_next)
    protected View buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        ButterKnife.bind(this);

        emailView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onNextButtonPressed();
            }
            return false;
        });

        presenter.onViewReady();
    }


    @Override
    public void onKeyboardShown() {
        super.onKeyboardShown();
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        animation.setStartOffset(200);
        buttonNext.setAnimation(animation);
        footer.setVisibility(View.GONE);
    }

    @Override
    public void onKeyboardHidden() {
        super.onKeyboardHidden();
        footer.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up));
        footer.setVisibility(View.VISIBLE);
    }


    @OnClick(R.id.la_button_next)
    public void onNextButtonPressed(){
        String email = emailView.getText().toString();
        presenter.onNextPressed(email);
    }

    @OnClick(R.id.la_link_sign_ap)
    public void onLinkPressed(){
        presenter.onLinkPressed();
    }

    @Override
    public void setupFields(LoginPresenter.PendingCredentials credentials) {
        emailView.setText(credentials.getEmail());
    }

}
