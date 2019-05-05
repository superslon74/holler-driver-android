package com.holler.app.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.holler.app.di.User;
import com.holler.app.utils.CustomActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.holler.app.Helper.ConnectionHelper;
import com.holler.app.R;

import androidx.core.content.ContextCompat;


public class ShowProfile extends CustomActivity {

    public Context context = ShowProfile.this;
    public Activity activity = ShowProfile.this;
    String TAG = "ShowActivity";
    ConnectionHelper helper;
    Boolean isInternet;
    ImageView backArrow;
    TextView email, first_name, last_name, mobile_no, services_provided;
    ImageView profile_Image;
    RatingBar ratingProvider;
    String strUserId = "", strServiceRequested = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        setContentView(R.layout.activity_show_profile);
        findViewByIdandInitialization();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void findViewByIdandInitialization() {
        email = (TextView) findViewById(R.id.email);
        first_name = (TextView) findViewById(R.id.first_name);
        last_name = (TextView) findViewById(R.id.last_name);
        mobile_no = (TextView) findViewById(R.id.mobile_no);
        //services_provided = (TextView) findViewById(R.id.services_provided);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        profile_Image = (ImageView) findViewById(R.id.img_profile);
        ratingProvider = (RatingBar) findViewById(R.id.ratingProvider);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();

        User user = getIntent().getParcelableExtra("user");
        if(user ==null) return;

            email.setText(user.email);
            first_name.setText(user.firstName);
            mobile_no.setText((user.mobile!=null&&!"".equals(user.mobile))?user.mobile:"No valid number.");
            last_name.setText(user.lastName);

            ratingProvider.setRating((user.rating!=null&&!"".equals(user.rating))?Float.parseFloat(user.rating):1);

        Glide
                .with(getApplicationContext())
                .load(user.avatar)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(profile_Image);

    }

    public void displayMessage(String toastString) {
        Toast.makeText(context, toastString + "", Toast.LENGTH_SHORT).show();
    }


}
