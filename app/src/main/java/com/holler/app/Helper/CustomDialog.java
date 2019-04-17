package com.holler.app.Helper;

import android.content.Context;
import android.widget.Toast;

import com.holler.app.R;


public class CustomDialog  extends Toast{



    public CustomDialog(Context context) {
        super(context);
        makeText(context,context.getResources().getString(R.string.please_wait),Toast.LENGTH_LONG);
        //  setContentView(R.layout.custom_dialog);
    }

    public CustomDialog(Context context, String message) {
        super(context);
        makeText(context,message,Toast.LENGTH_LONG);
        //  setContentView(R.layout.custom_dialog);
    }

    public void setCancelable(boolean b) {
    }

    public void dismiss() {
    }

    public boolean isShowing() {
        return false;
    }
}
