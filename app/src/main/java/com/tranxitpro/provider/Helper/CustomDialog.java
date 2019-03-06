package com.tranxitpro.provider.Helper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

import com.tranxitpro.provider.R;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class CustomDialog extends ProgressDialog {

    public CustomDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setIndeterminate(true);
        setMessage(context.getResources().getString(R.string.please_wait));
        //  setContentView(R.layout.custom_dialog);
    }

    public CustomDialog(Context context, String strMessage) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setIndeterminate(true);
        setMessage(strMessage);
        //  setContentView(R.layout.custom_dialog);
    }
}
