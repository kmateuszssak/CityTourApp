package com.example.mateusz.citytourapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

/**
 * Created by Mateusz on 09.12.2017.
 */

public class RestClient {
    private static final String TAG = "RestClient";

    public static JSONObject getJSON(Context aContext, String sEndpointUrl, HashMap<String, String> aHeaders) {
        URL aUrl = null;
        HttpURLConnection aConnection = null;
        JSONObject aData = null;

        try {
            aUrl = new URL(sEndpointUrl);
        } catch (MalformedURLException aEx) {
            Log.e(TAG, "Nie można przekonwertować adresu na URL! " + aEx.getMessage());
        }

        if (aUrl != null) {
            try {
                aConnection = (HttpURLConnection) aUrl.openConnection();

                //dodawanie nagłowków
                if (aHeaders != null) {
                    for (Map.Entry<String, String> aHeader : aHeaders.entrySet()) {
                        aConnection.setRequestProperty(aHeader.getKey(), aHeader.getValue());
                    }
                }

                BufferedReader aReader = new BufferedReader(
                        new InputStreamReader(aConnection.getInputStream(), "UTF-8"));

                StringBuffer aJsonResponse = new StringBuffer();
                String sTmp = "";
                while ((sTmp = aReader.readLine()) != null)
                    aJsonResponse.append(sTmp).append("\n");
                aReader.close();

                Log.d(TAG, aJsonResponse.toString());
                aData = new JSONObject(aJsonResponse.toString());

            } catch (IOException aEx) {
                Log.e(TAG, "IOException podczas wykonywania zapytania! " + aEx.getMessage());
            } catch (JSONException aEx) {
                Log.e(TAG, "Nie można przeparsować odpowiedzi do JSONa! " + aEx.getMessage());
            } finally {
                if (aConnection != null) {
                    aConnection.disconnect();
                }
            }
        }

        return aData;
    }
}
