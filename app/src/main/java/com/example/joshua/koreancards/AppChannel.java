package com.example.joshua.koreancards;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class AppChannel extends Application {
    public static final String STUDY_WAIT_CHANNEL = "study_wait_channel";
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }
    public void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel studyWaitChannel = new NotificationChannel(
                    STUDY_WAIT_CHANNEL,
                    "Trance Vocabulary",
                    NotificationManager.IMPORTANCE_HIGH
            );
            studyWaitChannel.setDescription("Notification to resume studying.");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(studyWaitChannel);
        }
    }
}
