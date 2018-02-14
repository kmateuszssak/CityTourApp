package com.example.mateusz.citytourapp.Services;

/**
 * Created by Mateusz on 29.12.2017.
 */

import android.location.Location;
import android.os.AsyncTask;

import com.example.mateusz.citytourapp.Constans;
import com.example.mateusz.citytourapp.Model.LocalizationOrangeDTO;
import com.example.mateusz.citytourapp.rest.RestClient;
import com.google.gson.Gson;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class OrangeApiService {

    public static final String getLocationURL = "https://apitest.orange.pl/Localization/v1/GeoLocation";

    public LocalizationOrangeDTO getGeoLocalizationOrange() {
        URI url = URI.create("");

        try {
            url = new URIBuilder(getLocationURL)
                    .addParameter("msisdn", Constans.msisdn)
                    .addParameter("apikey", Constans.apiKeyOrange)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        JSONObject response = null;
        LocalizationOrangeDTO localizationOrangeDTO = null;
        try {
            response = RestClient.getJSON(null, url.toString(), null);//response = new DownloadFromOrangeAPI().execute(url);
            Gson gson = new Gson();
            localizationOrangeDTO = gson.fromJson(response.toString(), LocalizationOrangeDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  localizationOrangeDTO;
    }

    public Location getGeoLocation() {
        return parseGeoLocationDTO(getGeoLocalizationOrange());
    }

    public Location parseGeoLocationDTO(LocalizationOrangeDTO localizationOrangeDTO)
    {
        if(localizationOrangeDTO == null)
        {
            return null;
        }
        String longitude1 = localizationOrangeDTO.getLongitude();
        double longitude = Double.parseDouble(longitude1.substring(0, longitude1.length() - 1));

        String latitude1 = localizationOrangeDTO.getLatitude();
        double latitude = Double.parseDouble(latitude1.substring(0, latitude1.length() - 1));

        Location location = new Location(localizationOrangeDTO.getResult());
        location.setLongitude(longitude);
        location.setLatitude(latitude);

        return location;
    }

    private class DownloadFromOrangeAPI extends AsyncTask<URI, Double, JSONObject> {

        @Override
        protected JSONObject doInBackground(URI... uris) {
            JSONObject response = RestClient.getJSON(null, uris[0].toString(), null);

            return response;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

        }
    }
}
