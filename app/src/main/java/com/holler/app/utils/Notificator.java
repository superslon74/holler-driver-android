package com.holler.app.utils;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.firebase.messaging.RemoteMessage;
import com.holler.app.R;
import com.holler.app.Services.NotificationPublisher;

import java.util.Date;
import java.util.Map;

import androidx.core.app.NotificationCompat;

import static android.media.AudioManager.STREAM_NOTIFICATION;


public class Notificator {

    private static final String NOTIFICATION_CHANNEL_ID = "holler.app";
    private static final String NOTIFICATION_CHANNEL_NAME = "Holler App";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Holler App Notifications";

    private Context context;
    private NotificationManager notificationManager;

    private PendingIntent pendingIntent;
    private NotificationCompat.Builder notificationBuilder;

    public Notificator(Context context) {
        this.context = context;

        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel());
        }
    }


    public Notificator buildPendingIntent(Class<?> activityClass, Bundle arguments, int requestCode) {
        Intent launchAppIntent = new Intent(context, activityClass);
        if (arguments != null)
            for (String argumentKey : arguments.keySet()) {
                launchAppIntent.putExtra(argumentKey, (String) arguments.get(argumentKey));
            }
        launchAppIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        this.pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                launchAppIntent,
                PendingIntent.FLAG_ONE_SHOT
        );

        notificationBuilder
                .setContentIntent(pendingIntent);

        return this;
    }

    public Notificator buildNotification(RemoteMessage message) {
        buildNotification(new NotificationText(message));
        return this;
    }

    public Notificator buildNotification(NotificationText text) {
        this.notificationBuilder = buildNotification(context, notificationBuilder, text.title, text.message);

        return this;
    }

    private Notification build(){
        Notification notification = notificationBuilder.build();
//        notification.flags = Notification.FLAG_INSISTENT | Notification.FLAG_ONGOING_EVENT;

        return notification;
    }

    public void castNotification() {
        Notification notification = build();
        int notificationId = (int) (Math.random() * 1000);
        castCustomNotification(notificationId, notification);
    }

    private void castCustomNotification(int id, Notification n) {

        notificationManager.notify(id, n);
    }

    public static NotificationCompat.Builder buildNotification(Context context, NotificationCompat.Builder builder, String title, String text) {
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert_tone);

        builder
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(soundUri, STREAM_NOTIFICATION)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setSmallIcon(R.mipmap.ic_launcher, 1);

        return builder;
    }

    public static NotificationText generateTextBasedOnWarningMode() {
        String title = "1 HOUR WARNING";
        String text = "You have accepted scheduled ride";

        return new NotificationText(title, text);
    }

    public void scheduleNotification(int notificationId, Date scheduledDate) {

        long warningTime = 0;
        warningTime = 60 * 60 * 1000;

        Intent alarmIntent = new Intent(context, AlarmSignalReceiver.class);
        alarmIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        alarmIntent.putExtra(NotificationPublisher.NOTIFICATION, build());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long currentTime = SystemClock.elapsedRealtime();
        long alarmTime = currentTime + (scheduledDate.getTime() - new Date().getTime() - warningTime);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);

    }

    public void unscheduleNotification(int notificationId, Date scheduledDate) {

        long warningTime = 0;
        warningTime = 60 * 60 * 1000;

        Intent alarmIntent = new Intent(context, AlarmSignalReceiver.class);
        alarmIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        alarmIntent.putExtra(NotificationPublisher.NOTIFICATION, build());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long currentTime = SystemClock.elapsedRealtime();
        long alarmTime = currentTime + (scheduledDate.getTime() - new Date().getTime() - warningTime);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    public void cancelByOrderId(int orderId){
        notificationManager.cancel(orderId);
    }


    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel createChannel() {

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert_tone);
        AudioAttributes soundAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{500,500,500,100});
        notificationChannel.setSound(soundUri, soundAttributes);

        return notificationChannel;

    }

    public static class NotificationText {
        private final String MESSAGE_NEW_RIDE = "New Incoming Ride";

        public String title;
        public String message;

        public NotificationText(RemoteMessage message) {
            Map<String, String> data = message.getData();
            boolean isMessageEmpty = message.getData() == null || message.getData().get("message") == null;
            boolean isNewRide;
                    try {
                        isNewRide = !isMessageEmpty && message
                                .getData()
                                .get("message")
                                .substring(0, MESSAGE_NEW_RIDE.length())
                                .equalsIgnoreCase(MESSAGE_NEW_RIDE);
                    }catch (IndexOutOfBoundsException e){
                        isNewRide = false;
                    }

            if (isMessageEmpty) {
                NotificationText.this.title = Notificator.NOTIFICATION_CHANNEL_NAME;
                NotificationText.this.message = null;
            } else if (isNewRide) {
                NotificationText.this.title = MESSAGE_NEW_RIDE;
                NotificationText.this.message = message
                        .getData()
                        .get("message")
                        .substring(MESSAGE_NEW_RIDE.length())
                        .trim();
            } else {
                NotificationText.this.title = "Message";
                NotificationText.this.message = message.getData().get("message");
            }
        }

        public NotificationText(String title, String text) {
            NotificationText.this.message = text;
            NotificationText.this.title = title;
        }
    }

    public static class AlarmSignalReceiver extends BroadcastReceiver {

        public static String NOTIFICATION_ID = "notification_id";
        public static String NOTIFICATION = "notification";

        @Override
        public void onReceive(final Context context, Intent intent) {

            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);

            new Notificator(context)
                    .castCustomNotification(notificationId, notification);

        }
    }

}
