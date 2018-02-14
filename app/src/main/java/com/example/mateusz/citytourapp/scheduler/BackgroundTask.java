package com.example.mateusz.citytourapp.scheduler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.mateusz.citytourapp.MapsActivity;
import com.example.mateusz.citytourapp.Model.Geometry;
import com.example.mateusz.citytourapp.Model.poznanModels.ChurchesDTO;
import com.example.mateusz.citytourapp.Model.poznanModels.Feature;
import com.example.mateusz.citytourapp.Model.poznanModels.MonumentsDTO;
import com.example.mateusz.citytourapp.R;
import com.example.mateusz.citytourapp.Services.OrangeApiService;
import com.example.mateusz.citytourapp.Services.PoznanApiService;

/**
 * Created by Luki on 2017-12-26.
 */

public class BackgroundTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = BackgroundTask.class.getSimpleName();
    private static String CHANNEL_ID = "BackgroundTask";
    private static final int NOTIFICATION_ID_OPEN_ACTIVITY = 9;
    private Context m_aContext;


    public BackgroundTask(Context aContext){
        this.m_aContext = aContext;
    }

    @Override
    protected String doInBackground(Void... voids) {
        openActivityNotification(this.m_aContext, getClosestAttraction());
        return "Job wykonany...";
    }

    private void openActivityNotification(Context aContext, String sText) {
        NotificationCompat.Builder aNotificationBuilder = new NotificationCompat.Builder(aContext, CHANNEL_ID);
        NotificationManager aNotificationManager = (NotificationManager) aContext.getSystemService(aContext.NOTIFICATION_SERVICE);

        Intent aNotifyIntent = new Intent(aContext, MapsActivity.class);

        aNotifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent aPendingIntent = PendingIntent.getActivity(aContext, 0, aNotifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        aNotificationBuilder.setContentIntent(aPendingIntent);

        aNotificationBuilder.setSmallIcon(R.drawable.ic_notification_icon);
        aNotificationBuilder.setAutoCancel(true);
        aNotificationBuilder.setContentTitle("Daj znać gdzie jesteś!");
        if(sText != null)
        {
            aNotificationBuilder.setContentText(sText);
        }
        else
        {
            aNotificationBuilder.setContentText("Udostępnij informację znajomym i pokaż im co ciekawego robisz.");
        }

        aNotificationManager.notify(NOTIFICATION_ID_OPEN_ACTIVITY, aNotificationBuilder.build());
    }

    private String getClosestAttraction()
    {
        //get Orange localization
        OrangeApiService orangeAPI = new OrangeApiService();
        Location currentLocation = orangeAPI.parseGeoLocationDTO(orangeAPI.getGeoLocalizationOrange());

        if(currentLocation != null) {
            Log.d(TAG, "Coords Orange API: " + currentLocation.getLongitude() + " : " + currentLocation.getLatitude());

            //get every selected monument etc.
            final PoznanApiService poznanApiService = new PoznanApiService();
            Feature closestFeature = null;
            float currentClosestDistance = 999999; //duza liczba poczatkowa zeby zbijac

            //TODO dodaj jakies ify zeby wyciagac tylko te zaznaczone w opcjach
            final ChurchesDTO churchesDTO = poznanApiService.getChurchesDTO();
            for (Feature feature : churchesDTO.getFeatures()) {
                if (closestFeature == null) {
                    closestFeature = feature;
                    continue;
                }

                Geometry newGeometry = feature.getGeometry();
                Location newLocation = new Location("Nowa lokacja");
                newLocation.setLatitude(newGeometry.getCoordinates().get(1));
                newLocation.setLongitude(newGeometry.getCoordinates().get(0));

                float newClosestDistance = currentLocation.distanceTo(newLocation);
                if (newClosestDistance < currentClosestDistance) {
                    closestFeature = feature;
                    currentClosestDistance = newClosestDistance;
                }
            }

            final MonumentsDTO monumentsDTO = poznanApiService.getMonumentsDTO();
            for (Feature feature : monumentsDTO.getFeatures()) {
                if (closestFeature == null) {
                    closestFeature = feature;
                    continue;
                }

                Geometry newGeometry = feature.getGeometry();
                Location newLocation = new Location("Nowa lokacja");
                newLocation.setLatitude(newGeometry.getCoordinates().get(1));
                newLocation.setLongitude(newGeometry.getCoordinates().get(0));

                float newClosestDistance = currentLocation.distanceTo(newLocation);
                if (newClosestDistance < currentClosestDistance) {
                    closestFeature = feature;
                    currentClosestDistance = newClosestDistance;
                }
            }

            //zwracamy informacje
            if (closestFeature != null) {
                Log.d(TAG, "Coords ClosestFeature: " + closestFeature.getGeometry().coordinates.get(0) + " : " + closestFeature.getGeometry().coordinates.get(1));
                String text = closestFeature.getProperties().getNazwa() + " w mieście " + closestFeature.getProperties().getMiasto();
                return text;
            } else {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
