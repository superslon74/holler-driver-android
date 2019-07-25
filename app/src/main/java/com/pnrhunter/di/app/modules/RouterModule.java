package com.pnrhunter.di.app.modules;

import android.content.Context;
import android.content.Intent;

import com.pnrhunter.activity.ActivitySocialLogin;
import com.pnrhunter.activity.WaitingForApproval;
import com.pnrhunter.mvp.details.DetailsView;
import com.pnrhunter.mvp.details.TripsView;
import com.pnrhunter.mvp.documents.DocumentsView;
import com.pnrhunter.mvp.documents.WaitingForApprovalView;
import com.pnrhunter.mvp.login.EmailView;
import com.pnrhunter.mvp.login.PasswordView;
import com.pnrhunter.mvp.main.MainView;
import com.pnrhunter.mvp.password.ChangePasswordView;
import com.pnrhunter.mvp.password.ForgotPasswordView;
import com.pnrhunter.mvp.profile.EditProfileView;
import com.pnrhunter.mvp.register.RegisterView;
import com.pnrhunter.mvp.welcome.WelcomeView;
import com.orhanobut.logger.Logger;

import java.util.EmptyStackException;
import java.util.Stack;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RouterModule {


    @Singleton
    @Provides
    public RouterModule.Router provideRouter(Context context) {
        return new RouterModule.Router(context);
    }

    public class Router {
        private Context context;
        private Stack<Intent> routingHistory;

        public Router(Context context) {
            this.context = context;
            routingHistory = new Stack<>();
        }

        public void goToWelcomeScreen(){
            Intent i = new Intent(context, WelcomeView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToMainScreen(){
            Intent i = new Intent(context, MainView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToEditProfileScreen(){
            Intent i = new Intent(context, EditProfileView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        @Deprecated
        public void goToLoginScreen(){
            Intent i = new Intent(context, EmailView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToRegisterScreen() {
            Intent i = new Intent(context, RegisterView.class);
            i.putExtra("signup", true).putExtra("viewpager", "yes");
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        @Deprecated
        public void gotoSocialLogin(){
            Intent i = new Intent(context, ActivitySocialLogin.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToPasswordScreen(){
            Intent i = new Intent(context, PasswordView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToEmailScreen(){
            Intent i = new Intent(context, EmailView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToForgotPasswordScreen(){
            Intent i = new Intent(context, ForgotPasswordView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToChangePasswordScreen() {
            Intent i = new Intent(context, ChangePasswordView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToDocumentsScreen() {
            Intent i = new Intent(context, DocumentsView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goToTripsScreen() {
            Intent i = new Intent(context, TripsView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }


        public void goToWaitingForApprovalScreen() {
            Intent i = new Intent(context, WaitingForApprovalView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            saveToHistory(i);
            context.startActivity(i);
        }


        public void goToDetailsScreen(String type, String orderId) {
            Intent i = new Intent(context, DetailsView.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(DetailsView.ARG_TYPE, type);
            i.putExtra(DetailsView.ARG_ID, orderId);
            saveToHistory(i);
            context.startActivity(i);
        }

        public void goBack(){
            try {
                Intent last = routingHistory.pop();
                if(last.equals(currentIntent))
                    last = routingHistory.pop();
                currentIntent=last;
                saveToHistory(last);
                context.startActivity(last);
            }catch (EmptyStackException e){
                Logger.e("Can't go back");
            }
        }

        private Intent currentIntent;

        private void saveToHistory(Intent i){
            currentIntent = i;
            routingHistory.push(i);
        }

    }

}
