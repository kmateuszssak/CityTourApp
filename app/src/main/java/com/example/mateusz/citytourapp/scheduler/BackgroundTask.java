package com.example.mateusz.citytourapp.scheduler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.example.mateusz.citytourapp.MainActivity;
import com.example.mateusz.citytourapp.R;

/**
 * Created by Luki on 2017-12-26.
 */

public class BackgroundTask extends AsyncTask<Void, Void, String> {

    private static String CHANNEL_ID = "BackgroundTask";
    private static final int NOTIFICATION_ID_OPEN_ACTIVITY = 9;
    private Context m_aContext;


    public BackgroundTask(Context aContext){
        this.m_aContext = aContext;
    }

    @Override
    protected String doInBackground(Void... voids) {
        openActivityNotification(this.m_aContext);
        return "Job wykonany...";
    }

    private void openActivityNotification(Context aContext) {
        NotificationCompat.Builder aNotificationBuilder = new NotificationCompat.Builder(aContext, CHANNEL_ID);
        NotificationManager aNotificationManager = (NotificationManager) aContext.getSystemService(aContext.NOTIFICATION_SERVICE);

        Intent aNotifyIntent = new Intent(aContext, MainActivity.class);

        aNotifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent aPendingIntent = PendingIntent.getActivity(aContext, 0, aNotifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        aNotificationBuilder.setContentIntent(aPendingIntent);

        aNotificationBuilder.setSmallIcon(R.drawable.ic_notification_icon);
        aNotificationBuilder.setAutoCancel(true);
        aNotificationBuilder.setContentTitle("DEMO");
        aNotificationBuilder.setContentText("witam");

        aNotificationManager.notify(NOTIFICATION_ID_OPEN_ACTIVITY, aNotificationBuilder.build());
    }
}
