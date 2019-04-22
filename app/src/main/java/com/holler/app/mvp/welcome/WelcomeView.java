package com.holler.app.mvp.welcome;

import android.os.Bundle;


import com.google.android.material.tabs.TabLayout;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.modules.RouterModule;
import com.holler.app.utils.CustomActivity;
import com.holler.app.utils.LoadingProgress;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeView extends CustomActivity {

    @Inject
    public RouterModule.Router router;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndarApplication.getInstance().component().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
        ButterKnife.bind(this);
        initSlider();
    }

    @OnClick(R.id.sign_in_btn)
    public void gotoLogin(){
        router.goToEmailScreen();
    }

    @OnClick(R.id.sign_up_btn)
    public void gotoRegistration(){
        router.goToRegisterScreen();
    }

    @OnClick(R.id.social_layout)
    public void gotoSocialLogin(){
        router.gotoSocialLogin();
    }

    private void initSlider(){
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        List<Fragment> slides = new ArrayList<>();
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_welcome,R.string.was_description_welcome,R.drawable.welcome_sample));
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_find,R.string.was_description_find,R.drawable.drive_sample));
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_notify,R.string.was_description_notify,R.drawable.earn_sample));

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

