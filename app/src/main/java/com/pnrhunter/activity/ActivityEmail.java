package com.pnrhunter.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pnrhunter.Helper.SharedHelper;
import com.pnrhunter.R;
import com.pnrhunter.Utilities.Utilities;
import com.pnrhunter.mvp.welcome.WelcomeView;
import com.pnrhunter.utils.CustomActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class ActivityEmail extends CustomActivity {

    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email;
    TextView register, forgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        email = (EditText) findViewById(R.id.email);
        nextICON = (FloatingActionButton) findViewById(R.id.nextIcon);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        register = (TextView) findViewById(R.id.register);
        forgetPassword = (TextView) findViewById(R.id.forgetPassword);

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {

                    displayMessage(getString(R.string.email_validation));

                } else {

                    if ((!isValidEmail(email.getText().toString()))) {

                        displayMessage(getString(R.string.email_validation));

                    } else {
                        Utilities.hideKeyboard(ActivityEmail.this);
                        SharedHelper.putKey(ActivityEmail.this, "email", email.getText().toString());
                        Intent mainIntent = new Intent(ActivityEmail.this, ActivityPassword.class);
                        startActivity(mainIntent);
                        finish();
                    }


                }
            }
        });

        Log.d("LALALA","EMAIL");
        Log.d("LALALA",SharedHelper.getKey(ActivityEmail.this, "email"));
        Log.d("LALALA",SharedHelper.getKey(ActivityEmail.this, "password"));


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedHelper.putKey(ActivityEmail.this, "email", "");
                Intent mainIntent = new Intent(ActivityEmail.this, WelcomeView.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                ActivityEmail.this.finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(ActivityEmail.this);
                SharedHelper.putKey(ActivityEmail.this, "password", "");
                Intent mainIntent = new Intent(ActivityEmail.this, RegisterActivity.class);
                mainIntent.putExtra("isFromMailActivity", true);
                startActivity(mainIntent);
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(ActivityEmail.this);
                SharedHelper.putKey(ActivityEmail.this, "password", "");
                Intent mainIntent = new Intent(ActivityEmail.this, ForgetPassword.class);
                mainIntent.putExtra("isFromMailActivity", true);
                startActivity(mainIntent);
            }
        });

    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedHelper.putKey(ActivityEmail.this, "email", "");
        Intent mainIntent = new Intent(ActivityEmail.this, WelcomeView.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        ActivityEmail.this.finish();
    }
}