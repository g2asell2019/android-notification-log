package org.hcilab.projects.nlogx.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public abstract class BaseForegroundServiceJava extends Service {

    protected final IBinder mBinder = new LocalBinder();
    protected int REQUEST_CODE, NOTIFICATION_ID = 3000;
    protected String CHANNEL_ID = "ForegroundService_ID";
    protected String CHANNEL_NAME = "ForegroundService Channel";
    protected NotificationManager mNotificationManager;
    protected NotificationCompat.Builder mNotificationBuilder;

    public static void start(Context context, Class<? extends BaseForegroundServiceJava> serviceClass) {
        ContextCompat.startForegroundService(context, new Intent(context, serviceClass));
    }

    public static void stop(Context context, Class<? extends BaseForegroundServiceJava> serviceClass) {
        context.stopService(new Intent(context, serviceClass));
    }

    protected abstract Notification serviceNotification();

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = serviceNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION : 0);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    protected Notification createNotification(String title, String message, int smallIcon, int bigIcon, Class<?> intentClass) {

        // Get the layouts to use in the custom notification
        // RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_main);
        // notificationLayout.setTextViewText(R.id.txtTitle, message);
        // .setCustomBigContentView(notificationLayout);

        mNotificationBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        if (smallIcon != 0) {
            mNotificationBuilder.setSmallIcon(smallIcon);
        }

        if (bigIcon != 0) {
            mNotificationBuilder.setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), bigIcon), 128, 128, true));
        }

        if (intentClass != null) {
            Intent notificationIntent = new Intent(this, intentClass);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.setContentIntent(pendingIntent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }

        return mNotificationBuilder.build();
    }

    public class LocalBinder extends Binder {
        public BaseForegroundServiceJava getService() {
            return BaseForegroundServiceJava.this;
        }
    }
}