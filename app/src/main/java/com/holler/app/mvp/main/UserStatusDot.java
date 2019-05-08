package com.holler.app.mvp.main;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.common.util.ArrayUtils;
import com.holler.app.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.Nullable;

public class UserStatusDot extends View {
    private static final int[] STATE_USER_ONLINE = {R.attr.usd_online};
    private static final int[] STATE_USER_OFFLINE = {R.attr.usd_offline};
    private static final int[] STATE_USER_DISAPPROVED= {R.attr.usd_disapproved};
    private static final int[] STATE_USER_BLOCKED = {R.attr.usd_blocked};
    private static final int[] STATE_USER_RIDING = {R.attr.usd_riding};
    private static final int[] HAS_BORDER = {R.attr.usd_has_border};

    private boolean isUserOnline;
    private boolean isUserOffline;
    private boolean isUserDisapproved;
    private boolean isUserBlocked;
    private boolean isUserRiding;
    private boolean hasBorder;

    public UserStatusDot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UserStatusDot,
                0, 0);

        try {
            isUserOnline = a.getBoolean(R.styleable.UserStatusDot_usd_online, false);
            isUserOffline = a.getBoolean(R.styleable.UserStatusDot_usd_offline, false);
            isUserDisapproved = a.getBoolean(R.styleable.UserStatusDot_usd_disapproved, false);
            isUserBlocked = a.getBoolean(R.styleable.UserStatusDot_usd_blocked, false);
            isUserRiding = a.getBoolean(R.styleable.UserStatusDot_usd_riding, false);
            hasBorder = a.getBoolean(R.styleable.UserStatusDot_usd_has_border, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 6);
        if(isUserOnline) mergeDrawableStates(drawableState,STATE_USER_ONLINE);
        if(isUserOffline) mergeDrawableStates(drawableState,STATE_USER_OFFLINE);
        if(isUserDisapproved) mergeDrawableStates(drawableState,STATE_USER_DISAPPROVED);
        if(isUserBlocked) mergeDrawableStates(drawableState,STATE_USER_BLOCKED);
        if(isUserRiding) mergeDrawableStates(drawableState,STATE_USER_RIDING);
        if(hasBorder) mergeDrawableStates(drawableState,HAS_BORDER);
        return drawableState;
    }

    private void resetState(){
        isUserOnline=false;
        isUserOffline=false;
        isUserDisapproved=false;
        isUserBlocked=false;
        isUserRiding=false;
    }

    public void setOnline(){
        resetState();
        isUserOnline=true;
        refreshDrawableState();
    }
    public void setOffline(){
        resetState();
        isUserOffline=true;
        refreshDrawableState();
    }
    public void setDisapproved(){
        resetState();
        isUserDisapproved=true;
        refreshDrawableState();
    }
    public void setBlocked(){
        resetState();
        isUserBlocked=true;
        refreshDrawableState();
    }
    public void setRiding(){
        resetState();
        isUserRiding=true;
        refreshDrawableState();
    }




}
