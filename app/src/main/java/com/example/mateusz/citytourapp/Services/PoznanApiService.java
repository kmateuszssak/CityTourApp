package com.example.mateusz.citytourapp.Services;

import android.location.Location;

import com.example.mateusz.citytourapp.Model.Geometry;
import com.example.mateusz.citytourapp.Model.MonumentsDTO;
import com.example.mateusz.citytourapp.rest.RestClient;
import com.google.gson.Gson;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Mateusz on 30.12.2017.
 */

public class PoznanApiService {

    public static final String getMonumentsURL = "http://www.poznan.pl/mim/plan/map_service.html?mtype=pub_transport&co=class_objects&class_id=2572";

    public MonumentsDTO getMonumentsDTO() {
        URI url = URI.create("");

        try {
            url = new URIBuilder(getMonumentsURL)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        JSONObject response = null;
        MonumentsDTO monumentsDTO = null;
        try {
            response = RestClient.getJSON(null, url.toString(), null);//response = new DownloadFromOrangeAPI().execute(url);
            Gson gson = new Gson();
            monumentsDTO = gson.fromJson(response.toString(), MonumentsDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  monumentsDTO;
    }

    public Location parseGeoLocationDTO(Geometry geometry)
    {
        Location location = new Location(geometry.type);
        location.setLongitude(geometry.coordinates.get(0));
        location.setLatitude(geometry.coordinates.get(1));

        return location;
    }
}
