package com.holler.app.mvp.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.holler.app.AndarApplication;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerTripsComponent;
import com.holler.app.di.app.components.trips.modules.TripDetailsModule;
import com.holler.app.di.app.components.trips.modules.TripsModule;
import com.holler.app.utils.CustomActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripsView extends CustomActivity implements TripsPresenter.View{

    @Inject protected TripsPresenter presenter;

    @BindView(R.id.ta_pager) protected ViewPager viewPager;
    @BindView(R.id.ta_tabs) protected TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);
        buildComponent();
        ButterKnife.bind(this);
        Map<String, String> titlesMap = new HashMap<>();
        titlesMap.put(DetailsView.TYPE_PAST_TRIPS,getResources().getString(R.string.past_trips));
        titlesMap.put(DetailsView.TYPE_UPCOMING_TRIPS,getResources().getString(R.string.upcoming_trips));

        tabLayout.setupWithViewPager(viewPager);
        presenter
                .getTrips()
                .doOnSubscribe(disposable -> {
                    showSpinner();
                })
                .doOnNext(ordersMap -> {
                    List<Fragment> fragments = new ArrayList<>();
                    List<String> types = new ArrayList<>();
                    List<String> titles = new ArrayList<>();
                    for (String type : ordersMap.keySet()) {
                        types.add(type);
                        titles.add(titlesMap.get(type));
                        fragments.add(TripsListFragment.newInstance(new ArrayList<>(ordersMap.get(type))));
                    }
                    runOnUiThread(() -> {
                        viewPager.setAdapter(new TripsListsPagerAdapter(fragments,titles,types,getSupportFragmentManager()));
                        setupTabIcons(titles);
                    });
                })
                .doOnError(throwable -> {
                    showMessage(throwable.getMessage());
                })
                .doOnComplete(() -> {
                    hideSpinner();
                })
                .subscribe();

    }

    public void openDetails(String orderId){
        String type = ((TripsListsPagerAdapter)viewPager.getAdapter()).getType(viewPager.getCurrentItem());
        presenter.goToDetailsScreen(type, orderId);
    }

    private void buildComponent() {
        AppComponent component = AndarApplication.getInstance().component();
        DaggerTripsComponent.builder()
                .appComponent(component)
                .tripsModule(new TripsModule(this))
                .build()
                .inject(this);
    }

    private void setupTabIcons(List<String> titles) {

        TextView tabOne = (TextView) LayoutInflater.from(TripsView.this).inflate(R.layout.layout_custom_tab, null);
        tabOne.setText(titles.get(0));
        tabLayout.getTabAt(0).setCustomView(tabOne);


        TextView tabTwo = (TextView) LayoutInflater.from(TripsView.this).inflate(R.layout.layout_custom_tab, null);
        tabTwo.setText(titles.get(1));
        tabLayout.getTabAt(1).setCustomView(tabTwo);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public class TripsListsPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private List<String> titles;
        private List<String> types;

        public TripsListsPagerAdapter(List<Fragment> fragments, List<String> titles, List<String> types, FragmentManager fm) {
            super(fm);
            this.fragments = fragments;
            this.titles = titles;
            this.types = types;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        public String getType(int position){
            return types.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


}
