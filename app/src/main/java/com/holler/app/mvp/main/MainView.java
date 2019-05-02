package com.holler.app.mvp.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.holler.app.AndarApplication;
import com.holler.app.Fragment.Help;
import com.holler.app.Fragment.SummaryFragment;
import com.holler.app.Fragment.Wallet;
import com.holler.app.R;
import com.holler.app.activity.ActivitySettings;
import com.holler.app.activity.DocumentsActivity;
import com.holler.app.activity.HistoryActivity;
import com.holler.app.activity.MainActivity;
import com.holler.app.activity.Offline;
import com.holler.app.di.app.AppComponent;
import com.holler.app.di.app.components.DaggerMainComponent;
import com.holler.app.di.app.components.main.modules.MainScreenModule;
import com.holler.app.utils.CustomActivity;
import com.orhanobut.logger.Logger;

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

        @OnClick(R.id.ma_nav_user_state_toggle)
        public void toggleStatus() {
            Logger.i("User online" + this.statusToggle.isOnline());
            if (this.statusToggle.isOnline()) {
                statusToggle.setOffline();
                presenter.goOffline();
            } else if(this.statusToggle.isOffline()) {
                statusToggle.setOnline();
                presenter.goOnline();
            }
        }

        @OnClick(R.id.ma_nav_button_close)
        public void closeNavigation() {
            drawerView.closeDrawer(Gravity.LEFT);
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
        fragmentRouter.openMap();

        buildComponent();


        //TODO: load user image to nav header
        //TODO: change user status
        //TODO: add listener to user image

        //TODO: bind with gps tracker
    }

    @OnClick(R.id.ma_map_nav_open_button)
    public void toggleNavigationMenu(){
        if(drawerView.isDrawerOpen(Gravity.LEFT)){
            drawerView.closeDrawers();
        }else{
            drawerView.openDrawer(Gravity.LEFT);
        }
    }

    @OnClick(R.id.ma_offline_status_toggle)
    public void toggleStatus() {
        if (this.offlineStatusTooggle.isOnline()) {
            offlineStatusTooggle.setOffline();
            presenter.goOffline();
        } else if(this.offlineStatusTooggle.isOffline()) {
            offlineStatusTooggle.setOnline();
            presenter.goOnline();
        }
    }

    @OnClick(R.id.ma_offline_later_button)
    public void changeStatusLater(){
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
                    if(isLogoutConfirmed){
                        return presenter.logout();
                    }else{
                        return Observable.just(false);
                    }
                })
                .flatMap(isLoggedOut -> {
                    if(isLoggedOut){
                        presenter.goToWelcomeScreen();
                        return Observable.just(true);
                    }else{
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

    private Subject<Boolean> showLogoutConfirmation(){
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
                //TODO: what is coupon
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

    //    view implementation


    private UserModel.Status currentStatus;
    @Override
    public void onStatusChanged(UserModel.Status newStatus) {
        boolean accountStatusChanged = currentStatus==null || currentStatus.account!=newStatus.account;
        boolean serviceStatusChanged = currentStatus==null || currentStatus.service!=newStatus.service;

        if(accountStatusChanged){
                switch (newStatus.account){
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

        if(serviceStatusChanged || (accountStatusChanged && newStatus.account==UserModel.Status.AccountStatus.APPROVED)){
            switch (newStatus.service){
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

        currentStatus=newStatus;
    }



    protected class FragmentRouter {
        private FragmentManager fragmentManager;
        public Fragment currentFragment;

        public FragmentRouter() {
            fragmentManager = getSupportFragmentManager();
        }

        public void openMap() {
            try{
                //how about this?
                String s = ((MapFragment) currentFragment).toString();
            }catch (ClassCastException | NullPointerException e){
                currentFragment = new MapFragment();
                fragmentManager
                        .beginTransaction()
                        .replace(content.getId(), currentFragment)
                        .commit();
            }
        }

        public void openHelp() {
            currentFragment = new Help();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(), currentFragment)
                    .commit();
        }

        public void openPayment() {
            currentFragment = new SummaryFragment();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(), currentFragment)
                    .commit();
        }

        public void openWallet() {
            currentFragment = new Wallet();
            fragmentManager
                    .beginTransaction()
                    .replace(content.getId(), currentFragment)
                    .commit();
        }

        public void openDocuments() {
            //TODO: launch documents activity
            startActivity(new Intent(MainView.this, DocumentsActivity.class));
        }

        public void openTrips() {
            //TODO: launch trips activity
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

        public void openOffline(){
            runOnUiThread(() -> {
                offlineHeader.setText(getApplicationContext().getText(R.string.mas_offline_header));
                contentOverflow.setVisibility(View.VISIBLE);
            });
        }

        public void closeOffline(){
            runOnUiThread(() -> contentOverflow.setVisibility(View.GONE));
        }

        public void closeOfflineAfterOneSecond(){
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                closeOffline();
            },1000);
        }
    }

}
