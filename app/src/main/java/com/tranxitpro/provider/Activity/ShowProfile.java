package com.tranxitpro.provider.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.tranxitpro.provider.Helper.ConnectionHelper;
import com.tranxitpro.provider.Helper.CustomDialog;
import com.tranxitpro.provider.Helper.User;
import com.tranxitpro.provider.R;


/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class ShowProfile extends AppCompatActivity {

    public Context context = ShowProfile.this;
    public Activity activity = ShowProfile.this;
    String TAG = "ShowActivity";
    CustomDialog customDialog;
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
        if (user.getEmail() != null && !user.getEmail().equalsIgnoreCase("null") && user.getEmail().length() > 0)
            email.setText(user.getEmail());
        else
            email.setText("");
        if (user.getFirstName() != null && !user.getFirstName().equalsIgnoreCase("null") && user.getFirstName().length() > 0)
            first_name.setText(user.getFirstName());
        else
            first_name.setText("");
        if (user.getMobile() != null && !user.getMobile().equalsIgnoreCase("null") && user.getMobile().length() > 0)
            mobile_no.setText(user.getMobile());
        else
            mobile_no.setText(getString(R.string.user_no_mobile));
        if (user.getLastName() != null && !user.getLastName().equalsIgnoreCase("null") && user.getLastName().length() > 0)
            last_name.setText(user.getLastName());
        else
            last_name.setText("");
        if (user.getRating() != null && !user.getRating().equalsIgnoreCase("null") && user.getRating().length() > 0)
            ratingProvider.setRating(Float.parseFloat(user.getRating()));
        else
            ratingProvider.setRating(1);
        Picasso.with(context).load(user.getImg()).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(profile_Image);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public void displayMessage(String toastString) {
        Toast.makeText(context, toastString + "", Toast.LENGTH_SHORT).show();
    }


}
