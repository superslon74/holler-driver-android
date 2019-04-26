package com.holler.app.mvp.main;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.navigation.NavigationView;
import com.holler.app.AndarApplication;
import com.holler.app.Fragment.Map;
import com.holler.app.R;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerMainComponent;
import com.holler.app.di.app.components.main.modules.MainScreenModule;
import com.holler.app.utils.CustomActivity;
import com.orhanobut.logger.Logger;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainView extends CustomActivity implements MainPresenter.View {

    @Inject
    public MainPresenter presenter;

    @BindView(R.id.ma_nav)
    public NavigationView navigationView;
    @BindView(R.id.ma_drawer)
    public DrawerLayout drawerView;

    @BindView(R.id.ma_content)
    public View content;

    private FragmentRouter router;

    protected HeaderViewHolder headerViewHolder;

    private void buildComponent(){
        AppComponent appComponent = AndarApplication.getInstance().component();
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .mainScreenModule(new MainScreenModule(this))
                .build()
                .inject(this);
    }

    protected class HeaderViewHolder{
        @BindView(R.id.ma_nav_user_state_toggle)
        public UserStatusToggle statusToggle;
        @BindView(R.id.ma_nav_user_status)
        public UserStatusDot statusDot;

        @OnClick(R.id.ma_nav_user_state_toggle)
        public void toggleStatus(){
            Logger.i("User online"+this.statusToggle.isOnline());
            if(this.statusToggle.isOnline()){
                statusToggle.setOffline();
                statusDot.setOffline();
                try {
                    ((MapFragment) router.currentFragment).userStatusDot.setOffline();
                }catch (Exception e){}
            }else{
                statusToggle.setOnline();
                statusDot.setOnline();
                try {
                    ((MapFragment) router.currentFragment).userStatusDot.setOnline();
                }catch (Exception e){}
            }
        }
        
        @OnClick(R.id.ma_nav_button_close)
        public void closeNavigation(){
            drawerView.closeDrawer(Gravity.LEFT);
        }

        public HeaderViewHolder(View view) {
            ButterKnife.bind(this,view);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        ButterKnife.bind(this);
        headerViewHolder = new HeaderViewHolder(navigationView.getHeaderView(0));
        buildComponent();
        router = new FragmentRouter();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupNavView();

        router.openMap();
    }



    private void setupNavView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            Logger.i("Menu item selected id: "+menuItem);
            drawerView.closeDrawer(Gravity.LEFT);
            return true;
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerView,
                0,0
        );

        drawerView.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }





    protected class FragmentRouter {
        private FragmentManager fragmentManager;
        public Fragment currentFragment;

        public FragmentRouter() {
            fragmentManager = getSupportFragmentManager();
        }

        public void openMap(){
            currentFragment = new MapFragment();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(),currentFragment)
                    .commit();
        }
    }

}
