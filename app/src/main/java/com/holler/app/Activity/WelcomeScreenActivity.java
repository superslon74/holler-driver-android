package com.holler.app.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
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
import com.holler.app.utils.CustomActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.zip.Inflater;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class WelcomeScreenActivity extends CustomActivity {

    private int[] slideResourceIds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Button loginButton = (Button) findViewById(R.id.sign_in_btn);
        Button signUpButton = (Button) findViewById(R.id.sign_up_btn);
        LinearLayout social_layout = (LinearLayout) findViewById(R.id.social_layout);

        initSlider();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    }

    private void initSlider(){
        slideResourceIds = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3
        };

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        viewPager.setAdapter(new SliderAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void calculateTextLayoutHeigth(View viewPager, int slideId){
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View slideLayout = layoutInflater.inflate(slideId, (ViewGroup) viewPager);
        viewPager.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        slideLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        int height1 = slideLayout.getMeasuredHeight();
        int height2 = slideLayout.getHeight();

        View textLayout = slideLayout.findViewById(R.id.ma_text_layout);
//        int height1 = viewPager.getMeasuredHeight();
//        int height2 = viewPager.getHeight();

        if(textLayout!=null){
            textLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int measuredHeight = textLayout.getMeasuredHeight();
            int height = textLayout.getHeight();
            Log.d("AZAZA",measuredHeight+"");
        }
    }


    public class SliderAdapter extends PagerAdapter {
        private int textLayoutMaxHeight = 0;

        public SliderAdapter() {
        }

        public SliderAdapter(int textLayoutHeight) {
            super();
            textLayoutMaxHeight = textLayoutHeight;
        }

        @Override
        public int getCount()  {
            return slideResourceIds.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View slide = layoutInflater.inflate(slideResourceIds[position], container, false);
            View textLayout = slide.findViewById(R.id.ma_text_layout);
            textLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int height1 = textLayout.getMeasuredHeight();
            int height = textLayout.getHeight();
            if(height>textLayoutMaxHeight) textLayoutMaxHeight = height;
//            textLayout.setMinimumHeight(textLayoutMaxHeight);
            container.addView(slide);
            return slide;
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

}

