package com.holler.app.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.holler.app.Helper.SharedHelper;
import com.holler.app.Models.AccessDetails;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WelcomeScreenActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    Button loginButton, signUpButton;
    TextView skipBtn;
    LinearLayout social_layout;
    ScalePageTransformer scalePageTransformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_welcome_screen);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        loginButton = (Button) findViewById(R.id.sign_in_btn);
        skipBtn = (TextView) findViewById(R.id.skip);
        signUpButton = (Button) findViewById(R.id.sign_up_btn);
        social_layout = (LinearLayout) findViewById(R.id.social_layout);
        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3
        };
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("LALALA", "Welcomescreen");
                Log.d("LALALA", SharedHelper.getKey(WelcomeScreenActivity.this, "email"));
                Log.d("LALALA", SharedHelper.getKey(WelcomeScreenActivity.this, "password"));

                startActivity(new Intent(WelcomeScreenActivity.this, ActivityEmail.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);


//                finish();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeScreenActivity.this, RegisterActivity.class).putExtra("signup", true).putExtra("viewpager", "yes").addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
//                finish();
            }
        });

        social_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeScreenActivity.this, ActivitySocialLogin.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
//                finish();
            }
        });
        overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
//        skipBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(WelcomeScreenActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
//                finish();
//
//            }
//        });
        // adding bottom dots
        addBottomDots(0);
        // making notification bar transparent
        changeStatusBarColor();
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        scalePageTransformer = new ScalePageTransformer(viewPager);

        Log.d("LALALA", "WELCOME to welcome screen");
        Log.d("LALALA", SharedHelper.getKey(WelcomeScreenActivity.this, "email"));
        Log.d("LALALA", SharedHelper.getKey(WelcomeScreenActivity.this, "password"));


            if (SharedHelper.getKey(WelcomeScreenActivity.this, "access_username").equalsIgnoreCase("")
                    && SharedHelper.getKey(WelcomeScreenActivity.this, "access_password").equalsIgnoreCase("")){
                accessKeyAPI();
            }

    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
            dots[currentPage].setTextColor(colorsActive[currentPage]);
            dots[currentPage].startAnimation(animation);
        }

    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }


    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
//            params.setMargins((int) ((position + positionOffset) * 500), 0, 0, 0);
//            viewPager.setLayoutParams(params);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public class ScalePageTransformer implements ViewPager.PageTransformer {
        private static final float SCALE_FACTOR = 0.95f;

        private final ViewPager mViewPager;

        public ScalePageTransformer(ViewPager viewPager) {
            this.mViewPager = viewPager;
        }

        @SuppressLint("NewApi")
        @Override
        public void transformPage(View page, float position) {
            if (position <= 0) {
                // apply zoom effect and offset translation only for pages to
                // the left
                final float transformValue = Math.abs(Math.abs(position) - 1) * (1.0f - SCALE_FACTOR) + SCALE_FACTOR;
                int pageWidth = mViewPager.getWidth();
                final float translateValue = position * -pageWidth;
                page.setScaleX(transformValue);
                page.setScaleY(transformValue);
                if (translateValue > -pageWidth) {
                    page.setTranslationX(translateValue);
                } else {
                    page.setTranslationX(0);
                }
            }
        }

    }
    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public int getCount()  {
            return layouts.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            if (position == 0){
                TextView txtFreshDesc = (TextView) view.findViewById(R.id.txtFreshDesc);
                txtFreshDesc.setText(getResources().getString(R.string.introducing) + " " + AccessDetails.siteTitle + " " +
                        getResources().getString(R.string.fresh_description) + " " + getResources().getString(R.string.app_users));
            }
            container.addView(view);
            return view;
        }
        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    public void accessKeyAPI() {
        Log.d("LALALA", "Access key api from welcome screen");

        JSONObject object = new JSONObject();
        try {
            object.put("username", AccessDetails.username);
            object.put("accesskey",AccessDetails.password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("REFACTORING", "This method should never be called");

//        AndarApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void processResponse(final JSONObject response) {
        try {
            AccessDetails accessDetails = new AccessDetails();
            accessDetails.status = response.optBoolean("status");

            if (accessDetails.status) {
                JSONArray jsonArrayData = response.optJSONArray("data");

                JSONObject jsonObjectData = jsonArrayData.optJSONObject(0);
                accessDetails.id = jsonObjectData.optInt("id");
                accessDetails.clientName = jsonObjectData.optString("client_name");
                accessDetails.email = jsonObjectData.optString("email");
                accessDetails.product = jsonObjectData.optString("product");
                accessDetails.username = jsonObjectData.optString("username");
                SharedHelper.putKey(WelcomeScreenActivity.this, "access_username", accessDetails.username);
                accessDetails.password = jsonObjectData.optString("password");
                SharedHelper.putKey(WelcomeScreenActivity.this, "access_password", accessDetails.password);
                accessDetails.passport = jsonObjectData.optString("passport");
                accessDetails.clientid = jsonObjectData.optInt("clientid");
                accessDetails.serviceurl = jsonObjectData.optString("serviceurl");
                accessDetails.isActive = jsonObjectData.optInt("is_active");
                accessDetails.createdAt = jsonObjectData.optString("created_at");
                accessDetails.updatedAt = jsonObjectData.optString("updated_at");
                accessDetails.isPaid = jsonObjectData.optInt("is_paid");
                accessDetails.isValid = jsonObjectData.optInt("is_valid");

                JSONObject jsonObjectSettings = response.optJSONObject("setting");

                accessDetails.siteTitle = jsonObjectSettings.optString("site_title");
                SharedHelper.putKey(WelcomeScreenActivity.this, "app_name", accessDetails.siteTitle);
                accessDetails.siteLogo = jsonObjectSettings.optString("site_logo");
                accessDetails.siteEmailLogo = jsonObjectSettings.optString("site_email_logo");
                accessDetails.siteIcon = jsonObjectSettings.optString("site_icon");
                accessDetails.site_icon = Utilities.drawableFromUrl(WelcomeScreenActivity.this, accessDetails.siteIcon);
                accessDetails.siteCopyright = jsonObjectSettings.optString("site_copyright");
                accessDetails.providerSelectTimeout = jsonObjectSettings.optString("provider_select_timeout");
                accessDetails.providerSearchRadius = jsonObjectSettings.optString("provider_search_radius");
                accessDetails.basePrice = jsonObjectSettings.optString("base_price");
                accessDetails.pricePerMinute = jsonObjectSettings.optString("price_per_minute");
                accessDetails.taxPercentage = jsonObjectSettings.optString("tax_percentage");
                accessDetails.stripeSecretKey = jsonObjectSettings.optString("stripe_secret_key");
                accessDetails.stripePublishableKey = jsonObjectSettings.optString("stripe_publishable_key");
                accessDetails.cash = jsonObjectSettings.optString("CASH");
                accessDetails.card = jsonObjectSettings.optString("CARD");
                accessDetails.manualRequest = jsonObjectSettings.optString("manual_request");
                accessDetails.defaultLang = jsonObjectSettings.optString("default_lang");
                accessDetails.currency = jsonObjectSettings.optString("currency");
                accessDetails.distance = jsonObjectSettings.optString("distance");
                accessDetails.scheduledCancelTimeExceed = jsonObjectSettings.optString("scheduled_cancel_time_exceed");
                accessDetails.pricePerKilometer = jsonObjectSettings.optString("price_per_kilometer");
                accessDetails.commissionPercentage = jsonObjectSettings.optString("commission_percentage");
                accessDetails.storeLinkAndroid = jsonObjectSettings.optString("store_link_android");
                accessDetails.storeLinkIos = jsonObjectSettings.optString("store_link_ios");
                accessDetails.dailyTarget = jsonObjectSettings.optString("daily_target");
                accessDetails.surgePercentage = jsonObjectSettings.optString("surge_percentage");
                accessDetails.surgeTrigger = jsonObjectSettings.optString("surge_trigger");
                accessDetails.demoMode = jsonObjectSettings.optString("demo_mode");
                accessDetails.bookingPrefix = jsonObjectSettings.optString("booking_prefix");
                accessDetails.sosNumber = jsonObjectSettings.optString("sos_number");
                accessDetails.contactNumber = jsonObjectSettings.optString("contact_number");
                accessDetails.contactEmail = jsonObjectSettings.optString("contact_email");
                accessDetails.socialLogin = jsonObjectSettings.optString("social_login");
            }
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public void displayMessage(String toastString) {
        Toast.makeText(WelcomeScreenActivity.this, toastString, Toast.LENGTH_SHORT).show();
    }
}

