package com.holler.app.FCM;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.holler.app.Activity.MainActivity;
import com.holler.app.Activity.SplashScreen;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.Utilities.Utilities;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "MyFirebaseMsgService";
    Utilities utils = new Utilities();

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getData() == null){
            Log.e(TAG, "Received empty message, notification will not build..");
            return;
        }

        Notification notification = buildNotification(remoteMessage);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);

    }

    /**
     * Builds notification object based on cases
     * 1. app in background
     *      - start main activity
     * 2. app not launched
     *      - start from splash screen
     * 3. remote message text
     */
    private Notification buildNotification(RemoteMessage message){
        //creating pending intent for interaction
        String messageBody = message.getData().get("message");
        Intent launchAppIntent;
        if(Utilities.isAppIsInBackground(getApplicationContext()) && messageBody.equalsIgnoreCase("New Incoming Ride")){
            launchAppIntent = new Intent(this, MainActivity.class);
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }else{
            launchAppIntent = new Intent(this, SplashScreen.class);
            launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchAppIntent.putExtra("push", true);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                launchAppIntent,
                PendingIntent.FLAG_ONE_SHOT
        );

        //configuration notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "");

        String notificationTitle = SharedHelper.getKey(this, "app_name");
        String notificationText = messageBody;
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_tone);

        notificationBuilder
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher, 1);

        return notificationBuilder.build();

    }

}
