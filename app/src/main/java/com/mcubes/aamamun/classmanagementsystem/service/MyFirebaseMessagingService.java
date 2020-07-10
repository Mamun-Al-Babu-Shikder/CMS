package com.mcubes.aamamun.classmanagementsystem.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mcubes.aamamun.classmanagementsystem.R;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by A.A.MAMUN on 3/18/2019.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_channel_01", "Channel_Title",NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000,1000});
            channel.setLightColor(Color.BLUE);
            nm.createNotificationChannel(channel);

        }
        Notification.Builder nb = new Notification.Builder(getBaseContext());
        nb.setSmallIcon(R.mipmap.applogo);
        nb.setVibrate(new long[]{1000,1000});
        nb.setLights(Color.BLUE, 3000, 3000);
        nb.setTicker(remoteMessage.getNotification().getBody());
        nb.setContentText(remoteMessage.getNotification().getBody());
        nb.setContentTitle(remoteMessage.getNotification().getTitle());
        nm.notify(((int)(Math.random()*100)+1), nb.build() );

    }




}
