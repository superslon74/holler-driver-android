package com.pnrhunter.mvp.welcome;

import android.os.Bundle;
import android.view.View;


import com.google.android.material.tabs.TabLayout;
import com.pnrhunter.HollerApplication;
import com.pnrhunter.R;
import com.pnrhunter.di.app.modules.RetrofitModule;
import com.pnrhunter.di.app.modules.RouterModule;
import com.pnrhunter.utils.CustomActivity;
import com.pnrhunter.utils.LoadingView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeView extends CustomActivity {

    @Inject public RouterModule.Router router;
    @Inject public RetrofitModule.ServerAPI serverAPI;

    @BindView(R.id.loading_view)
    protected LoadingView loadingView;

    @BindView(R.id.social_layout)
    protected View socialLayoutView;

    private boolean isSocialEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HollerApplication.getInstance().component().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
        ButterKnife.bind(this);

        serverAPI
                .getSocialLoginStatus()
                .doOnSubscribe(disposable -> showSpinner())
                .doOnSuccess(response -> {
                    isSocialEnabled = response.isEnabled;
                    if(isSocialEnabled){
                        socialLayoutView.setVisibility(View.VISIBLE);
                    }else{
                        socialLayoutView.setVisibility(View.INVISIBLE);
                    }
                })
                .doFinally(() -> hideSpinner())
                .subscribe();

        initSlider();
    }

    @OnClick(R.id.sign_in_btn)
    public void gotoLogin(){
        router.goToEmailScreen();
    }

    @OnClick(R.id.sign_up_btn)
    public void gotoRegistration() {
        router.goToRegisterScreen();
    }

    @OnClick(R.id.social_layout)
    public void gotoSocialLogin() {
        if(isSocialEnabled){
            router.gotoSocialLogin();
        }
    }

    private void initSlider() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        List<Fragment> slides = new ArrayList<>();
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_welcome, R.string.was_description_welcome, R.drawable.welcome_sample));
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_find, R.string.was_description_find, R.drawable.drive_sample));
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_notify, R.string.was_description_notify, R.drawable.earn_sample));

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
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

    }

}

