package com.example.takepack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service
{
    MainActivity m = new MainActivity();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId )
    {
        // QQQ: 두번 이상 호출되지 않도록 조치해야 할 것 같다.
        Intent clsIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, clsIntent, 0);
        NotificationCompat.Builder clsBuilder;

        if( Build.VERSION.SDK_INT >= 26 )
        {
            String CHANNEL_ID = "channel_id";
            NotificationChannel clsChannel = new NotificationChannel( CHANNEL_ID, "서비스 앱", NotificationManager.IMPORTANCE_DEFAULT );
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel( clsChannel );

            clsBuilder = new NotificationCompat.Builder(this, CHANNEL_ID );
        }
        else
        {
            clsBuilder = new NotificationCompat.Builder(this );
        }

        // QQQ: notification 에 보여줄 타이틀, 내용을 수정한다.
        clsBuilder.setSmallIcon( R.drawable.main )
                .setContentTitle( "TakePack" ).setContentText( "실행중" )
                .setContentIntent( pendingIntent );


        // foreground 서비스로 실행한다.
        startForeground( 1, clsBuilder.build() );

        // QQQ: 쓰레드 등을 실행하여서
        // 서비스에 적합한 로직을 구현한다.


        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent )
    {
        return null;
    }

}