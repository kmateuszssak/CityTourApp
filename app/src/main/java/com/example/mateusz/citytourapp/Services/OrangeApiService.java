package com.example.mateusz.citytourapp.Services;

/**
 * Created by Mateusz on 29.12.2017.
 */

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

    public LocalizationOrangeDTO getGeoLocation() {
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
            response = new DownloadFromOrangeAPI().execute(url).get();
            Gson gson = new Gson();
            localizationOrangeDTO = gson.fromJson(response.toString(), LocalizationOrangeDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  localizationOrangeDTO;
    }

    private class DownloadFromOrangeAPI extends AsyncTask<URI, Double, JSONObject> {

        @Override
        protected JSONObject doInBackground(URI... uris) {
            JSONObject response = RestClient.getJSON(null, uris[0].toString(), null);

            return response;
        }
    }
}
