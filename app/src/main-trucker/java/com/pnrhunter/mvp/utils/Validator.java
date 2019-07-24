package com.pnrhunter.mvp.utils;

import android.app.Application;
import android.content.Context;

import com.pnrhunter.R;

import java.util.regex.Pattern;

public class Validator {

    private Throwable VALIDATION_ERROR_EMAIL_EMPTY;
    private Throwable VALIDATION_ERROR_EMAIL;

    private Throwable VALIDATION_ERROR_PASSWORD_EMPTY;
    private Throwable VALIDATION_ERROR_PASSWORD ;
    private Throwable VALIDATION_ERROR_PASSWORD_MISMATCHED;

    private Throwable VALIDATION_ERROR_NAME_EMPTY;
    private Throwable VALIDATION_ERROR_NAME;

    private Throwable VALIDATION_ERROR_OTP_MISMATCHED;
    private Throwable VALIDATION_ERROR_OTP_EMPTY;

    private Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[a-z])(?=.*[\\d]).{8,16}");
    private Pattern NAME_PATTERN = Pattern.compile("\\p{L}+");
    private Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");


    private Context context;

    public Validator(Context c) {
        this.context = c;

        VALIDATION_ERROR_EMAIL_EMPTY = createByStringResId(R.string.error_validation_email_empty);
        VALIDATION_ERROR_EMAIL = createByStringResId(R.string.error_validation_email);

        VALIDATION_ERROR_PASSWORD_EMPTY = createByStringResId(R.string.error_validation_password_empty);
        VALIDATION_ERROR_PASSWORD = createByStringResId(R.string.error_validation_password);
        VALIDATION_ERROR_PASSWORD_MISMATCHED = createByStringResId(R.string.error_validation_password_mismatched);

        VALIDATION_ERROR_NAME_EMPTY = createByStringResId(R.string.error_validation_name_empty);
        VALIDATION_ERROR_NAME = createByStringResId(R.string.error_validation_name);

        VALIDATION_ERROR_OTP_MISMATCHED = createByStringResId(R.string.error_validation_otp_empty);
        VALIDATION_ERROR_OTP_EMPTY = createByStringResId(R.string.error_validation_otp_mismatched);
    }

    public Throwable validateEmail(String email){
        if (email == null || email.length() == 0)
            return VALIDATION_ERROR_EMAIL_EMPTY;
        if(!EMAIL_PATTERN.matcher(email).matches())
            return VALIDATION_ERROR_EMAIL;

        return null;
    }

    public Throwable validatePassword(String password, String passwordConfirmation){
        if (password == null || password.length() == 0)
            return VALIDATION_ERROR_PASSWORD_EMPTY;
        if(!password.equals(passwordConfirmation))
            return VALIDATION_ERROR_PASSWORD_MISMATCHED;
        if(!PASSWORD_PATTERN.matcher(password).matches())
            return VALIDATION_ERROR_PASSWORD;
        return null;
    }

    public Throwable createByStringResId(int res){
        String message = context.getString(res);
        return new Throwable(message);
    }

    public Throwable validateOtp(String input, String code) {
        if(input==null || input.length()==0)
            return VALIDATION_ERROR_OTP_EMPTY;
        if(!input.equals(code))
            return VALIDATION_ERROR_OTP_MISMATCHED;

        return null;
    }

    public Throwable validateName(String name) {
        if(name==null || name.length()==0)
            return VALIDATION_ERROR_NAME_EMPTY;
        if(!NAME_PATTERN.matcher(name).matches())
            return VALIDATION_ERROR_NAME;
        return null;

    }
}
