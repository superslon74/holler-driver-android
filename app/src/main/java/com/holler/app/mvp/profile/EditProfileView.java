package com.holler.app.mvp.profile;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.holler.app.AndarApplication;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerProfileComponent;
import com.holler.app.di.app.components.DaggerRegisterComponent;
import com.holler.app.di.app.components.profile.modules.ProfileModule;
import com.holler.app.di.app.components.register.modules.RegisterModule;
import com.holler.app.mvp.profile.ProfilePresenter;
import com.holler.app.utils.CustomActivity;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EditProfileView
        extends CustomActivity
        implements ProfilePresenter.View {



    private void setupComponent(){
        AppComponent appComponent = (AppComponent) AndarApplication.getInstance().component();
        DaggerProfileComponent.builder()
                .appComponent(appComponent)
                .profileModule(new ProfileModule(this))
                .build()
                .inject(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        ButterKnife.bind(this);
        setupComponent();


    }

}
