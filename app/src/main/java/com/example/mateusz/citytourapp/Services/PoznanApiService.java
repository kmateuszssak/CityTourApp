package com.example.mateusz.citytourapp.Services;

import android.location.Location;

import com.example.mateusz.citytourapp.Model.Geometry;
import com.example.mateusz.citytourapp.Model.poznanModels.ChurchesDTO;
import com.example.mateusz.citytourapp.Model.poznanModels.MonumentsDTO;
import com.example.mateusz.citytourapp.rest.RestClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Mateusz on 30.12.2017.
 */

public class PoznanApiService {

    public static final String MONUMENTS_URL = "http://www.poznan.pl/mim/plan/map_service.html?mtype=pub_transport&co=class_objects&class_id=2572";
    public static final String CHURCHES_URL = "http://www.poznan.pl/mim/plan/map_service.html?mtype=pub_transport&co=class_objects&class_id=2471";
    //public static final String getPictureURL = "http://www.poznan.pl/mim/upload/obiekty/";

    public URI getURIfromString(String s)
    {
        URI url = URI.create("");
        try {
            url = new URIBuilder(s)
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return url;
    }

    public MonumentsDTO getMonumentsDTO() {
        URI url = getURIfromString(MONUMENTS_URL);
        JSONObject response = null;
        MonumentsDTO monumentsDTO = null;

        try {
            response = RestClient.getJSON(null, url.toString(), null);
            Gson gson = new Gson();
            monumentsDTO = gson.fromJson(response.toString(), MonumentsDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return monumentsDTO;
    }

    public ChurchesDTO getChurchesDTO() {
        URI url = getURIfromString(CHURCHES_URL);
        JSONObject response = null;
        ChurchesDTO churchesDTO = null;

        try {
            response = RestClient.getJSON(null, url.toString(), null);
            Gson gson = new Gson();
            churchesDTO = gson.fromJson(response.toString(), ChurchesDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return churchesDTO;
    }

    public Location parseGeoLocationDTO(Geometry geometry) {
        Location location = new Location(geometry.type);
        location.setLongitude(geometry.coordinates.get(0));
        location.setLatitude(geometry.coordinates.get(1));

        return location;
    }

    public LatLng parseGeoLocationDTOLatLng(Geometry geometry) {
        return new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0));
    }
}
