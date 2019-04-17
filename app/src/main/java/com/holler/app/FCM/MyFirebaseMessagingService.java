package com.holler.app.FCM;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.holler.app.activity.MainActivity;
import com.holler.app.utils.Notificator;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        new Notificator(this)
                .buildPendingIntent(MainActivity.class, null, 0)
                .buildNotification(remoteMessage)
                .castNotification();

    }

}
