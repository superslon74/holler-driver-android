package com.holler.app.utils;

import android.content.Context;

import com.holler.app.AndarApplication;
import com.holler.app.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.IdRes;

public class Validator {

    public static final Throwable VALIDATION_ERROR_EMAIL_EMPTY =
            createByStringResId(R.string.error_validation_email_empty);
    public static final Throwable VALIDATION_ERROR_EMAIL =
            createByStringResId(R.string.error_validation_email);

    public static final Throwable VALIDATION_ERROR_PASSWORD_EMPTY =
            createByStringResId(R.string.error_validation_password_empty);
    public static final Throwable VALIDATION_ERROR_PASSWORD =
            createByStringResId(R.string.error_validation_password);
    public static final Throwable VALIDATION_ERROR_PASSWORD_MISMATCHED =
            createByStringResId(R.string.error_validation_password_mismatched);

    public static final Throwable VALIDATION_ERROR_NAME_EMPTY =
            createByStringResId(R.string.error_validation_name_empty);
    public static final Throwable VALIDATION_ERROR_NAME =
            createByStringResId(R.string.error_validation_name);

    public static final Throwable VALIDATION_ERROR_OTP_MISMATCHED =
            createByStringResId(R.string.error_validation_otp_empty);
    public static final Throwable VALIDATION_ERROR_OTP_EMPTY =
            createByStringResId(R.string.error_validation_otp_mismatched);

    public static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[a-z])(?=.*[\\d]).{8,16}");
    public static final Pattern NAME_PATTERN = Pattern.compile("\\p{L}+");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");


    public static Throwable validateEmail(String email){
        if (email == null || email.length() == 0)
            return VALIDATION_ERROR_EMAIL_EMPTY;
        if(!EMAIL_PATTERN.matcher(email).matches())
            return VALIDATION_ERROR_EMAIL;

        return null;
    }

    public static Throwable validatePassword(String password, String passwordConfirmation){
        if (password == null || password.length() == 0)
            return VALIDATION_ERROR_PASSWORD_EMPTY;
        if(!password.equals(passwordConfirmation))
            return VALIDATION_ERROR_PASSWORD_MISMATCHED;
        if(!PASSWORD_PATTERN.matcher(password).matches())
            return VALIDATION_ERROR_PASSWORD;
        return null;
    }

    public static Throwable createByStringResId(int res){
        Context context = AndarApplication.getInstance().getApplicationContext();
        String message = context.getString(res);
        return new Throwable(message);
    }



    public static Throwable validateOtp(String input, String code) {
        if(input==null || input.length()==0)
            return VALIDATION_ERROR_OTP_EMPTY;
        if(!input.equals(code))
            return VALIDATION_ERROR_OTP_MISMATCHED;

        return null;
    }

    public static Throwable validateName(String name) {
        if(name==null || name.length()==0)
            return VALIDATION_ERROR_NAME_EMPTY;
        if(!NAME_PATTERN.matcher(name).matches())
            return VALIDATION_ERROR_NAME;
        return null;

    }
}
