package com.pnrhunter.FCM;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pnrhunter.mvp.main.MainView;
import com.pnrhunter.utils.Notificator;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        new Notificator(this)
                .buildPendingIntent(MainView.class, null, 0)
                .buildNotification(remoteMessage)
                .castNotification();

    }

}
