package com.pnrhunter.mvp.welcome;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.activity.LoadingView;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.router.AbstractRouter;
import com.pnrhunter.mvp.utils.server.ServerConfigurationInterface;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeView extends ExtendedActivity {

    @Inject protected AbstractRouter router;
    @Inject protected ServerConfigurationInterface serverConfig;

    @BindView(R.id.loading_view)
    protected LoadingView loadingView;

    @BindView(R.id.social_layout)
    protected View socialLayoutView;

    private boolean isSocialEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
        ButterKnife.bind(this);

        serverConfig
                .checkSocialLoginIsEnabled()
                .doOnSubscribe(disposable -> showSpinner())
                .doOnSuccess(isEnabled -> {
                    isSocialEnabled = isEnabled;
                    if(isEnabled){
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
        router.goTo(AbstractRouter.ROUTE_LOGIN_EMAIL);
    }

    @OnClick(R.id.sign_up_btn)
    public void gotoRegistration() {
        router.goTo(AbstractRouter.ROUTE_REGISTRATION);
    }

    @OnClick(R.id.social_layout)
    public void gotoSocialLogin() {
        if(isSocialEnabled){
            router.goTo(AbstractRouter.ROUTE_LOGIN_SOCIAL);
        }
    }

    private void initSlider() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        List<Fragment> slides = new ArrayList<>();
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_overview, R.string.was_description_overview, R.mipmap.welcome_overview));
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_find, R.string.was_description_find, R.mipmap.welcome_find));
        slides.add(WelcomeViewSlideFragment.newInstance(R.string.was_header_notify, R.string.was_description_notify, R.mipmap.welcome_notify));

        viewPager.setAdapter(new SliderAdapter(this.getSupportFragmentManager(), slides));
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
