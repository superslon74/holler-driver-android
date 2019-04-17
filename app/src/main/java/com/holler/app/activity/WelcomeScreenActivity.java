package com.holler.app.activity;

import android.content.Intent;
import android.os.Bundle;


import com.google.android.material.tabs.TabLayout;
import com.holler.app.Fragment.WelcomeScreenSlideFragment;
import com.holler.app.R;
import com.holler.app.utils.CustomActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.ButterKnife;
import butterknife.OnClick;

//import butterknife.ButterKnife;
//import butterknife.OnClick;

public class WelcomeScreenActivity extends CustomActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
        ButterKnife.bind(this);
        initSlider();
    }

    @OnClick(R.id.sign_in_btn)
    public void gotoLogin(){
        Intent i = new Intent(WelcomeScreenActivity.this, ActivityEmail.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }
    @OnClick(R.id.sign_up_btn)
    public void gotoRegistration(){
        Intent i = new Intent(WelcomeScreenActivity.this, RegisterActivity.class);
        i.putExtra("signup", true).putExtra("viewpager", "yes");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @OnClick(R.id.social_layout)
    public void gotoSocialLogin(){
        Intent i = new Intent(WelcomeScreenActivity.this, ActivitySocialLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void initSlider(){
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        List<Fragment> slides = new ArrayList<>();
        slides.add(WelcomeScreenSlideFragment.newInstance(R.string.mas_header_welcome,R.string.mas_description_welcome,R.drawable.welcome_sample));
        slides.add(WelcomeScreenSlideFragment.newInstance(R.string.mas_header_find,R.string.mas_description_find,R.drawable.drive_sample));
        slides.add(WelcomeScreenSlideFragment.newInstance(R.string.mas_header_notify,R.string.mas_description_notify,R.drawable.earn_sample));

        viewPager.setAdapter(new SliderAdapter(getSupportFragmentManager(), slides));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class SliderAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();

        public SliderAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public int getCount()  {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

    }

}

