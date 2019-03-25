package com.holler.app.utils;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Application;
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
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.holler.app.Activity.HistoryDetails;
import com.holler.app.Activity.MainActivity;
import com.holler.app.Activity.SplashScreen;
import com.holler.app.Helper.SharedHelper;
import com.holler.app.R;
import com.holler.app.Services.NotificationPublisher;

import java.util.Date;


public class Notificator {

    private static final String NOTIFICATION_CHANNEL_ID = "holler.app";
    private static final String NOTIFICATION_CHANNEL_NAME = "Holler App";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Holler App Notifications";

    private Context context;
    private NotificationManager notificationManager;

    private PendingIntent pendingIntent;
    private NotificationCompat.Builder notificationBuilder;

    public Notificator(Context context){
        this.context = context;

        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(createChannel());
        }
    }


    public Notificator buildPendingIntent(Class<?> activityClass, Bundle arguments, int requestCode){
        Intent launchAppIntent = new Intent(context,activityClass);
        if(arguments!=null)
        for(String argumentKey : arguments.keySet()){
            launchAppIntent.putExtra(argumentKey,(Parcelable) arguments.get(argumentKey));
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

    public Notificator buildNotification(RemoteMessage message){
        buildNotification(new NotificationText(message));
        return this;
    }

    public Notificator buildNotification(NotificationText text){
        this.notificationBuilder = buildNotification(context,notificationBuilder,text.title,text.message);

        return this;
    }

    public void castNotification(){
        Notification notification = notificationBuilder.build();
        int notificationId = (int)(Math.random()*1000);
        castCustomNotification(notificationId, notification);
    }

    private void castCustomNotification(int id, Notification n){

        notificationManager.notify(id, n);
    }

    public static NotificationCompat.Builder buildNotification(Context context, NotificationCompat.Builder builder, String title, String text){
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert_tone);

        builder
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher, 1);

        return builder;
    }

    public static NotificationText generateTextBasedOnWarningMode(int warningMode){
        String title = "";
        String text = "";
        switch (warningMode){
            case WARNING_10_MIN: title="10 MIN WARNING"; break;
            case WARNING_30_MIN: title="30 MIN WARNING";  break;
            case WARNING_60_MIN: title="1 HOUR WARNING"; break;
        }
        text = "You have accepted scheduled ride on tomorrow";

        return new NotificationText(title,text);
    }

    public static final int WARNING_10_MIN = 1;
    public static final int WARNING_30_MIN = 2;
    public static final int WARNING_60_MIN = 3;

    public void scheduleNotification(int warningMode, int notificationId, Date scheduledDate){

        long warningTime = 0;
        String title = "WARNING";
        switch (warningMode){
            case WARNING_10_MIN: warningTime = 10*60*1000; break;
            case WARNING_30_MIN: warningTime = 30*60*1000; break;
            case WARNING_60_MIN: warningTime = 60*60*1000; break;
        }


        Intent alarmIntent = new Intent(context, AlarmSignalReceiver.class);
        alarmIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        alarmIntent.putExtra(NotificationPublisher.NOTIFICATION, notificationBuilder.build());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        long currentTime = SystemClock.elapsedRealtime();
        long alarmTime =  currentTime + (scheduledDate.getTime() - new Date().getTime() - warningTime);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);

    }

    public void cancelAllNotifications(){
        notificationManager.cancelAll();
    }



    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel createChannel(){

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert_tone);
        AudioAttributes soundAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationChannel.setSound(soundUri, soundAttributes);

        return notificationChannel;

    }

    public static class NotificationText{
        String title;
        String message;

        public NotificationText(RemoteMessage message){
            if(message.getData()==null)
            this.title = Notificator.NOTIFICATION_CHANNEL_NAME;
            this.message = (message.getData()!=null)
                    ?message.getData().get("message")
                    :"New message";
        }

        public NotificationText(String title, String text){
            this.message=text;
            this.title = title;
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
                    .castCustomNotification(notificationId,notification);

        }
    }

}
