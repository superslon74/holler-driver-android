package com.pnrhunter.mvp.register;

import android.app.Fragment;
import android.os.Parcel;

import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.ui.BaseUIManager;
import com.facebook.accountkit.ui.LoginFlowState;

import androidx.annotation.Nullable;

public class AccountKitAdvancedManager extends BaseUIManager {


    public AccountKitAdvancedManager(final int themeResourceId) {
        super(themeResourceId);
    }

    private AccountKitAdvancedManager(final Parcel source) {
        super(source);
    }

    @Override
    @Nullable
    public Fragment getHeaderFragment(final LoginFlowState state) {
        Fragment headerFragment;
        switch (state) {
            case PHONE_NUMBER_INPUT:
            case EMAIL_INPUT:
            case EMAIL_VERIFY:
            case SENDING_CODE:
            case SENT_CODE:
            case CODE_INPUT:
            case VERIFYING_CODE:
            case VERIFIED:
            case ACCOUNT_VERIFIED:
            case CONFIRM_ACCOUNT_VERIFIED:
            case CONFIRM_INSTANT_VERIFICATION_LOGIN:
                // insert appropriate customizations for headerFragment
            case ERROR:
                // handle appropriate error for headerFragment
            default:
                headerFragment = new Fragment();
        }

        return headerFragment;
    }


    @Override
    public void onError(final AccountKitError error) {
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);

    }

    public static final Creator<AccountKitAdvancedManager> CREATOR
            = new Creator<AccountKitAdvancedManager>() {
        @Override
        public AccountKitAdvancedManager createFromParcel(final Parcel source) {
            return new AccountKitAdvancedManager(source);
        }

        @Override
        public AccountKitAdvancedManager[] newArray(final int size) {
            return new AccountKitAdvancedManager[size];
        }
    };
}