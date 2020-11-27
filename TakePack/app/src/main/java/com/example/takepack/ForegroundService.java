package com.example.takepack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service
{
    MainActivity m = new MainActivity();

    @Override
    public void onCreate()
    {
        super.onCreate();
        startForegroundService();
    }
    void startForegroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.foreground_service);

        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "snwodeer_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "SnowDeer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.drawable.main)
                .setContent(remoteViews)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        startForeground(1, builder.build());
    }
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId )
//    {
//        Log.i("Foreground","작동됨");
//        Intent clsIntent = new Intent(this, MainActivity.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, clsIntent, 0);
//        NotificationCompat.Builder clsBuilder;
//
//        if( Build.VERSION.SDK_INT >= 26 )
//        {
//            String CHANNEL_ID = "channel_id";
//            NotificationChannel clsChannel = new NotificationChannel( CHANNEL_ID, "서비스 앱", NotificationManager.IMPORTANCE_DEFAULT );
//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel( clsChannel );
//            clsBuilder = new NotificationCompat.Builder(this, CHANNEL_ID );
//        }
//        else
//        {
//            clsBuilder = new NotificationCompat.Builder(this );
//        }
//        clsBuilder.setSmallIcon( R.drawable.main )
//                .setContentTitle( "TakePack" ).setContentText( "실행중" )
//                .setContentIntent( pendingIntent );
//
//        startForeground( 1, clsBuilder.build());
//        return START_STICKY;
//    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) { //핸들링 하는 부분
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    @Override
    public IBinder onBind(Intent intent )
    {
        return null;
    }

}