package com.andar.hand.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.andar.hand.Helper.CustomDialog;
import com.andar.hand.Helper.LocaleUtils;
import com.andar.hand.Helper.SharedHelper;
import com.andar.hand.R;


public class ActivitySettings extends AppCompatActivity {

    private RadioButton radioEnglish, radioArabic;

    private LinearLayout lnrEnglish, lnrArabic;

    private CustomDialog customDialog;

    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init() {

        radioEnglish = (RadioButton) findViewById(R.id.radioEnglish);
        radioArabic = (RadioButton) findViewById(R.id.radioArabic);

        lnrEnglish = (LinearLayout) findViewById(R.id.lnrEnglish);
        lnrArabic = (LinearLayout) findViewById(R.id.lnrArabic);

        backArrow = (ImageView) findViewById(R.id.backArrow);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("en")){
            radioEnglish.setChecked(true);
        }else if (SharedHelper.getKey(ActivitySettings.this, "language").equalsIgnoreCase("ar")){
            radioArabic.setChecked(true);
        }else{
            radioEnglish.setChecked(true);
        }

        lnrEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioArabic.setChecked(false);
                radioEnglish.setChecked(true);
            }
        });

        lnrArabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEnglish.setChecked(false);
                radioArabic.setChecked(true);
            }
        });

        radioArabic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    radioEnglish.setChecked(false);
                    SharedHelper.putKey(ActivitySettings.this, "language", "ar");
                    setLanguage();
//                    recreate();
                    GoToMainActivity();
                }
            }
        });

        radioEnglish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    radioArabic.setChecked(false);
                    SharedHelper.putKey(ActivitySettings.this, "language", "en");
                    setLanguage();
//                    recreate();
                    GoToMainActivity();
                }
            }
        });
    }

    public void GoToMainActivity(){
        customDialog = new CustomDialog(ActivitySettings.this, getResources().getString(R.string.language_update));
        if (customDialog != null)
            customDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                customDialog.dismiss();
                Intent mainIntent = new Intent(ActivitySettings.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }
        }, 3000);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleUtils.onAttach(base));
    }

    private void setLanguage() {
        String languageCode = SharedHelper.getKey(ActivitySettings.this, "language");
        LocaleUtils.setLocale(this, languageCode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
