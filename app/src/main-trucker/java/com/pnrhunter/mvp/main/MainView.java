package com.pnrhunter.mvp.main;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.orhanobut.logger.Logger;
import com.pnrhunter.R;
import com.pnrhunter.mvp.utils.activity.ExtendedActivity;
import com.pnrhunter.mvp.utils.server.objects.User;
import com.pnrhunter.mvp.utils.server.objects.order.OrderResponse;
import com.pnrhunter.mvp.utils.server.objects.order.RequestedOrderResponse;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainView extends ExtendedActivity implements MainPresenter.View {
    @Inject
    public MainPresenter presenter;

    @BindView(R.id.ma_nav) protected NavigationView navigationView;
    @BindView(R.id.ma_drawer) protected DrawerLayout drawerView;
    @BindView(R.id.ma_map_container) protected View mapContainer;
    @BindView(R.id.ma_order_container) protected View orderContainer;
    @BindView(R.id.ma_content_overflow) protected View contentOverflow;
    @BindView(R.id.ma_map_nav_open_dot) protected UserStatusDot openMenuUserStatusDot;
    @BindView(R.id.ma_map_nav_open_button) protected ImageView gamburger;

    @BindView(R.id.ma_offline_header) protected TextView offlineHeader;
    @BindView(R.id.ma_offline_status_toggle) protected UserStatusToggle offlineStatusTooggle;

    protected OrderContainerFragment orderDispatcher;

    protected HeaderViewHolder headerViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        ButterKnife.bind(this);
//        displayGamburger(false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupMenu();
        insertMap();
        insertOrderDispatcher();


//        fragmentRouter = new FragmentRouter();
//        fragmentRouter.openMap();

    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.dispatchOrders();
    }

    private void insertOrderDispatcher() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        orderDispatcher = new OrderContainerFragment();

        fragmentManager
                .beginTransaction()
                .replace(orderContainer.getId(), orderDispatcher)
                .commitNow();
    }


    private void insertMap() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment map = new MapFragment();

        fragmentManager
                .beginTransaction()
                .replace(mapContainer.getId(), map)
                .commit();
    }

    private void setupMenu(){

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            Logger.i("Menu item selected id: " + menuItem);
            drawerView.closeDrawer(GravityCompat.START);
//            switch (menuItem.getItemId()) {
//                case R.id.ma_nav_payment:
//                    fragmentRouter.openPayment();
//                    break;
////                case R.id.ma_nav_trips:
////                    fragmentRouter.openTrips();
////                    break;
////                case R.id.ma_nav_coupon:
////                    break;
////                case R.id.ma_nav_wallet:
////                    fragmentRouter.openWallet();
////                    break;
//                case R.id.ma_nav_help:
//                    fragmentRouter.openHelp();
//                    break;
//                case R.id.ma_nav_share:
//                    fragmentRouter.openShare();
//                    break;
//                case R.id.ma_nav_documents:
//                    fragmentRouter.openDocuments();
//                    break;
//                case R.id.ma_nav_logout:
//                    logout();
//                    break;
////                case R.id.ma_nav_crash:
////                    throw new RuntimeException("Log crash");
//            }

            return true;
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerView,
                0, 0
        );

        drawerView.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        headerViewHolder = new HeaderViewHolder(navigationView.getHeaderView(0));

        User profile = presenter.getUser();
        headerViewHolder.userNameView.setText(profile.firstName + " " + profile.lastName);
        //TODO: test

        Glide
                .with(MainView.this)
                .load(profile.avatar)
                .error(R.drawable.img_avatar)
                .into(headerViewHolder.userPhotoView);
    }

    @Override
    public void showOrder(RequestedOrderResponse order) {
        orderDispatcher.setOrderRequest(order);
    }

    protected class HeaderViewHolder {
        @BindView(R.id.ma_nav_user_state_toggle)
        public UserStatusToggle statusToggle;
        @BindView(R.id.ma_nav_user_status)
        public UserStatusDot statusDot;

        @BindView(R.id.ma_nav_user_name)
        public TextView userNameView;
        @BindView(R.id.ma_nav_user_photo)
        public CircularImageView userPhotoView;

        @OnClick(R.id.ma_nav_user_state_toggle)
        public void toggleStatus() {
            Logger.i("User online" + this.statusToggle.isOnline());
            if (this.statusToggle.isOnline()) {
                statusToggle.setOffline();
                presenter.goOffline();
            } else if (this.statusToggle.isOffline()) {
                statusToggle.setOnline();
                presenter.goOnline();
            }
        }

        @OnClick(R.id.ma_nav_button_close)
        public void closeNavigation() {
            drawerView.closeDrawer(GravityCompat.START);
        }

        @OnClick(R.id.ma_nav_user_photo)
        public void openProfile() {
//            fragmentRouter.openProfile();
        }

        public HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @OnClick(R.id.ma_map_nav_open_button)
    public void toggleNavigationMenu() {
        if (drawerView.isDrawerOpen(GravityCompat.START)) {
            drawerView.closeDrawers();
        } else {
            drawerView.openDrawer(GravityCompat.START);
        }
    }

}
