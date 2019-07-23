package com.pnrhunter.mvp.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.pnrhunter.R;

public class UserStatusToggle extends View {

    private static final int[] STATE_USER_ONLINE = {R.attr.ust_online};
    private static final int[] STATE_USER_OFFLINE = {R.attr.ust_offline};
    private static final int[] STATE_USER_DISAPPROVED= {R.attr.ust_disapproved};
    private static final int[] STATE_USER_BLOCKED = {R.attr.ust_blocked};
    private static final int[] STATE_USER_RIDING = {R.attr.ust_riding};

    private boolean isUserOnline;
    private boolean isUserOffline;
    private boolean isUserDisapproved;
    private boolean isUserBlocked;
    private boolean isUserRiding;

    public UserStatusToggle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UserStatusToggle,
                0, 0);

        try {
            isUserOnline = a.getBoolean(R.styleable.UserStatusToggle_ust_online, false);
            isUserOffline = a.getBoolean(R.styleable.UserStatusToggle_ust_offline, false);
            isUserDisapproved = a.getBoolean(R.styleable.UserStatusToggle_ust_disapproved, false);
            isUserBlocked = a.getBoolean(R.styleable.UserStatusToggle_ust_blocked, false);
            isUserRiding = a.getBoolean(R.styleable.UserStatusToggle_ust_riding, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 5);
        if(isUserOnline) mergeDrawableStates(drawableState,STATE_USER_ONLINE);
        if(isUserOffline) mergeDrawableStates(drawableState,STATE_USER_OFFLINE);
        if(isUserDisapproved) mergeDrawableStates(drawableState,STATE_USER_DISAPPROVED);
        if(isUserBlocked) mergeDrawableStates(drawableState,STATE_USER_BLOCKED);
        if(isUserRiding) mergeDrawableStates(drawableState,STATE_USER_RIDING);
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

    public boolean isOnline(){
        return isUserOnline;
    }
    public boolean isOffline(){
        return isUserOffline;
    }
    public boolean isDisapproved(){
        return isUserDisapproved;
    }
    public boolean isBlocked(){
        return isUserBlocked;
    }
    public boolean isRiding(){
        return isUserRiding;
    }
}
