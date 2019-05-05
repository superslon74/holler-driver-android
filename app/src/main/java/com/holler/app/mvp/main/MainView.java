package com.holler.app.mvp.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.holler.app.AndarApplication;
import com.holler.app.Fragment.Help;
import com.holler.app.Fragment.SummaryFragment;
import com.holler.app.Fragment.Wallet;
import com.holler.app.R;
import com.holler.app.activity.ActivitySettings;
import com.holler.app.activity.DocumentsActivity;
import com.holler.app.activity.EditProfile;
import com.holler.app.activity.HistoryActivity;
import com.holler.app.activity.MainActivity;
import com.holler.app.activity.Offline;
import com.holler.app.activity.ShowProfile;
import com.holler.app.di.User;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerMainComponent;
import com.holler.app.di.app.components.main.modules.MainScreenModule;
import com.holler.app.utils.CustomActivity;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;


public class MainView extends CustomActivity implements MainPresenter.View {

    @Inject
    public MainPresenter presenter;

    @BindView(R.id.ma_nav)
    public NavigationView navigationView;
    @BindView(R.id.ma_drawer)
    public DrawerLayout drawerView;
    @BindView(R.id.ma_content)
    public View content;
    @BindView(R.id.ma_content_overflow)
    public View contentOverflow;
    @BindView(R.id.ma_map_nav_open_dot)
    public UserStatusDot openMenuUserStatusDot;
    @BindView(R.id.ma_map_nav_open_button)
    public ImageView gamburger;

    @BindView(R.id.ma_offline_header)
    public TextView offlineHeader;
    @BindView(R.id.ma_offline_status_toggle)
    public UserStatusToggle offlineStatusTooggle;

    private FragmentRouter fragmentRouter;

    protected HeaderViewHolder headerViewHolder;

    private void buildComponent() {
        AppComponent appComponent = AndarApplication.getInstance().component();
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .mainScreenModule(new MainScreenModule(this))
                .build()
                .inject(this);
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
            drawerView.closeDrawer(Gravity.LEFT);
        }

        @OnClick(R.id.ma_nav_user_photo)
        public void openProfile() {
            fragmentRouter.openProfile();
        }

        public HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        ButterKnife.bind(this);
        headerViewHolder = new HeaderViewHolder(navigationView.getHeaderView(0));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setupNavView();

        fragmentRouter = new FragmentRouter();

        buildComponent();

        User profile = presenter.userModel.getProfileData();
        headerViewHolder.userNameView.setText(profile.firstName + " " + profile.lastName);

        Glide
                .with(getApplicationContext())
                .load(profile.avatar)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(headerViewHolder.userPhotoView);

    }

    @OnClick(R.id.ma_map_nav_open_button)
    public void toggleNavigationMenu() {
        if (drawerView.isDrawerOpen(Gravity.LEFT)) {
            drawerView.closeDrawers();
        } else {
            drawerView.openDrawer(Gravity.LEFT);
        }
    }

    @OnClick(R.id.ma_offline_status_toggle)
    public void toggleStatus() {
        if (this.offlineStatusTooggle.isOnline()) {
            offlineStatusTooggle.setOffline();
            presenter.goOffline();
        } else if (this.offlineStatusTooggle.isOffline()) {
            offlineStatusTooggle.setOnline();
            presenter.goOnline();
        }
    }

    @OnClick(R.id.ma_offline_later_button)
    public void changeStatusLater() {
        fragmentRouter.closeOffline();
    }


    @Override
    public void onBackPressed() {
        if (drawerView.isDrawerOpen(Gravity.LEFT)) {
            drawerView.closeDrawer(Gravity.LEFT);
            return;
        }

        if (fragmentRouter.currentFragment instanceof MapFragment) {
            super.onBackPressed();
        } else {
            fragmentRouter.openMap();
        }
    }

    public void logout() {
        showLogoutConfirmation()
                .flatMap(isLogoutConfirmed -> {
                    if (isLogoutConfirmed) {
                        return presenter.logout();
                    } else {
                        return Observable.just(false);
                    }
                })
                .flatMap(isLoggedOut -> {
                    if (isLoggedOut) {
                        presenter.goToWelcomeScreen();
                        return Observable.just(true);
                    } else {
                        return Observable.just(false);
                    }
                })
                .doOnComplete(() -> {
                    Logger.d("Logged out");
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private Subject<Boolean> showLogoutConfirmation() {
        final Subject result = UnicastSubject.create();

        final AlertDialog logoutDialog = new AlertDialog
                .Builder(this)
                .setTitle(getString(R.string.ma_logout_alert_title))
                .setMessage(getString(R.string.ma_logout_alert_message))
                .setPositiveButton(R.string.ma_logout_alert_button_confirm, (dialog, which) -> {
                    result.onNext(true);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.ma_logout_alert_button_cancel, (dialog, which) -> {
                    result.onNext(false);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create();

        logoutDialog.show();

        return result;
    }

    private void setupNavView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            Logger.i("Menu item selected id: " + menuItem);
            drawerView.closeDrawer(Gravity.LEFT);
            switch (menuItem.getItemId()) {
                case R.id.ma_nav_payment:
                    fragmentRouter.openPayment();
                    break;
                case R.id.ma_nav_trips:
                    fragmentRouter.openTrips();
                    break;
                case R.id.ma_nav_coupon:
                    break;
                case R.id.ma_nav_wallet:
                    fragmentRouter.openWallet();
                    break;
                case R.id.ma_nav_settings:
                    fragmentRouter.openSettings();
                    break;
                case R.id.ma_nav_help:
                    fragmentRouter.openHelp();
                    break;
                case R.id.ma_nav_share:
                    fragmentRouter.openShare();
                    break;
                case R.id.ma_nav_documents:
                    fragmentRouter.openDocuments();
                    break;
                case R.id.ma_nav_logout:
                    logout();
                    break;
            }

            return true;
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerView,
                0, 0
        );

        drawerView.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

    }


    public void displayGamburger(boolean show) {
        runOnUiThread(() -> {
            if (show) {
                openMenuUserStatusDot.setVisibility(View.VISIBLE);
                gamburger.setVisibility(View.VISIBLE);
            } else {
                openMenuUserStatusDot.setVisibility(View.GONE);
                gamburger.setVisibility(View.GONE);
            }
        });
    }

    //    view implementation


    private UserModel.Status currentStatus;

    @Override
    public void onStatusChanged(UserModel.Status newStatus) {
        boolean accountStatusChanged = currentStatus == null || currentStatus.account != newStatus.account;
        boolean serviceStatusChanged = currentStatus == null || currentStatus.service != newStatus.service;

        if (accountStatusChanged) {
            switch (newStatus.account) {
                case DISAPPROVED:
                case NEW:
                    fragmentRouter.openDocuments();
                    runOnUiThread(() -> {
                        headerViewHolder.statusToggle.setDisapproved();
                        headerViewHolder.statusDot.setDisapproved();
                        openMenuUserStatusDot.setDisapproved();
                    });
                    break;
                case BLOCKED:
                    fragmentRouter.openOffline();
                    runOnUiThread(() -> {
                        offlineStatusTooggle.setBlocked();
                        headerViewHolder.statusToggle.setBlocked();
                        headerViewHolder.statusDot.setBlocked();
                        openMenuUserStatusDot.setBlocked();
                    });
                    break;
                case APPROVED:

                    break;
            }
        }

        if (serviceStatusChanged || (accountStatusChanged && newStatus.account == UserModel.Status.AccountStatus.APPROVED)) {
            switch (newStatus.service) {
                case ONLINE:
                    fragmentRouter.openMap();
                    fragmentRouter.closeOfflineAfterOneSecond();
                    runOnUiThread(() -> {
                        offlineStatusTooggle.setOnline();
                        offlineHeader.setText(getApplicationContext().getString(R.string.mas_offline_header_success));
                        headerViewHolder.statusDot.setOnline();
                        headerViewHolder.statusToggle.setOnline();
                        openMenuUserStatusDot.setOnline();
                    });
                    break;
                case OFFLINE:
                    fragmentRouter.openOffline();
                    runOnUiThread(() -> {
                        offlineStatusTooggle.setOffline();
                        headerViewHolder.statusToggle.setOffline();
                        headerViewHolder.statusDot.setOffline();
                        openMenuUserStatusDot.setOffline();
                    });
                    break;
            }
        }

        currentStatus = newStatus;
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentStatus = null;
        presenter.orderModel.clearState();
        presenter.userModel.clearState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentStatus = null;
        presenter.orderModel.clearState();
        presenter.userModel.clearState();
    }

    @Override
    public void onOrderChanged(OrderModel.Order newOrder) {
        Logger.w("ORDER");
        if (newOrder == null) return;
        fragmentRouter.openMap();
        switch (newOrder.status) {
            case SEARCHING:
                ((MapFragment) fragmentRouter.currentFragment).showRequestOrder(newOrder);
                break;
            case STARTED:
                ((MapFragment) fragmentRouter.currentFragment).showArrivedOrder(newOrder);
                break;
            case COMPLETED:
                ((MapFragment) fragmentRouter.currentFragment).showRateOrder(newOrder);
                break;
        }
    }


    protected class FragmentRouter {
        private FragmentManager fragmentManager;
        public Fragment currentFragment;

        public FragmentRouter() {
            fragmentManager = getSupportFragmentManager();
        }

        public void openMap() {
            try {
                //how about this?
                displayGamburger(true);

                String s = ((MapFragment) currentFragment).toString();
            } catch (ClassCastException | NullPointerException e) {
                currentFragment = new MapFragment();
                fragmentManager
                        .beginTransaction()
                        .replace(content.getId(), currentFragment)
                        .commit();
            }
        }

        public void openProfile() {
//            Intent i = new Intent(MainView.this, ShowProfile.class);
//            i.putExtra("user", presenter.userModel.getProfileData());
//            startActivity(i);
            Intent i = new Intent(MainView.this, EditProfile.class);
            startActivity(i);
        }

        public void openHelp() {
            displayGamburger(false);

            currentFragment = new Help();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(), currentFragment)
                    .commit();
        }

        public void openPayment() {
            displayGamburger(false);

            currentFragment = new SummaryFragment();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(), currentFragment)
                    .commit();
        }

        public void openWallet() {
            displayGamburger(false);
            currentFragment = new Wallet();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(), currentFragment)
                    .commit();
        }

        public void openDocuments() {
            startActivity(new Intent(MainView.this, DocumentsActivity.class));
        }

        public void openTrips() {
            startActivity(new Intent(MainView.this, HistoryActivity.class));
        }

        public void openShare() {
            //TODO: insert sharing app url
            //TODO: create chooser to share url
            String appSharingUrl = "https://www.google.com";
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, appSharingUrl);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        public void openSettings() {
            //TODO: remove with router
            startActivity(new Intent(MainView.this, ActivitySettings.class));
        }

        public void openOffline() {
            runOnUiThread(() -> {
                displayGamburger(false);
                offlineHeader.setText(getApplicationContext().getText(R.string.mas_offline_header));
                contentOverflow.setVisibility(View.VISIBLE);
                try {
                    ((MapFragment) currentFragment).hidePassItOnButton();
                } catch (ClassCastException | NullPointerException e) {
                }
            });
        }

        public void closeOffline() {
            runOnUiThread(() -> {
                displayGamburger(true);
                contentOverflow.setVisibility(View.GONE);
                try {
                    ((MapFragment) currentFragment).showPassItOnButton();
                } catch (ClassCastException | NullPointerException e) {
                }
            });
        }

        public void closeOfflineAfterOneSecond() {
            Observable
                    .timer(1, TimeUnit.SECONDS)
                    .flatMap(aLong -> {
                        closeOffline();
                        return Observable.empty();
                    })
                    .subscribe();
        }
    }

}
